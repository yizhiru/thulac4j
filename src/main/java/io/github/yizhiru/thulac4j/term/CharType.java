package io.github.yizhiru.thulac4j.term;

import java.util.HashMap;
import java.util.Map;

public enum CharType {

    /**
     * 只能单独成词的标点符号，不会与其他字符合组合成词
     */
    SINGLE_PUNCTUATION_CHAR("p"),

    /**
     * 既能单独成词又能与其他字符组合成词的标点符号
     */
    EX_SINGLE_PUNCTUATION_CHAR("ep"),

    /**
     * 空格或控制字符串
     */
    SPACE_OR_CONTROL_CHAR("c"),

    /**
     * 中文数字字符
     */
    CHINESE_NUMERAL_CHAR("cn"),

    /**
     * 阿拉伯数字字符
     */
    ARABIC_NUMERAL_CHAR("an"),

    /**
     * 数词专用标点符合
     */
    NUMERAL_PUNCTUATION_CHAR("np"),

    /**
     * 汉字字符
     */
    HAN_ZI_CHAR("h"),

    /**
     * 英文字符
     */
    ENGLISH_LETTER_CHAR("e"),

    /**
     * 其他字符
     */
    OTHER_CHAR("o"),
    ;

    /**
     * 简写
     */
    private final String abbreviation;

    /**
     * 简写与CharType之间的映射
     */
    private static final Map<String, CharType> MAP = new HashMap<>(values().length, 1);

    // 静态初始化
    static {
        for (CharType t : values()) {
            MAP.put(t.abbreviation, t);
        }
    }

    CharType(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * 根据CharType的简写得到枚举值
     *
     * @param abbr 简写
     * @return 具体枚举值，若没有则抛出 IllegalArgumentException
     */
    public static CharType of(String abbr) {
        CharType type = MAP.get(abbr);
        if (type == null) {
            throw new IllegalArgumentException("Invalid char type abbreviation: " + abbr);
        }
        return type;
    }
}
