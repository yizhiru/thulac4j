package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.base.SegItem;
import io.github.yizhiru.thulac4j.dat.Dat;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author jyzheng
 */
public class Cementer implements Serializable {
  private Dat dat;
  private String pos; // 词性

  public Cementer(String path, String pos) throws FileNotFoundException {
    dat = Dat.loadDat(path);
    this.pos = pos;
  }

  public Cementer(InputStream in, String pos) {
    dat = Dat.loadDat(in);
    this.pos = pos;
  }

  public Cementer() {
  }

  /**
   * 黏结词
   *
   * @param segmented 分词后结果
   */
  public void cement(List<String> segmented) {
    int index, j;
    for (int i = 0; i < segmented.size(); i++) {
      index = -dat.hasMatched(0, segmented.get(i));
      if (index <= 0) continue;
      StringBuilder builder = new StringBuilder(segmented.get(i));
      for (j = i + 1; j < segmented.size(); j++) {
        index = -dat.hasMatched(index, segmented.get(j));
        if (index <= 0) break;
        builder.append(segmented.get(j));
      }
      if (builder.length() > segmented.get(i).length()) {
        segmented.set(i, builder.toString());
        for (j = j - 1; j > i; j--)
          segmented.remove(j);
      }
    }
  }

  // 黏结POS后的分词结果
  public void cementPos(List<SegItem> segmented) {
    int index, j;
    for (int i = 0; i < segmented.size(); i++) {
      index = -dat.hasMatched(0, segmented.get(i).word);
      if (index <= 0) continue;
      StringBuilder builder = new StringBuilder(segmented.get(i).word);
      for (j = i + 1; j < segmented.size(); j++) {
        index = -dat.hasMatched(index, segmented.get(j).word);
        if (index <= 0) break;
        builder.append(segmented.get(j).word);
      }
      if (builder.length() > segmented.get(i).word.length()) {
        segmented.set(i, new SegItem(builder.toString(), pos));
        for (j = j - 1; j > i; j--)
          segmented.remove(j);
      }
    }
  }
}
