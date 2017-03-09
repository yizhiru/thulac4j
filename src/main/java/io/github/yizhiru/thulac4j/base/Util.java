package io.github.yizhiru.thulac4j.base;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

/**
 * @author jyzheng
 */
public class Util {

  public static final Character POC_B = '0'; // begin
  public static final Character POC_M = '1'; // middle
  public static final Character POC_E = '2'; // end
  public static final Character POC_S = '3'; // single

  public static final String base = "/models/";
  public static final String nsDat = "models/ns_dat.bin";
  public static final String idiomDat = "models/idiom_dat.bin";
  public static final String t2s = base + "t2s.bin";


  /**
   * label转移图
   *
   * @return List(前向label)的数组
   */
  public static int[][] labelPreTransitions(String[] labelValues) {
    int labelSize = labelValues.length;
    List<List<Integer>> labelTrans = new LinkedList<>();
    for (int i = 0; i < labelSize; i++) {
      labelTrans.add(new LinkedList<>());
    }
    for (int i = 0; i < labelSize; i++) {
      for (int j = 0; j < labelSize; j++) {
        Character iPoc = labelValues[i].charAt(0);
        Character jPoc = labelValues[j].charAt(0);
        // 如果有相同词性，则按转移规则进行转移
        if (labelValues[i].substring(1).equals(labelValues[j].substring(1))) {
          if ((iPoc == POC_B && (jPoc == POC_M || jPoc == POC_E)) ||
                  (iPoc == POC_M && (jPoc == POC_M || jPoc == POC_E)) ||
                  (iPoc == POC_E && (jPoc == POC_B || jPoc == POC_S)) ||
                  (iPoc == POC_S && (jPoc == POC_B || jPoc == POC_S))) {
            labelTrans.get(j).add(i);
          }
        } else if ((iPoc == POC_E || iPoc == POC_S) && (jPoc == POC_B || jPoc == POC_S)) {
          labelTrans.get(j).add(i);
        }
      }
    }
    int[][] preTrans = new int[labelSize][];
    for (int i = 0; i < labelSize; i++) {
      preTrans[i] = new int[labelTrans.get(i).size()];
      for (int j = 0; j < labelTrans.get(i).size(); j++) {
        preTrans[i][j] = labelTrans.get(i).get(j);
      }
    }
    return preTrans;
  }

  public static void serialize(Object obj, String path) throws FileNotFoundException {
    Kryo kryo = new Kryo();
    Output output = new Output(new DeflaterOutputStream(new FileOutputStream(path)));
    kryo.writeObject(output, obj);
    output.close();
  }


}
