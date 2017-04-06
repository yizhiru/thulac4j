package io.github.yizhiru.thulac4j.dat;

import java.io.*;
import java.util.*;

/**
 * @author jyzheng
 */
public class DatMaker extends Dat {
  private int available;

  public DatMaker() {
    entries = new ArrayList<>(Collections.singletonList(new Entry(0, -1)));
    available = 0;
  }

  // DAT 扩张2倍
  private void expand() {
    int oldSize = entries.size();
    for (int i = 0; i < oldSize; i++) {
      entries.add(new Dat.Entry(-1, -1));
    }
  }

  // remove useless entry
  private void shrink() {
    for (int i = entries.size() - 1; i >= 0; i--) {
      if (entries.get(i).check == -1)
        entries.remove(i);
      else break;
    }
  }

  /**
   * 找到满足条件的baseIndex
   *
   * @param children 前缀的后一字符集合
   * @return baseIndex
   */
  private int findBaseIndex(List<Integer> children) {
    int cSize = children.size();
    for (int bi = available; ; bi++) {
      if (bi == entries.size()) expand();
      if (cSize > 0) {
        while (bi + children.get(cSize - 1) >= entries.size())
          expand();
      }
      // baseIndex应满足条件：
      // 1. 未被使用
      // 2. 满足所有children跳转到的node也未被使用
      if (entries.get(bi).check >= 0) continue;
      boolean isValid = true;
      for (Integer c : children) {
        if (entries.get(bi + c).check >= 0) {
          isValid = false;
          break;
        }
      }
      if (isValid) {
        return bi;
      }
    }
  }

  /**
   * 插入到Trie树
   *
   * @param preIndex 前缀对应的index
   * @param children 前缀的后一字符集合
   * @param isWord   前缀是否为词
   */
  private void insert(int preIndex, List<Integer> children, boolean isWord) {
    int bi = findBaseIndex(children);
    entries.get(preIndex).base = bi;
    if (isWord) {
      entries.get(bi).check = preIndex;
      available = bi + 1;
    }
    for (int c : children) {
      entries.get(bi + c).base = 0;
      entries.get(bi + c).check = preIndex;
    }
  }

  /**
   * 给定前缀生成后一字符集合
   *
   * @param lexicon 词典
   * @param start   词典开始时的索引位置
   * @param prefix  前缀
   * @return 后一字符集合
   */
  private List<Integer> genChildren(List<String> lexicon, int start, String prefix) {
    List<Integer> children = new LinkedList<>();
    int preLen = prefix.length();
    for (int i = start; i < lexicon.size(); i++) {
      String word = lexicon.get(i);
      if (word.length() < preLen) return children;
      if (!word.substring(0, preLen).equals(prefix)) return children;
      if (word.length() > preLen && (children.isEmpty() ||
              ((int) word.charAt(preLen)) != children.get(children.size() - 1)))
        children.add((int) word.charAt(preLen));
    }
    return children;
  }

  /**
   * 构建DAT
   *
   * @param lexicon 词典
   * @return 词典对应的DAT
   */
  public static Dat build(List<String> lexicon) {
    DatMaker maker = new DatMaker();
    lexicon.sort(String::compareTo);
    String word, prefix;
    int preIndex;
    for (int i = 0; i < lexicon.size(); i++) {
      word = lexicon.get(i);
      int matched = maker.hasMatched(word);
      matched = matched < 0 ? word.length() : matched;
      for (int j = matched; j <= word.length(); j++) {
        prefix = word.substring(0, j);
        preIndex = -maker.hasMatched(prefix);
        List<Integer> children = maker.genChildren(lexicon, i, prefix);
        maker.insert(preIndex, children, j == word.length());
      }
      matched = -maker.hasMatched(word);
      maker.entries.get(maker.entries.get(matched).base).base = i;
    }
    maker.shrink();
    return new Dat(maker.entries);
  }

  public static Dat make(String path) throws IOException {
    return make(new FileInputStream(path));
  }


  public static Dat make(InputStream in) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String line;
    List<String> lexicon = new ArrayList<>();
    while ((line = br.readLine()) != null) {
      line = line.trim();
      if (line.length() == 0) continue;
      lexicon.add(line);
    }
    return build(lexicon);
  }
}
