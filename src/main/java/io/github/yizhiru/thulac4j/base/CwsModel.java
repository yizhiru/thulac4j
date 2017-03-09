package io.github.yizhiru.thulac4j.base;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import io.github.yizhiru.thulac4j.dat.Dat;
import io.github.yizhiru.thulac4j.process.Cementer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.zip.InflaterInputStream;

/**
 * @author jyzheng
 */
public class CwsModel implements Serializable {

  public int labelSize;
  public int featureSize;
  public int[][] llWeights; // weights of (label, label)
  public int[][] flWeights; // weights of (feature, label)
  public Dat featureDat; // feature DAT
  public String[] labelValues;
  public int[][] allowTabular; // map to enum POCS

  public Cementer ns;
  public Cementer idiom;

  public static CwsModel loadModel(String path) throws FileNotFoundException {
    FileInputStream fis = new FileInputStream(path);
    return loadModel(fis);
  }

  public static CwsModel loadModel(InputStream in) {
    Kryo kryo = new Kryo();
    Input input = new Input(new InflaterInputStream(in));
    CwsModel model = kryo.readObject(input, CwsModel.class);
    input.close();
    return model;
  }


}
