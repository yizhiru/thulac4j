package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel;
import io.github.yizhiru.thulac4j.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.yizhiru.thulac4j.util.ModelPaths.*;

/**
 * 中文分词.
 */
public final class Segmenter {

	private static final class ModelHolder {
		static final StructuredPerceptronModel MODEL = load();

		private static StructuredPerceptronModel load() {
			try (InputStream weights = IOUtils.openResource(SEGMENTER_WEIGHT_PATH);
			     InputStream features = IOUtils.openResource(SEGMENTER_FEATURE_PATH);
			     InputStream labels = IOUtils.openResource(SEGMENTER_LABEL_PATH)) {
				return new StructuredPerceptronModel(weights, features, labels);
			} catch (IOException e) {
				throw new UncheckedIOException("Cannot load segmentation model", e);
			}
		}
	}

	private static final class DefaultHolder {
		static final SPChineseTokenizer TOKENIZER = newTokenizer();
	}

	/** Creates an independent tokenizer while sharing the read-only model and built-in dictionaries. */
	public static SPChineseTokenizer newTokenizer() {
		return newTokenizer(TokenizerConfig.DEFAULT);
	}

	public static SPChineseTokenizer newTokenizer(TokenizerConfig config) {
		return new SPChineseTokenizer(ModelHolder.MODEL, config);
	}

	/**
	 * 中文分词
	 *
	 * @param text 待分词文本
	 * @return 分词结果
	 */
	public static List<String> segment(String text) {
		return DefaultHolder.TOKENIZER.tokenize(text)
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
		DefaultHolder.TOKENIZER.addUserWords(words);
	}

	/**
	 * 开启开启书名单独成词
	 */
	public static void enableTitleWord() {
		DefaultHolder.TOKENIZER.enableTitleWord();
	}

	/**
	 * 开启停用词过滤
	 */
	public static void enableFilterStopWords() {
		DefaultHolder.TOKENIZER.enableFilterStopWords();
	}

	/**
	 * 开启转简写
	 */
	public static void enableConvertToSimplifiedCHN() {
		DefaultHolder.TOKENIZER.enableConvertToSimplifiedCHN();
	}

	public static void setUserWords(List<String> words) { DefaultHolder.TOKENIZER.setUserWords(words); }
	public static void setConfig(TokenizerConfig config) { DefaultHolder.TOKENIZER.setConfig(config); }
	public static void setTitleWord(boolean enabled) { DefaultHolder.TOKENIZER.setTitleWord(enabled); }
	public static void setFilterStopWords(boolean enabled) { DefaultHolder.TOKENIZER.setFilterStopWords(enabled); }
	public static void setConvertToSimplifiedCHN(boolean enabled) { DefaultHolder.TOKENIZER.setConvertToSimplifiedCHN(enabled); }
}
