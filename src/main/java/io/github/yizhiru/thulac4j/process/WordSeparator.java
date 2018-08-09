package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.term.SegItem;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用于拆词, 在分词实现中未使用.
 */
public class WordSeparator {

    public static final Set<String> NEGATIVE_WORDS = new HashSet<>(
            Arrays.asList("不会", "不住", "不到", "不想", "不敢", "不是", "不能", "也是"));

    /**
     * 将否定词拆成两个词
     *
     * @param segItems 中间序列标注结果
     */
    public static void separate(List<SegItem> segItems) {
        for (int i = 0; i < segItems.size(); i++) {
            String word = segItems.get(i).word;
            if (NEGATIVE_WORDS.contains(word)) {
                segItems.set(i, new SegItem(word.substring(0, 1), "v"));
                segItems.add(i + 1, new SegItem(word.substring(1), "d"));
            }
        }
    }
}
