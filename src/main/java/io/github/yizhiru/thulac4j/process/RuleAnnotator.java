package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.term.AnnotatedTerms;
import io.github.yizhiru.thulac4j.term.CharType;
import io.github.yizhiru.thulac4j.util.CharUtils;
import io.github.yizhiru.thulac4j.term.POC;

import java.util.function.Predicate;

import static io.github.yizhiru.thulac4j.util.CharUtils.*;


/**
 * 借助标点符号、数字等信息，提前标注字符的可能label.
 */
public final class RuleAnnotator {

	/**
	 * 依据标点符号、数字等规则，标注部分字符的POC
	 *
	 * @param text 待分词文本
	 * @return 清洗后String
	 */
	public static AnnotatedTerms annotate(String text, boolean isEnableTileWord) {
		int len = text.length();
		AnnotatedTerms annotatedTerms = new AnnotatedTerms(text.toCharArray());
		boolean hasTitleBegin = false;
		int titleBegin = 0;
		for (int i = 0; i < len; ) {
			CharType charType = annotatedTerms.getCharTypeByIndex(i);
			// 1. Space or control character
			if (charType == CharType.SPACE_OR_CONTROL_CHAR) {
				annotatedTerms.intersectLastPoc(POC.END_OR_SINGLE_POC);
				// 连续忽略
				for (i++; i < len; i++) {
					if (annotatedTerms.getCharTypeByIndex(i) != CharType.SPACE_OR_CONTROL_CHAR) {
						break;
					}
				}
				// 处理后面字符
				if (i < len) {
					annotatedTerms.appendAhead(i, POC.BEGIN_OR_SINGLE_POC);
				}
			}
			// 2. 标点符号
			else if (charType == CharType.SINGLE_PUNCTUATION_CHAR) {
				annotatedTerms.intersectLastPoc(POC.END_OR_SINGLE_POC);
				annotatedTerms.append(i, POC.PUNCTUATION_POC);
				if (isEnableTileWord) {
					// 前书名号
					char ch = annotatedTerms.getRawCharByIndex(i);
					if (ch == LEFT_TITLE_QUOTATION_CHAR) {
						hasTitleBegin = true;
						titleBegin = i;
					}
					// 后书名号
					else if (hasTitleBegin && ch == RIGHT_TITLE_QUOTATION_CHAR) {
						if (isPossibleTitle(annotatedTerms, titleBegin + 1, i - 1)) {
							setTitleWordPoc(annotatedTerms,
									titleBegin + 1,
									i - 1,
									annotatedTerms.getAnnotatedLength() - 2);
						}
						hasTitleBegin = false;
					}
				}
				i++;
				// 处理后面字符
				if (i < len && annotatedTerms.getCharTypeByIndex(i) != CharType.SPACE_OR_CONTROL_CHAR) {
					annotatedTerms.appendAhead(i, POC.BEGIN_OR_SINGLE_POC);
				}
			}
			// 3. 英文字母
			else if (charType == CharType.ENGLISH_LETTER_CHAR) {
				i = processWord(annotatedTerms,
						i,
						RuleAnnotator::isPartOfLetterWord,
						false);
			}
			// 4. Numbers
			else if (charType == CharType.ARABIC_NUMERAL_CHAR) {
				i = processWord(annotatedTerms,
						i,
						RuleAnnotator::isPartOfNumeral,
						true);
			}
			// 5. 以上条件均不满足的标点符号单独成词
			else if (charType == CharType.EX_SINGLE_PUNCTUATION_CHAR
					|| charType == CharType.NUMERAL_PUNCTUATION_CHAR) {
				setCurrentAsSingle(i, annotatedTerms, POC.PUNCTUATION_POC);
				i++;
			}
			// 6. 汉字字符
			else if (charType == CharType.HAN_ZI_CHAR
					|| charType == CharType.CHINESE_NUMERAL_CHAR) {
				annotatedTerms.append(i, POC.DEFAULT_POC);
				i++;
			}
			// 7. 其他字符
			else {
				setCurrentAsSingle(i, annotatedTerms, POC.SINGLE_POC);
				i++;
			}
		}
		annotatedTerms.intersectPocByIndex(0, POC.BEGIN_OR_SINGLE_POC);
		annotatedTerms.intersectLastPoc(POC.END_OR_SINGLE_POC);
		return annotatedTerms;
	}

	/**
	 * 当前字符单独成词，设置前一、当前、后一字符的POC.
	 *
	 * @param currentRawIndex 当前原字符串索引位置
	 * @param annotatedTerms  标注结果
	 * @param currentPoc      当前字符对应的POC
	 */
	private static void setCurrentAsSingle(int currentRawIndex,
	                                       AnnotatedTerms annotatedTerms,
	                                       POC currentPoc) {
		annotatedTerms.intersectLastPoc(POC.END_OR_SINGLE_POC);
		annotatedTerms.append(currentRawIndex, currentPoc);
		int nextIndex = currentRawIndex + 1;
		if (nextIndex < annotatedTerms.getRawCharsLength()
				&& annotatedTerms.getCharTypeByIndex(nextIndex) != CharType.SPACE_OR_CONTROL_CHAR) {
			annotatedTerms.appendAhead(nextIndex, POC.BEGIN_OR_SINGLE_POC);
		}
	}

	/**
	 * 判断前后书名号内的字符串是否为能成词
	 *
	 * @param annotatedTerms 标注结果
	 * @param startIndex     前书名号《 后一个index
	 * @param endIndex       后书名号》前一个index
	 * @return 若能则true
	 */
	private static boolean isPossibleTitle(AnnotatedTerms annotatedTerms, int startIndex, int endIndex) {
		if (endIndex - startIndex > 8 || endIndex - startIndex <= 0) {
			return false;
		}
		for (int i = startIndex; i <= endIndex; i++) {
			CharType charType = annotatedTerms.getCharTypeByIndex(i);
			if (charType == CharType.SINGLE_PUNCTUATION_CHAR
					|| charType == CharType.SPACE_OR_CONTROL_CHAR) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 设置书名号内为一个词.
	 *
	 * @param annotatedTerms    清洗句子结果
	 * @param startRawIndex     词的起始原字符串索引位置（在待分词文本中的索引值）
	 * @param endRawIndex       词的结束原字符串索引位置（在待分词文本中的索引值）
	 * @param endAnnotatedIndex 词的结束标注索引位置
	 */
	private static void setTitleWordPoc(
			AnnotatedTerms annotatedTerms,
			int startRawIndex,
			int endRawIndex,
			int endAnnotatedIndex) {
		// 单独字符成词
		if (startRawIndex == endRawIndex) {
			annotatedTerms.intersectPocByIndex(endAnnotatedIndex, POC.SINGLE_POC);
			return;
		}
		// 对应起始标注索引位置
		int startAnnotatedIndex = endAnnotatedIndex - endRawIndex + startRawIndex;
		annotatedTerms.setPocByIndex(startAnnotatedIndex, POC.BEGIN_POC);
		for (int i = startAnnotatedIndex + 1; i < endAnnotatedIndex; i++) {
			annotatedTerms.setPocByIndex(i, POC.MIDDLE_POC);
		}
		annotatedTerms.setPocByIndex(endAnnotatedIndex, POC.END_POC);
	}

	/**
	 * 英文可与数字联合成词
	 *
	 * @param charType 字符类型
	 * @return 布尔值
	 */
	public static boolean isPartOfLetterWord(CharType charType) {
		return charType == CharType.ENGLISH_LETTER_CHAR
				|| charType == CharType.ARABIC_NUMERAL_CHAR
				|| charType == CharType.EX_SINGLE_PUNCTUATION_CHAR;
	}


	/**
	 * 为数词的一部分，数字字符或可与数字搭配的标点符号.
	 *
	 * @param charType 字符类型
	 * @return 布尔值
	 */
	public static boolean isPartOfNumeral(CharType charType) {
		return charType == CharType.CHINESE_NUMERAL_CHAR
				|| charType == CharType.ARABIC_NUMERAL_CHAR
				|| charType == CharType.NUMERAL_PUNCTUATION_CHAR;
	}

	/**
	 * 处理单词或连续数字
	 *
	 * @param annotatedTerms 规则标注结果
	 * @param startRawIndex  在字符串raw中的起始位置
	 * @param condition      函数式接口，判断是否为字母或数字
	 * @param isNumeral      单词or数字
	 * @return 词结束后的下一个字符所处位置
	 */
	private static int processWord(
			AnnotatedTerms annotatedTerms,
			int startRawIndex,
			Predicate<CharType> condition,
			boolean isNumeral) {
		POC b, m, e, s;
		if (isNumeral) {
			b = POC.BEGIN_NUMERAL_POC;
			m = POC.MIDDLE_NUMERAL_POC;
			e = POC.END_NUMERAL_POC;
			s = POC.SINGLE_NUMERAL_POC;
		} else {
			b = POC.BEGIN_POC;
			m = POC.MIDDLE_POC;
			e = POC.END_POC;
			s = POC.SINGLE_POC;
		}

		// 处理前一字符
		annotatedTerms.intersectLastPoc(POC.END_OR_SINGLE_POC);

		int len = annotatedTerms.getRawCharsLength();
		int i = startRawIndex;
		i++;
		// 单独成词
		if (i == len
				|| (i < len && !condition.test(annotatedTerms.getCharTypeByIndex(i)))) {
			annotatedTerms.append(i - 1, s);
		}
		// 连续成词
		else {
			annotatedTerms.append(i - 1, b);
			for (; i + 1 < len && condition.test(annotatedTerms.getCharTypeByIndex(i + 1)); i++) {
				annotatedTerms.append(i, m);
			}
			annotatedTerms.append(i, e);
			i++;
		}
		// 处理成词后的下一字符
		if (i < len && annotatedTerms.getCharTypeByIndex(i) != CharType.SPACE_OR_CONTROL_CHAR) {
			annotatedTerms.appendAhead(i, POC.BEGIN_OR_SINGLE_POC);
		}
		return i;
	}
}

