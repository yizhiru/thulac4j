package io.github.yizhiru.thulac4j.base;


import io.github.yizhiru.thulac4j.dat.Dat;

/**
 * @author jyzheng
 */
public class NGramFeature {
  private static final char BOUNDARY = '#'; // 超越边界的统一字符
  private static final char SPACE = ' '; // feature的一部分

  private Dat featDat;

  public NGramFeature(Dat featDat) {
    this.featDat = featDat;
  }


  /**
   * 寻找Unigram字符特征对应于DAT中的base
   *
   * @param ch   字符
   * @param mark 标识属于3种特征中的一种: '1', '2', '3'
   * @return 若存在则返回base，否则则返回-1
   */
  private int findUniFeat(char ch, char mark) {
    int index = process(ch);
    index = featDat.transition(index, SPACE);
    index = featDat.transition(index, mark);
    if (index == -1) return -1;
    return featDat.entries.get(index).base;
  }

  /**
   * 寻找Bigram字符特征对应于DAT中的base
   *
   * @param c1   第一个字符
   * @param c2   第二个字符
   * @param mark 标识属于4种特征中的一种: '1', '2', '3', '4'
   * @return 若存在则返回base，否则则返回-1
   */
  private int findBiFeat(char c1, char c2, char mark) {
    int index1 = process(c1);
    int index2 = process(c2);
    int index = featDat.transition(index1, index2);
    index = featDat.transition(index, SPACE);
    index = featDat.transition(index, mark);
    if (index == -1) return -1;
    return featDat.entries.get(index).base;
  }

  public int[][] putValues(CwsModel model, char[] sentence) {
    int len = sentence.length;
    int[][] values = new int[len][];
    char[] chs = new char[len + 4];
    System.arraycopy(sentence, 0, chs, 2, len);
    chs[0] = chs[1] = chs[chs.length - 2] = chs[chs.length - 1] = BOUNDARY;
    for (int i = 0; i < len; i++) {
      values[i] = putValue(model, chs[i], chs[i + 1], chs[i + 2], chs[i + 3], chs[i + 4]);
    }
    return values;
  }

  // 根据前后一起的五个字符，计算加权之和
  // 返回每个label对应的特征权值加权之和F(y_{t+1}=y,C)
  private int[] putValue(CwsModel model, char left2, char left1, char mid, char right1,
                         char right2) {
    int[] value = new int[model.labelSize];
    int base;
    if ((base = findUniFeat(mid, '1')) != -1) addValue(value, base, model);
    if ((base = findUniFeat(left1, '2')) != -1) addValue(value, base, model);
    if ((base = findUniFeat(right1, '3')) != -1) addValue(value, base, model);
    if ((base = findBiFeat(left1, mid, '1')) != -1) addValue(value, base, model);
    if ((base = findBiFeat(mid, right1, '2')) != -1) addValue(value, base, model);
    if ((base = findBiFeat(left2, left1, '3')) != -1) addValue(value, base, model);
    if ((base = findBiFeat(right1, right2, '4')) != -1) addValue(value, base, model);

    return value;
  }

  private void addValue(int[] value, int base, CwsModel model) {
    for (int i = 0; i < model.labelSize; i++) {
      value[i] += model.flWeights[base][i];
    }
  }

  private int process(char ch) {
    if (ch > 32 && ch < 128)
      return ch + 65248;
    return ch;
  }
}
