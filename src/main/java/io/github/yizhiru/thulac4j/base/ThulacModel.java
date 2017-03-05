package io.github.yizhiru.thulac4j.base;

import io.github.yizhiru.thulac4j.dat.Dat;

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
  public int labelSize; //size of the labels
  public int featureSize; //size of the features
  public int[] llWeights; // weights of (label, label)
  public int[] flWeights; // weights of (featureDat, label)

  // feature dat: cws_dat.bin  model_c_model.bin
  public int datSize;
  public int[] featDat;

  // label: cws_label.txt  model_c_label.txt
  public String[] labels;

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
    labels = new String[labelList.size()];
    labelList.toArray(labels);
  }

  public void convert(String path) throws FileNotFoundException {
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

    // convert labels
    cws.labelValues = labels;
    cws.allowTabular = new int[7][];
    List<List<Integer>> pocTags = pocTag();
    int[] indices = {1, 2, 4, 8, 9, 12, 15};
    for (int i = 0; i < 7; i++) {
      cws.allowTabular[i] = toArray(pocTags, indices[i]);
    }

    // serialization
    Util.serialize(cws, path);
  }

  private List<List<Integer>> pocTag() {
    List<List<Integer>> pocTags = new ArrayList<>();
    for (int i = 0; i < 16; i++) {
      pocTags.add(new ArrayList<>());
    }
    for (int i = 0; i < labels.length; i++) {
      int segIndex = labels[i].charAt(0) - '0';
      for (int j = 0; j < 16; j++) {
        if (((1 << segIndex) & j) != 0)
          pocTags.get(j).add(i);
      }
    }
    return pocTags;
  }

  private int[] toArray(List<List<Integer>> pocTags, int index) {
    List<Integer> list = pocTags.get(index);
    int[] arr = new int[list.size()];
    for (int i = 0; i < list.size(); i++) {
      arr[i] = list.get(i);
    }
    return arr;
  }


  public static void main(String[] args) throws IOException {
//    ThulacModel thulac = new ThulacModel("D:\\IdeaProjects\\scala-test\\models/cws_model.bin",
//            "D:\\IdeaProjects\\scala-test\\models/cws_dat.bin",
//            "D:\\IdeaProjects\\scala-test\\models/cws_label.txt");
    ThulacModel thulac = new ThulacModel("D:\\IdeaProjects\\scala-test\\models/model_c_model.bin",
            "D:\\IdeaProjects\\scala-test\\models/model_c_dat.bin",
            "D:\\IdeaProjects\\scala-test\\models/model_c_label.txt");

    System.out.println(thulac.featureSize);
    System.out.println(thulac.labelSize);
    System.out.println(thulac.llWeights[0]);
    System.out.println(thulac.flWeights[0]);
    System.out.println(thulac.datSize);
    System.out.println(thulac.featDat[5]);
    for (String s : thulac.labels) {
      System.out.println(s);
    }
    System.out.println(thulac.pocTag());
    thulac.convert("models/seg_pos.bin");
  }
}
