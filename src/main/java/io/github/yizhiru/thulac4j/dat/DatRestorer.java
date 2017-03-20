package io.github.yizhiru.thulac4j.dat;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jyzheng
 */
public class DatRestorer {

  /**
   * restore DAT
   *
   * @param dat DAT
   */
  public static List<String> restore(Dat dat) throws IOException {
    String word;
    LinkedList<String> list = new LinkedList<>();
    for (int i = 0; i < dat.entries.size(); i++) {
      if (dat.entries.get(i).check >= 0) {
        word = restoreWord(dat, i);
        if (dat.isWordMatched(word)) {
          list.add(word);
        }
      }
    }
    return list;
  }

  /**
   * restore word by its last index
   *
   * @param dat   Double Array Trie
   * @param index the last index of word, i.e. its check >= 0
   * @return word
   */
  private static String restoreWord(Dat dat, int index) {
    List<Dat.Entry> entries = dat.entries;
    int r, s = index;
    StringBuilder sb = new StringBuilder();
    while (s > 0 && s < entries.size()) {
      r = entries.get(s).check;
      if (r == s || entries.get(r).base >= s)
        break;
      sb.insert(0, (char) (s - entries.get(r).base));
      s = r;
    }
    return sb.toString();
  }
}
