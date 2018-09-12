package io.github.yizhiru.thulac4j.util;


import io.github.yizhiru.thulac4j.term.CharType;
import org.junit.Test;

import static io.github.yizhiru.thulac4j.util.CharUtils.getCharType;
import static org.junit.Assert.assertSame;

public class CharUtilsTest {

	@Test
	public void checkCharType() {
		char[] singlePunctuations = new char[]{
				'，', '。', '？', '！', '：', '；', '‘', '’', '“', '”', '【', '】', '、',
				'《', '》', '@', '#', '（', '）', '"', '[', ']', '~', ':', '?', '◤',
				'☆', '★', '…', '\'', '!', '*', '+', '>', '(', ')', ';', '=',
				'℃', '℉',
		};
		for (char c : singlePunctuations) {
			assertSame(CharType.SINGLE_PUNCTUATION_CHAR, getCharType(c));
		}

		char[] exSinglePunctuations = new char[]{
				'·', '—', '￥', '$', '&', '\\', '^', '_', '{', '|', '}'
		};
		for (char c : exSinglePunctuations) {
			assertSame(CharType.EX_SINGLE_PUNCTUATION_CHAR, getCharType(c));
		}

		char[] chineseNumeralChars = new char[]{
				'〇', '一', '二', '三', '四', '五', '六', '七', '八', '九'
		};
		for (char c : chineseNumeralChars) {
			assertSame(CharType.CHINESE_NUMERAL_CHAR, getCharType(c));
		}

		char[] arabicNumeralChars = new char[]{
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'０', '１', '２', '３', '４', '５', '６', '７', '８', '９'
		};
		for (char c : arabicNumeralChars) {
			assertSame(CharType.ARABIC_NUMERAL_CHAR, getCharType(c));
		}

		// numeral punctuations
		char[] numeralPunctuationChars = new char[]{
				'%', '.', ',', '/', '％', '-', '±', '‰',
		};
		for (char c : numeralPunctuationChars) {
			assertSame(CharType.NUMERAL_PUNCTUATION_CHAR, getCharType(c));
		}

		char[] hanChars = new char[]{
				'苟', '利', '国', '家', '生', '死', '以',
				'豈', '因', '禍', '福', '避', '趨', '之',
		};
		for (char c : hanChars) {
			assertSame(CharType.HAN_ZI_CHAR, getCharType(c));
		}

		char[] englishLetterChars = new char[]{
				'a', 'b', 'c', 'd', 'h', 'l', 'o', 'r', 'u', 'z',
				'A', 'B', 'C', 'D', 'H', 'L', 'O', 'R', 'U', 'Z'
		};
		for (char c : englishLetterChars) {
			assertSame(CharType.ENGLISH_LETTER_CHAR, getCharType(c));
		}

		char[] otherChars = new char[]{
				'＆',
		};
		for (char c : otherChars) {
			assertSame(CharType.OTHER_CHAR, getCharType(c));
		}
	}
}
