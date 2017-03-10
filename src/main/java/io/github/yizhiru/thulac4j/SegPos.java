package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.base.CwsModel;
import io.github.yizhiru.thulac4j.base.SegItem;
import io.github.yizhiru.thulac4j.base.Util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jyzheng
 */
public class SegPos extends Segmenter {

  public SegPos(String modelPath) throws FileNotFoundException {
    model = CwsModel.loadModel(modelPath);
    setPreTrans();
  }

  public SegPos(InputStream in) {
    model = CwsModel.loadModel(in);
    setPreTrans();
  }


  @Override
  public List<SegItem> getResult(String sentence, int[] labels) {
    List<SegItem> result = new ArrayList<>();
    if (labels == null || labels.length == 0) return result;
    int len = sentence.length();
    char poc;
    String word, label;
    for (int i = 0, offset = 0; i < len; i++) {
      label = model.labelValues[labels[i]];
      poc = label.charAt(0);
      if (poc == Util.POC_E || poc == Util.POC_S) {
        word = sentence.substring(offset, i + 1);
        label = label.substring(1);
        result.add(new SegItem(word, label));
        offset = i + 1;
      }
    }
    model.ns.cementPos(result);
    model.idiom.cementPos(result);
    if (uw != null) uw.cementPos(result);
    return result;
  }
}
