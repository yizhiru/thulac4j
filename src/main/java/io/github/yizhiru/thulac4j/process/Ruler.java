package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.base.POCS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author jyzheng
 */
public class Ruler {

  // size = 58
  private final static HashSet<Character> Punctuations = new HashSet<>(Arrays.asList(
          '！', '、', '。', '★', '☆', '（', '）', '《', '》', '，', '【', '】', '—', '‘', '’',
          '：', '；', '“', '”', '？', '!', '"', '#', '$', '%', '…', '&', '\'', '(', ')', '*',
          '+', ',', '-', '.', '/', '·', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']',
          '^', '_', '◤', '￥', '{', '|', '}', '~', '～'));

  //  private final static HashSet<Character> Spaces = new HashSet<>(Arrays.asList(' ', '　'));
  private final static Character EngSpace = ' ';

  private final static HashSet<Character> Numbers = new HashSet<>(Arrays.asList(
          '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
          '０', '１', '２', '３', '４', '５', '６', '７', '８', '９'));
  private final static HashSet<Character> NumPuns = new HashSet<>(Arrays.asList(
          '%', '.', ',', '/', '％'));


  private static boolean isSkipped(char ch) {
    return (ch < EngSpace) || Character.isSpaceChar(ch);
  }

  private static boolean isLetter(char ch) {
    return Character.getType(ch) == Character.LOWERCASE_LETTER ||
            Character.getType(ch) == Character.UPPERCASE_LETTER;
  }

  private static boolean isNumber(char ch) {
    return Numbers.contains(ch) || NumPuns.contains(ch);
  }

  private char[] raw;
  public List<POCS> pocss;

  public Ruler(char[] raw) {
    this.raw = raw;
    pocss = new ArrayList<>();
  }

  /**
   * 根据标点符号，给每个字符赋予对应的POCS
   *
   * @return 清洗后String
   */
  public String rulePoc() {
    int len = raw.length;
    StringBuilder builder = new StringBuilder(len);
    boolean hasTitleBegin = false, existed;
    int titleBegin = 0;
    for (int i = 0; i < len; ) {
      // 1. space or control character
      if (isSkipped(raw[i])) {
        setPocs(pocss.size() - 1, POCS.ES_POCS);
        for (i++; i < len && isSkipped(raw[i]); i++) {
        }
        if (i < len) {
          pocss.add(POCS.BS_POCS);
        }
      } else {
        existed = (pocss.size() == builder.length() + 1);
        // 2. punctuation
        if (Punctuations.contains(raw[i])) {
          setCurrPos(existed, POCS.PUN_POCS);
          setPocs(pocss.size() - 2, POCS.ES_POCS);
          builder.append(raw[i]);
          // 处理书名号
          if (raw[i] == '《') {
            hasTitleBegin = true;
            titleBegin = i;
          } else if (hasTitleBegin && raw[i] == '》') {
            if (isPossibleTitle(raw, titleBegin + 1, i - 1)) {
              setWordPos(titleBegin + 1, i - 1, pocss.size() - 2);
            }
            hasTitleBegin = false;
          }
          for (i++; i < len && Punctuations.contains(raw[i]); i++) {
            pocss.add(POCS.PUN_POCS);
            builder.append(raw[i]);
          }
          if (i < len && !isSkipped(raw[i])) {
            pocss.add(POCS.BS_POCS);
          }
        } // 3. English or Latin words
        else if (isLetter(raw[i])) {
          i = wordProcess(existed, i, builder, Ruler::isLetter, false);
        } // 4. Numbers
        else if (Numbers.contains(raw[i])) {
          i = wordProcess(existed, i, builder, Ruler::isNumber, true);
        } else {
          setCurrPos(existed, POCS.DEFAULT_POCS);
          builder.append(raw[i]);
          i++;
        }
      }
    }
    setPocs(0, POCS.BS_POCS);
    setPocs(pocss.size() - 1, POCS.ES_POCS);
    return builder.toString();
  }

  /**
   * 判断前后书名号内的字符串是否为能成词
   *
   * @param raw   待分词句子
   * @param start 前书名号《 后一个index
   * @param end   后书名号》前一个index
   * @return 若能则true
   */
  private static boolean isPossibleTitle(char[] raw, int start, int end) {
    if (end - start > 8 || end - start <= 0)
      return false;
    for (int i = start; i <= end; i++) {
      if (Punctuations.contains(raw[i]) || isSkipped(raw[i])) return false;
    }
    return true;
  }

  /**
   * set word POCS
   *
   * @param start    the start index of raw sentence
   * @param end      the end index of raw sentence
   * @param endIndex the end index of poses
   */
  private void setWordPos(int start, int end, int endIndex) {
    if (start == end) {
      pocss.set(endIndex, POCS.S_POCS);
      return;
    }
    int startIndex = endIndex - end + start;
    pocss.set(startIndex, POCS.B_POCS);
    for (startIndex++; startIndex < endIndex; startIndex++) {
      pocss.set(startIndex, POCS.M_POCS);
    }
    pocss.set(endIndex, POCS.E_POCS);
  }


  /**
   * 对于pocss中处于index置pocs
   *
   * @param index 所处位置
   * @param pocs  POCS
   */
  private void setPocs(int index, POCS pocs) {
    if (index < 0 || index >= pocss.size()) return;
    POCS p = pocss.get(index);
    pocss.set(index, p.intersect(pocs));
  }

  /**
   * 设置当前位置的POCS
   *
   * @param existed 是否已存在
   * @param curr    POCS
   */
  private void setCurrPos(boolean existed, POCS curr) {
    if (existed) setPocs(pocss.size() - 1, curr);
    else pocss.add(curr);
  }

  /**
   * 处理单词或连续数字
   *
   * @param existed   是否已存在
   * @param start     在字符串raw中的起始位置
   * @param builder   StringBuilder
   * @param condition 函数式接口，判断是否为字母或数字
   * @param isNumeral 单词or数字
   * @return 词结束后的下一个字符所处位置
   */
  private int wordProcess(boolean existed, int start, StringBuilder builder,
                          Predicate<Character> condition, boolean isNumeral) {
    builder.append(Character.toLowerCase(raw[start]));
    POCS b, m, e, s;
    if (isNumeral) {
      b = POCS.B_M_POC;
      m = POCS.M_M_POC;
      e = POCS.E_M_POC;
      s = POCS.S_M_POC;
    } else {
      b = POCS.B_POCS;
      m = POCS.M_POCS;
      e = POCS.E_POCS;
      s = POCS.S_POCS;
    }
    int i = start;
    if (i + 1 == raw.length || (i + 1 < raw.length && !condition.test(raw[i + 1]))) {
      setCurrPos(existed, s);
      setPocs(pocss.size() - 2, POCS.ES_POCS);
      i++;
    } else {
      setCurrPos(existed, b);
      setPocs(pocss.size() - 2, POCS.ES_POCS);
      for (i++; i < raw.length && condition.test(raw[i]); i++) {
        pocss.add(m);
        builder.append(Character.toLowerCase(raw[i]));
      }
      pocss.set(pocss.size() - 1, e);
    }
    if (i < raw.length && !isSkipped(raw[i]))
      pocss.add(POCS.BS_POCS);
    return i;
  }

}
