package io.github.yizhiru.thulac4j.base;

import io.github.yizhiru.thulac4j.dat.Dat;
import io.github.yizhiru.thulac4j.process.Cementer;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jyzheng
 */
public class ThulacModel {
  // weight model: cws_model.bin  model_c_model.bin
  public int labelSize; //size of the labelValues
  public int featureSize; //size of the features
  public int[] llWeights; // weights of (label, label)
  public int[] flWeights; // weights of (featureDat, label)

  // feature dat: cws_dat.bin  model_c_model.bin
  public int datSize;
  public int[] featDat;

  // label: cws_label.txt  model_c_label.txt
  public String[] labelValues;

  public ThulacModel(String modelPath, String featPath, String labelPath) throws IOException {
    // load weights model
    FileChannel channel = new FileInputStream(modelPath).getChannel();
    ByteBuffer buff = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
    IntBuffer intBuffer = buff.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
    labelSize = intBuffer.get();
    featureSize = intBuffer.get();
    llWeights = new int[labelSize * labelSize];
    flWeights = new int[featureSize * labelSize];
    intBuffer.get(llWeights);
    intBuffer.get(flWeights);
    channel.close();

    // load feature DAT
    channel = new FileInputStream(featPath).getChannel();
    datSize = (int) channel.size() / 8;
    featDat = new int[datSize * 2];
    buff = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
    intBuffer = buff.order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
    intBuffer.get(featDat);
    channel.close();

    // load label file
    BufferedReader reader = new BufferedReader(new InputStreamReader(
            new FileInputStream(labelPath)));
    String line;
    List<String> labelList = new ArrayList<>();
    while ((line = reader.readLine()) != null) {
      labelList.add(line);
    }
    reader.close();
    labelValues = new String[labelList.size()];
    labelList.toArray(labelValues);
  }

  public void serialize(String path) throws FileNotFoundException {
    CwsModel cws = new CwsModel();
    // convert model
    cws.featureSize = featureSize;
    cws.labelSize = labelSize;
    cws.llWeights = new int[labelSize][];
    for (int i = 0; i < labelSize; i++) {
      cws.llWeights[i] = new int[labelSize];
    }
    for (int i = 0; i < llWeights.length; i++) {
      cws.llWeights[i / labelSize][i % labelSize] = llWeights[i];
    }
    cws.flWeights = new int[featureSize][];
    for (int i = 0; i < featureSize; i++) {
      cws.flWeights[i] = new int[labelSize];
    }
    for (int i = 0; i < flWeights.length; i++) {
      cws.flWeights[i / labelSize][i % labelSize] = flWeights[i];
    }

    // convert feature DAT
    List<Dat.Entry> dat = new ArrayList<>(datSize);
    for (int i = 0; i < 2 * datSize; i += 2) {
      dat.add(new Dat.Entry(featDat[i], featDat[i + 1]));
    }
    cws.featureDat = new Dat(dat);

    // convert labelValues
    cws.labelValues = labelValues;
    cws.allowTabular = new int[12][];
    List<List<Integer>> pocTags = pocTag();
    for (int i = 0; i < labelValues.length; i++) {
      if (labelValues[i].equals("3") || labelValues[i].equals("3w")) // punctuation
        cws.allowTabular[0] = new int[]{i};
      if (labelValues[i].equals("0") || labelValues[i].equals("0m"))  // begin of numeral
        cws.allowTabular[1] = new int[]{i};
      if (labelValues[i].equals("1") || labelValues[i].equals("1m"))  // middle of numeral
        cws.allowTabular[2] = new int[]{i};
      if (labelValues[i].equals("2") || labelValues[i].equals("2m"))  // end of numeral
        cws.allowTabular[3] = new int[]{i};
      if (labelValues[i].equals("3") || labelValues[i].equals("3m"))  // single of numeral
        cws.allowTabular[4] = new int[]{i};
    }
    int[] indices = {1, 2, 4, 8, 9, 12, 15};
    for (int i = 0; i < 7; i++) {
      cws.allowTabular[i + 5] = toArray(pocTags, indices[i]);
    }

    cws.ns = new Cementer(Util.nsDat, "ns");
    cws.idiom = new Cementer(Util.idiomDat, "i");

    // serialization
    Util.serialize(cws, path);
  }

  // get the pocTags
  public List<List<Integer>> pocTag() {
    List<List<Integer>> pocTags = new ArrayList<>();
    for (int i = 0; i < 16; i++) {
      pocTags.add(new ArrayList<>());
    }
    for (int i = 0; i < labelValues.length; i++) {
      int segIndex = labelValues[i].charAt(0) - '0';
      for (int j = 0; j < 16; j++) {
        if (((1 << segIndex) & j) != 0)
          pocTags.get(j).add(i);
      }
    }
    return pocTags;
  }

  // get int[] array from pocTags[i]
  private int[] toArray(List<List<Integer>> pocTags, int index) {
    List<Integer> list = pocTags.get(index);
    int[] arr = new int[list.size()];
    for (int i = 0; i < list.size(); i++) {
      arr[i] = list.get(i);
    }
    return arr;
  }

}
