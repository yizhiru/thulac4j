package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.base.CwsModel;
import io.github.yizhiru.thulac4j.base.Util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jyzheng
 */
public class SegOnly extends Segmenter {

  public SegOnly(String modelPath) throws FileNotFoundException {
    model = CwsModel.loadModel(modelPath);
    setPreTrans();
  }

  public SegOnly(InputStream in) {
    model = CwsModel.loadModel(in);
    setPreTrans();
  }


  @Override
  public List<String> getResult(String sentence, int[] labels) {
    List<String> result = new ArrayList<>();
    if (labels == null || labels.length == 0) return result;
    int len = sentence.length();
    char poc;
    String word, label;
    for (int i = 0, offset = 0; i < len; i++) {
      label = model.labelValues[labels[i]];
      poc = label.charAt(0);
      if (poc == Util.POC_E || poc == Util.POC_S) {
        word = sentence.substring(offset, i + 1);
        result.add(word);
        offset = i + 1;
      }
    }
    model.ns.cement(result);
    model.idiom.cement(result);
    if (uw != null) uw.cement(result);
    return result;
  }
}
