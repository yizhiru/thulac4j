package io.github.yizhiru.thulac4j.process;

import java.util.Arrays;
import java.util.HashSet;

import io.github.yizhiru.thulac4j.base.ALLowLabels;

/**
 * @author jyzheng
 */
public class Allower {

  // size = 58
  private static HashSet<Character> punctuations = new HashSet<>(Arrays.asList(
          '！', '、', '。', '★', '☆', '（', '）', '《', '》', '，', '【', '】', '—', '‘', '’',
          '：', '；', '“', '”', '？', '!', '"', '#', '$', '%', '…', '&', '\'', '(', ')', '*',
          '+', ',', '-', '.', '/', '·', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']',
          '^', '_', '◤', '￥', '{', '|', '}', '~', '～', ' ', '　'));

  private static HashSet<Character> numbers = new HashSet<>(Arrays.asList(
          '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
          '０', '１', '２', '３', '４', '５', '６', '７', '８', '９'));

  /**
   * 根据标点符号，给每个字符赋予allow label
   *
   * @param sentence 待分词句子
   * @return allow label数组
   */
  public static ALLowLabels[] ruleAllow(char[] sentence) {
    final int len = sentence.length;
    ALLowLabels[] allows = new ALLowLabels[len];
    if (len == 1) {
      allows[0] = ALLowLabels.S_POS;
      return allows;
    }
    boolean hasTitleBegin = false;
    int tileBegin = 0;
    allows[0] = ALLowLabels.BS_POS;
    for (int i = 1; i < len; ) {
      // 1. 为标点符号
      if (punctuations.contains(sentence[i])) {
        allows[i] = ALLowLabels.S_POS;
        if (allows[i - 1] == ALLowLabels.DEFAULT_POS)
          allows[i - 1] = ALLowLabels.ES_POS;
        // 处理书名号
        if (sentence[i] == '《') {
          hasTitleBegin = true;
          tileBegin = i;
        } else if (hasTitleBegin && sentence[i] == '》') {
          if (i - tileBegin == 2) allows[i - 1] = ALLowLabels.S_POS;
          else if (isPossibleTitle(tileBegin, i, sentence)) {
            allows[tileBegin + 1] = ALLowLabels.B_POS;
            allows[i - 1] = ALLowLabels.E_POS;
            for (int j = i - 2; j > tileBegin + 1; j--) {
              allows[j] = ALLowLabels.M_POS;
            }
          }
          hasTitleBegin = false;
        }
        if (i + 1 < sentence.length) {
          allows[i + 1] = ALLowLabels.BS_POS;
        }
        i += 2;
      } else {
        allows[i] = ALLowLabels.DEFAULT_POS;
        i++;
      }
    }
    if (allows[len - 1] == ALLowLabels.DEFAULT_POS)
      allows[len - 1] = ALLowLabels.ES_POS;
    return allows;
  }

  /**
   * 判断前后书名号内的字符串是否为能成词
   *
   * @param beginIndex 前书名号《 index
   * @param endIndex   后书名号》index
   * @param sentence   输入句子
   * @return 若能则true
   */
  private static boolean isPossibleTitle(int beginIndex, int endIndex, char[] sentence) {
    if (endIndex - beginIndex > 10 && endIndex - beginIndex < 3)
      return false;
    for (int i = beginIndex + 1; i < endIndex; i++) {
      if (punctuations.contains(sentence[i]))
        return false;
    }
    return true;
  }
}
