package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.term.TokenItem;
import io.github.yizhiru.thulac4j.util.CharUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 特定词的黏结
 */
public class SpecifiedWordCementer {

    private static final Set<Character> TIME_UNITS = new HashSet<>(
            Arrays.asList('年', '月', '日', '号', '时', '点', '分', '秒'));

    /**
     * 黏结阿拉伯数字与时间量词.
     *
     * @param tokenItems 分词中间结果
     */
    public static void cementTimeWord(List<TokenItem> tokenItems) {
        for (int i = tokenItems.size() - 1; i > 0; i--) {
            String word = tokenItems.get(i).word;
            if (word.length() == 1 && TIME_UNITS.contains(word.charAt(0))) {
                TokenItem tokenItem = tokenItems.get(i - 1);
                if (isNumberWord(tokenItem.word)) {
                    tokenItems.set(i - 1,
                            new TokenItem(tokenItem.word + tokenItems.remove(i).word, "t")
                    );
                    i--;
                }
            }
        }
    }

    /**
     * 一个词是否全为阿拉伯数字组成.
     *
     * @param word 词.
     * @return 布尔值
     */
    private static boolean isNumberWord(String word) {
        for (char ch : word.toCharArray()) {
            if (!CharUtils.isNumeralChar(ch)) {
                return false;
            }
        }
        return true;
    }
}
