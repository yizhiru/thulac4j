package io.github.yizhiru.thulac4j;

import java.nio.charset.StandardCharsets;

import static io.github.yizhiru.thulac4j.SegOnlyTest.sentences;

/**
 * @author jyzheng
 */
public class SegPosTest {

  public static void main(String[] args) throws Exception {
    SegPos poser = new SegPos("models/seg_pos.bin");

    for (String sentence : sentences) {
      System.out.println(poser.segment(sentence));
    }

    for (String sentence : SegOnlyTest.busSentences) {
      System.out.println(poser.segment(sentence));
    }

    long length = 0L;
    long start = System.currentTimeMillis();
    for (int i = 0; i < 10000; ++i) {
      for (String sentence : sentences) {
        poser.segment(sentence);
        length += sentence.getBytes(StandardCharsets.UTF_8).length;
      }
    }
    long elapsed = (System.currentTimeMillis() - start);
    System.out.println(String.format("time elapsed: %d ms, rate: %f kb/s.",
            elapsed, (length * 1.0) / 1024.0f / (elapsed * 1.0 / 1000.0f)));
  }
}
