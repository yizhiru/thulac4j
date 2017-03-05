package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.base.SegItem;
import org.junit.Test;

import java.util.List;

/**
 * @author jyzheng
 */
public class PosTest {

  public static void main(String[] args) throws Exception {
    SegPOSer poser = new SegPOSer("models/seg_pos.bin");

    for (String sentence : SegmentTest.sentences) {
      List<SegItem> result = poser.segment(sentence);
      System.out.println(result);
    }

    for (String sentence : SegmentTest.bugs) {
      List<SegItem> result = poser.segment(sentence);
      System.out.println(result);
    }
  }
}
