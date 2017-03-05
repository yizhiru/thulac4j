package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.base.CwsModel;
import io.github.yizhiru.thulac4j.base.Util;
import io.github.yizhiru.thulac4j.process.Flatter;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jyzheng
 */
public class Segmenter extends AbstractSegger {

  public Segmenter(String modelPath) throws FileNotFoundException {
    model = CwsModel.loadModel(modelPath);
    setUp();
  }

  public Segmenter(InputStream in) {
    model = CwsModel.loadModel(in);
    setUp();
  }


  @Override
  public List<String> segment(String sentence) {
    List<String> result = new ArrayList<>();
    int[] labels = sequenceLabel(sentence);
    if (labels == null) return result;
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
    ns.cement(result);
    idiom.cement(result);
    if (uw != null) uw.cement(result);
    Flatter.flat(result);
    return result;
  }
}
