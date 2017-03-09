package io.github.yizhiru.thulac4j.base;

import java.io.IOException;
import java.util.List;

/**
 * @author jyzheng
 */
public class ThulacModelTest {

  public static void main(String[] args) throws IOException {
//    ThulacModel thulac = new ThulacModel("train/cws_model.bin",
//            "train/cws_dat.bin",
//            "train/cws_label.txt");

    ThulacModel thulac = new ThulacModel("train/model_c_model.bin",
            "train/model_c_dat.bin",
            "train/model_c_label.txt");

    System.out.println(thulac.featureSize);
    System.out.println(thulac.labelSize);
    System.out.println(thulac.llWeights[0]);
    System.out.println(thulac.flWeights[0]);
    System.out.println(thulac.datSize);
    System.out.println(thulac.featDat[5]);
    thulac.serialize("models/seg_pos.bin");

//    String[] labelValues = thulac.labelValues;
//    List<List<Integer>> pocTags = thulac.pocTag();
//    int[] indices = {1, 2, 4, 8, 9, 12, 15};
//    for (int i = 0; i < 6; i++) {
//      System.out.println("index: " + i);
//      for(int index: pocTags.get(indices[i])) {
//        System.out.println(labelValues[index]);
//      }
//      System.out.println();
//    }
  }
}
