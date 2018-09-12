package io.github.yizhiru.thulac4j;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.yizhiru.thulac4j.util.ModelPaths.*;

/**
 * 中文分词.
 */
public final class Segmenter {

	private static final SPChineseTokenizer TOKENIZER = new SPChineseTokenizer(
			Segmenter.class.getResourceAsStream(SEGMENTER_WEIGHT_PATH),
			Segmenter.class.getResourceAsStream(SEGMENTER_FEATURE_PATH),
			Segmenter.class.getResourceAsStream(SEGMENTER_LABEL_PATH));

	/**
	 * 中文分词
	 *
	 * @param text 待分词文本
	 * @return 分词结果
	 */
	public static List<String> segment(String text) {
		return TOKENIZER.tokenize(text)
				.stream()
				.map(item -> (item.word))
				.collect(Collectors.toList());
	}

	/**
	 * 添加自定义词典
	 *
	 * @param words 词典
	 */
	public static void addUserWords(List<String> words) {
		TOKENIZER.addUserWords(words);
	}

	/**
	 * 开启开启书名单独成词
	 */
	public static void enableTitleWord() {
		TOKENIZER.enableTitleWord();
	}

	/**
	 * 开启停用词过滤
	 */
	public static void enableFilterStopWords() {
		TOKENIZER.enableFilterStopWords();
	}

	/**
	 * 开启转简写
	 */
	public static void enableConvertToSimplifiedCHN() {
		TOKENIZER.enableConvertToSimplifiedCHN();
	}
}
