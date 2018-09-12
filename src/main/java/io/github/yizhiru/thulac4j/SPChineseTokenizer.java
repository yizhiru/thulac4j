package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.common.DoubleArrayTrie;
import io.github.yizhiru.thulac4j.util.ModelPaths;
import io.github.yizhiru.thulac4j.common.Nullable;
import io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronClassifier;
import io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel;
import io.github.yizhiru.thulac4j.process.RuleAnnotator;
import io.github.yizhiru.thulac4j.process.LexiconCementer;
import io.github.yizhiru.thulac4j.process.SpecifiedWordCementer;
import io.github.yizhiru.thulac4j.term.AnnotatedTerms;
import io.github.yizhiru.thulac4j.term.TokenItem;
import io.github.yizhiru.thulac4j.util.ChineseUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel.PocMark.*;

public class SPChineseTokenizer {

	/**
	 * 结构感知器模型
	 */
	private StructuredPerceptronClassifier classifier;

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
	protected LexiconCementer uwCementer = null;

	private static final class Config {

		/**
		 * 是否开启黏结书名号内的词.
		 */
		private static boolean isEnableTileWord = false;

		/**
		 * 是否开启停用词过滤
		 */
		private static boolean isEnableFilterStopWords = false;

		/**
		 * 是否开启转简体中文
		 */
		private static boolean isEnableConvertToSimplifiedCHN = false;

	}

	SPChineseTokenizer(InputStream weightInput, InputStream featureInput, InputStream labelInput) {
		try {
			this.classifier = new StructuredPerceptronClassifier(
					new StructuredPerceptronModel(weightInput, featureInput, labelInput));
			this.nsCementer = new LexiconCementer(
					this.getClass().getResourceAsStream(ModelPaths.NS_BIN_PATH), "ns");
			this.idiomCementer = new LexiconCementer(
					this.getClass().getResourceAsStream(ModelPaths.IDIOM_BIN_PATH), "i");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.previousTrans = setPreviousTransitions(classifier.getLabelValues());
	}

	/**
	 * Label 前向转移图
	 *
	 * @param labelValues label值
	 * @return 前向转移二维数组，每行表示该label的所有前向label
	 */
	private int[][] setPreviousTransitions(String[] labelValues) {
		int labelSize = labelValues.length;
		List<List<Integer>> labelTransitions = new ArrayList<>();
		for (int i = 0; i < labelSize; i++) {
			labelTransitions.add(new LinkedList<>());
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
		List<TokenItem> tokenItems = new ArrayList<>();
		if (text.length() == 0) {
			return tokenItems;
		}

		AnnotatedTerms annotatedTerms;
		// 若开启转简体
		if (Config.isEnableConvertToSimplifiedCHN) {
			String simplifiedSentence = ChineseUtils.simplified(text);
			annotatedTerms = RuleAnnotator.annotate(simplifiedSentence, Config.isEnableTileWord);
		} else {
			annotatedTerms = RuleAnnotator.annotate(text, Config.isEnableTileWord);
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
		// 若开启停用词过滤
		if (Config.isEnableFilterStopWords) {
			filterStopWords(tokenItems);
		}
		// 地名词典黏结
		nsCementer.cement(tokenItems);
		// 习语词典黏结
		idiomCementer.cement(tokenItems);
		// 特定词语黏结
		SpecifiedWordCementer.cementWord(tokenItems);
		if (uwCementer != null) {
			uwCementer.cement(tokenItems);
		}
		return tokenItems;
	}

	/**
	 * 添加自定义词典
	 *
	 * @param words 词典
	 */
	public void addUserWords(List<String> words) {
		DoubleArrayTrie dat = DoubleArrayTrie.make(words);
		this.uwCementer = new LexiconCementer(dat, "uw");
	}

	/**
	 * 开启书名单独成词
	 */
	public void enableTitleWord() {
		Config.isEnableTileWord = true;
	}

	/**
	 * 开启停用词过滤
	 */
	public void enableFilterStopWords() {
		Config.isEnableFilterStopWords = true;
	}

	/**
	 * 开启转简写
	 */
	public void enableConvertToSimplifiedCHN() {
		Config.isEnableConvertToSimplifiedCHN = true;
	}

	/**
	 * 过滤停用词
	 *
	 * @param tokenItems 解码结果
	 */
	private void filterStopWords(List<TokenItem> tokenItems) {
		for (int i = 0; i < tokenItems.size(); ) {
			if (ChineseUtils.isStopWord(tokenItems.get(i).word)) {
				tokenItems.remove(i);
			} else {
				i++;
			}
		}
	}
}
