package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.util.CharUtils;
import io.github.yizhiru.thulac4j.term.SegItem;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 黏结阿拉伯数字与时间量词.
 */
public class WordCementer {

    private static final Set<Character> TIME_UNITS = new HashSet<>(
            Arrays.asList('年', '月', '日', '号', '时', '点', '分', '秒'));

    /**
     * 黏结阿拉伯数字与时间量词.
     * @param segItems 分词中间结果
     */
    public static void cementTimeWord(List<SegItem> segItems) {
        for (int i = segItems.size() - 1; i > 0; i--) {
            String word = segItems.get(i).word;
            if (word.length() == 1 && TIME_UNITS.contains(word.charAt(0))) {
                SegItem segItem = segItems.get(i - 1);
                if (isNumberWord(segItem.word)) {
                    segItems.set(i - 1,
                            new SegItem(segItem.word + segItems.remove(i).word, "t")
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
            if (!CharUtils.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }
}
