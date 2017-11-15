package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.base.CwsModel;
import io.github.yizhiru.thulac4j.base.NGramFeature;
import io.github.yizhiru.thulac4j.base.POCS;
import io.github.yizhiru.thulac4j.base.Util;
import io.github.yizhiru.thulac4j.dat.Dat;
import io.github.yizhiru.thulac4j.dat.DatMaker;
import io.github.yizhiru.thulac4j.process.Cementer;
import io.github.yizhiru.thulac4j.process.Decoder;
import io.github.yizhiru.thulac4j.process.Ruler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author jyzheng
 */
public abstract class Segmenter<T> {
  protected CwsModel model;
  protected int[][] labelTrans;
  protected List<Cementer> uws;



  protected void setPreTrans() {
    labelTrans = Util.labelPreTransitions(model.labelValues);
  }


  /**
   * 处理序列标注得到分词结果
   *
   * @param cleaned 清洗后的字符串
   * @return 词与词性的数组
   */
  abstract List<T> getResult(String cleaned, int[] labels);


  public List<T> segment(String sentence) {
    if (sentence.length() == 0) return getResult(sentence, new int[0]);
    Ruler ruler = new Ruler(sentence.toCharArray());
    String cleaned = ruler.rulePoc();
    if(cleaned.length() == 0) return getResult(cleaned, new int[0]);
    NGramFeature nGram = new NGramFeature(model.featureDat);
    int[][] values = nGram.putValues(model, cleaned.toCharArray());
    int[] labels = Decoder.viterbi(model, cleaned.length(), ruler.pocss, values, labelTrans);
    return getResult(cleaned, labels);
  }

  @Deprecated
  public void setUserWordsPath(String path) throws IOException {
    Dat dat = DatMaker.make(path);
    uws = Collections.singletonList(new Cementer(dat, "uw"));
  }

  /**
   * Key: path; Value: pos
   */
  public void setUserWordsPath(Map<String, String> pathWithPos) throws IOException {
    for (Map.Entry<String, String> entry : pathWithPos.entrySet()) {
      addUserWordsPath(entry.getKey(), entry.getValue());
    }
  }

  public void addUserWordsPath(String path, String pos) throws IOException {
    if (uws == null) {
      uws = new ArrayList<>();
    }
    Dat dat = DatMaker.make(path);
    uws.add(new Cementer(dat, pos));
  }
}
