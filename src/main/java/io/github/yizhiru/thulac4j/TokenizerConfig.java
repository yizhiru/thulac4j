package io.github.yizhiru.thulac4j;

/** Immutable options. Each tokenizer captures one configuration per request. */
public final class TokenizerConfig {
	public static final TokenizerConfig DEFAULT = new TokenizerConfig(false, false, false);

	public final boolean titleWord;
	public final boolean filterStopWords;
	public final boolean convertToSimplifiedChinese;

	public TokenizerConfig(boolean titleWord, boolean filterStopWords, boolean convertToSimplifiedChinese) {
		this.titleWord = titleWord;
		this.filterStopWords = filterStopWords;
		this.convertToSimplifiedChinese = convertToSimplifiedChinese;
	}

	public TokenizerConfig withTitleWord(boolean enabled) {
		return new TokenizerConfig(enabled, filterStopWords, convertToSimplifiedChinese);
	}

	public TokenizerConfig withFilterStopWords(boolean enabled) {
		return new TokenizerConfig(titleWord, enabled, convertToSimplifiedChinese);
	}

	public TokenizerConfig withConvertToSimplifiedChinese(boolean enabled) {
		return new TokenizerConfig(titleWord, filterStopWords, enabled);
	}
}
