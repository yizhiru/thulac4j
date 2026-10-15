package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.common.DoubleArrayTrie;
import io.github.yizhiru.thulac4j.common.Nullable;
import io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronClassifier;
import io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel;
import io.github.yizhiru.thulac4j.process.LexiconCementer;
import io.github.yizhiru.thulac4j.process.RuleAnnotator;
import io.github.yizhiru.thulac4j.process.SpecifiedWordCementer;
import io.github.yizhiru.thulac4j.term.AnnotatedTerms;
import io.github.yizhiru.thulac4j.term.TokenItem;
import io.github.yizhiru.thulac4j.util.ChineseUtils;
import io.github.yizhiru.thulac4j.util.IOUtils;
import io.github.yizhiru.thulac4j.util.ModelPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import static io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel.PocMark.*;

public class SPChineseTokenizer {

	/**
	 * 结构感知器模型
	 */
	private final StructuredPerceptronClassifier classifier;

	/**
	 * 前向Label 二维数组
	 */
	protected int[][] previousTrans;

	/**
	 * 地名 ns 词典黏结.
	 */
	public final LexiconCementer nsCementer;

	/**
	 * 习语 idiom 词典黏结.
	 */
	public final LexiconCementer idiomCementer;

	/**
	 * 自定义词典，可为null
	 */
	@Nullable
	protected volatile LexiconCementer uwCementer = null;
	private List<String> userWords = Collections.emptyList();
	private volatile TokenizerConfig config;

	private static final class Dictionaries {
		static final LexiconCementer NS = load(ModelPaths.NS_BIN_PATH, "ns");
		static final LexiconCementer IDIOM = load(ModelPaths.IDIOM_BIN_PATH, "i");

		private static LexiconCementer load(String path, String pos) {
			try {
				return new LexiconCementer(IOUtils.openResource(path), pos);
			} catch (IOException e) {
				throw new UncheckedIOException("Cannot load dictionary " + path, e);
			}
		}
	}

	/** Loads a model and takes ownership of all three streams, including on failure. */
	public SPChineseTokenizer(InputStream weightInput, InputStream featureInput, InputStream labelInput) {
		this(loadModel(weightInput, featureInput, labelInput), TokenizerConfig.DEFAULT);
	}

	/** The supplied model may be shared, but its arrays must not be modified after publication. */
	public SPChineseTokenizer(StructuredPerceptronModel model, TokenizerConfig config) {
		this.classifier = new StructuredPerceptronClassifier(Objects.requireNonNull(model, "model"));
		this.config = Objects.requireNonNull(config, "config");
		this.nsCementer = Dictionaries.NS;
		this.idiomCementer = Dictionaries.IDIOM;
		this.previousTrans = setPreviousTransitions(classifier.getLabelValues());
	}

	private static StructuredPerceptronModel loadModel(InputStream weights, InputStream features, InputStream labels) {
		try {
			return new StructuredPerceptronModel(weights, features, labels);
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot load tokenizer model", e);
		}
	}

	/**
	 * Label 前向转移图
	 *
	 * @param labelValues label值
	 * @return 前向转移二维数组，每行表示该label的所有前向label
	 */
	static int[][] setPreviousTransitions(String[] labelValues) {
		int labelSize = labelValues.length;
		List<List<Integer>> labelTransitions = new ArrayList<>();
		for (int i = 0; i < labelSize; i++) {
			labelTransitions.add(new ArrayList<>());
		}
		for (int cur = 0; cur < labelSize; cur++) {
			for (int pre = 0; pre < labelSize; pre++) {
				String curString = labelValues[cur];
				String preString = labelValues[pre];
				char curPoc = curString.charAt(0);
				char prePoc = preString.charAt(0);
				// 如果有相同词性或者不带词性，按转移规则进行转移
				if (curString.substring(1).equals(preString.substring(1))) {
					// B 前面只能是E 或S
					if (curPoc == POS_B_CHAR) {
						if (prePoc == POS_E_CHAR || prePoc == POS_S_CHAR) {
							labelTransitions.get(cur).add(pre);
						}
					}
					// M 前面只能是M 或 B
					else if (curPoc == POS_M_CHAR) {
						if (prePoc == POS_M_CHAR || prePoc == POS_B_CHAR) {
							labelTransitions.get(cur).add(pre);
						}
					}
					// E 前面只能是B 或 M
					else if (curPoc == POS_E_CHAR) {
						if (prePoc == POS_B_CHAR || prePoc == POS_M_CHAR) {
							labelTransitions.get(cur).add(pre);
						}
					}
					// S 前面只能是E 或 S
					else if (curPoc == POS_S_CHAR) {
						if (prePoc == POS_E_CHAR || prePoc == POS_S_CHAR) {
							labelTransitions.get(cur).add(pre);
						}
					}
				}
				// 如果带有词性并且前后词性不相同，那么则按规则
				// B 前面只能是E 或S，S 前面只能是E 或S 进行转移
				else if (curString.length() > 1) {
					if (curPoc == POS_B_CHAR || curPoc == POS_S_CHAR) {
						if (prePoc == POS_E_CHAR || prePoc == POS_S_CHAR) {
							labelTransitions.get(cur).add(pre);
						}
					}
				}
			}
		}
		// 将List 转成二维数组
		int[][] previousTrans = new int[labelSize][];
		for (int i = 0; i < labelSize; i++) {
			previousTrans[i] = new int[labelTransitions.get(i).size()];
			for (int j = 0; j < labelTransitions.get(i).size(); j++) {
				previousTrans[i][j] = labelTransitions.get(i).get(j);
			}
		}
		return previousTrans;
	}

	/**
	 * 序列标注分词
	 *
	 * @param text 输入文本
	 * @return 序列标注结果
	 */
	public List<TokenItem> tokenize(String text) {
		Objects.requireNonNull(text, "text");
		TokenizerConfig options = config;
		LexiconCementer userCementer = uwCementer;
		List<TokenItem> tokenItems = new ArrayList<>();
		if (text.length() == 0) {
			return tokenItems;
		}

		AnnotatedTerms annotatedTerms;
		// 若开启转简体
		if (options.convertToSimplifiedChinese) {
			String simplifiedSentence = ChineseUtils.simplified(text);
			annotatedTerms = RuleAnnotator.annotate(simplifiedSentence, options.titleWord);
		} else {
			annotatedTerms = RuleAnnotator.annotate(text, options.titleWord);
		}
		if (annotatedTerms.isEmpty()) {
			return tokenItems;
		}

		int[] labels = classifier.classify(annotatedTerms, previousTrans);

		char[] rawChars = annotatedTerms.getPreAnnotateChars();
		String[] labelValues = classifier.getLabelValues();
		for (int i = 0, offset = 0; i < rawChars.length; i++) {
			String label = labelValues[labels[i]];
			char pocChar = label.charAt(0);
			if (pocChar == POS_E_CHAR || pocChar == POS_S_CHAR) {
				String word = new String(rawChars, offset, i + 1 - offset);
				if (label.length() >= 2) {
					tokenItems.add(new TokenItem(word, label.substring(1)));
				} else {
					tokenItems.add(new TokenItem(word, null));
				}
				offset = i + 1;
			}
		}

		// 地名词典黏结
		nsCementer.cement(tokenItems);
		// 习语词典黏结
		idiomCementer.cement(tokenItems);
		// 特定词语黏结
		SpecifiedWordCementer.cementWord(tokenItems);
		if (userCementer != null) {
			userCementer.cement(tokenItems);
		}
		// Filter only after cementing, preserving punctuation boundaries during matching.
		if (options.filterStopWords) {
			filterStopWords(tokenItems);
		}
		return tokenItems;
	}

	/**
	 * 添加自定义词典
	 *
	 * @param words 词典
	 */
	public synchronized void addUserWords(List<String> words) {
		List<String> combined = new ArrayList<>(userWords);
		combined.addAll(Objects.requireNonNull(words, "words"));
		setUserWords(combined);
	}

	/** Replaces the user dictionary atomically. An empty list clears it. */
	public synchronized void setUserWords(List<String> words) {
		Objects.requireNonNull(words, "words");
		TreeSet<String> normalized = new TreeSet<>();
		for (String word : words) {
			if (word == null || word.trim().isEmpty()) {
				throw new IllegalArgumentException("User dictionary words must not be null or blank");
			}
			normalized.add(word.trim());
		}
		List<String> replacement = new ArrayList<>(normalized);
		LexiconCementer cementer = replacement.isEmpty() ? null
				: new LexiconCementer(DoubleArrayTrie.make(replacement), "uw");
		userWords = replacement;
		uwCementer = cementer;
	}

	public TokenizerConfig getConfig() {
		return config;
	}

	public synchronized void setConfig(TokenizerConfig config) {
		this.config = Objects.requireNonNull(config, "config");
	}

	public void enableTitleWord() { setTitleWord(true); }
	public void enableFilterStopWords() { setFilterStopWords(true); }
	public void enableConvertToSimplifiedCHN() { setConvertToSimplifiedCHN(true); }

	public synchronized void setTitleWord(boolean enabled) {
		config = config.withTitleWord(enabled);
	}

	public synchronized void setFilterStopWords(boolean enabled) {
		config = config.withFilterStopWords(enabled);
	}

	public synchronized void setConvertToSimplifiedCHN(boolean enabled) {
		config = config.withConvertToSimplifiedChinese(enabled);
	}

	private void filterStopWords(List<TokenItem> tokenItems) {
		tokenItems.removeIf(item -> ChineseUtils.isStopWord(item.word));
	}
}
