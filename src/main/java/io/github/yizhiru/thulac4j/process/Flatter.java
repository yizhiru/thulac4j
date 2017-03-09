package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.base.SegItem;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用于拆词, 因在工程项目中可不用到, 故未分词中未使用
 */
public class Flatter {

  public static final Set<String> negWords = new HashSet<>(Arrays.asList(
          "不会", "不住", "不到", "不想", "不敢", "不是", "不能", "也是"));

  public static void flat(List<String> segmented) {
    String word;
    for (int i = 0; i < segmented.size(); i++) {
      word = segmented.get(i);
      if (negWords.contains(word)) {
        segmented.set(i, word.substring(0, 1));
        segmented.add(i + 1, word.substring(1));
      }
    }
  }

  public static void flatPos(List<SegItem> segmented) {
    String word;
    for (int i = 0; i < segmented.size(); i++) {
      word = segmented.get(i).word;
      if (negWords.contains(word)) {
        segmented.set(i, new SegItem(word.substring(0, 1), "v"));
        segmented.add(i + 1, new SegItem(word.substring(1), "d"));
      }
    }
  }

}
