package io.github.yizhiru.thulac4j.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class CharUtils {

    /**
     * 标点符合集合
     */
    private final static Set<Character> SINGLE_PUNCTUATIONS = new HashSet<>(Arrays.asList(
            '，', '。', '？', '！', '：', '；', '‘', '’', '“', '”', '【', '】', '、',
            '《', '》', '@', '#', '（', '）', '"', '[', ']', '~', '/', ',', ':', '?', '◤',
            '☆', '★', '…', '\'', '!', '*', '+', '>', '(', ')', ';', '=')
    );

    /**
     * 除了SINGLE_PUNCTUATIONS以外的标点符号.
     */
    private final static Set<Character> REMAIN_PUNCTUATIONS = new HashSet<>(Arrays.asList(
            '·', '—', '￥', '$', '%', '&', '-', '.', '\\', '^', '_', '{', '|', '}')
    );

    private final static Character LATIN_SPACE = ' ';

    private final static Set<Character> CHINESE_DIGITS = new HashSet<>(Arrays.asList(
            '〇', '一', '二', '三', '四', '五', '六', '七', '八', '九')
    );

    /**
     * 全角或半角数字.
     */
    private final static Set<Character> DIGITS = new HashSet<>(Arrays.asList(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '０', '１', '２', '３', '４', '５', '６', '７', '８', '９')
    );

    /**
     * 数词专用标点符合集合
     */
    private final static Set<Character> DIGIT_PUNCTUATIONS = new HashSet<>(Arrays.asList(
            '%', '.', ',', '/', '％', '-')
    );

    /**
     * 是否为单独成词的标点符号.
     */
    public static boolean isSinglePunctuation(char ch) {
        return SINGLE_PUNCTUATIONS.contains(ch);
    }

    public static boolean isRemainPunctuation(char ch) {
        return REMAIN_PUNCTUATIONS.contains(ch);
    }

    /**
     * 是否为控制字符或空格字符，在分词过程中需要略过这样的字符.
     *
     * @param ch 字符
     * @return 布尔值，若是则返回true
     */
    public static boolean isSkipped(char ch) {
        return (ch < LATIN_SPACE) || Character.isSpaceChar(ch);
    }

    /**
     * 是否为字母
     *
     * @param ch 字符
     * @return 布尔值，若是则返回true
     */
    public static boolean isLetter(char ch) {
        return Character.getType(ch) == Character.LOWERCASE_LETTER
                || Character.getType(ch) == Character.UPPERCASE_LETTER;
    }

    public static boolean mayBeLetterWord(char ch) {
        if (isSkipped(ch) || isSinglePunctuation(ch) || isHan(ch)) {
            return false;
        }
        return true;
    }

    /**
     * 是否为数字（全角或半角）
     *
     * @param ch 字符
     * @return 布尔值，若是则返回true
     */
    public static boolean isDigit(char ch) {
        return DIGITS.contains(ch);
    }

    /**
     * 是否为数字或数字标点符号.
     *
     * @param ch 字符
     * @return 布尔值
     */
    public static boolean mayBeNumeral(char ch) {
        if (isSkipped(ch) || isSinglePunctuation(ch) || isHan(ch)) {
            return false;
        }
        return isDigit(ch) || isChineseDigit(ch)
                || DIGIT_PUNCTUATIONS.contains(ch);
    }

    /**
     * 是否为中文数字.
     *
     * @param ch 字符
     * @return 布尔值
     */
    public static boolean isChineseDigit(char ch) {
        return CHINESE_DIGITS.contains(ch);
    }

    public static boolean isHan(char c) {
        return Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN;
    }

    /**
     * Convert half-width character to full-width character.
     * 半角空格为32, 全角空格为12288;
     * 其他半角字符(33-126)与全角字符(65281-65374)均相差 65248.
     *
     * @param ch 字符
     * @return 半角转成的全角字符
     */
    public static char convertHalfWidth(char ch) {
        if (ch == 32) {
            return (char) 12288;
        } else if (ch > 32 && ch < 127) {
            return (char) (ch + 65248);
        }
        return ch;
    }
}
