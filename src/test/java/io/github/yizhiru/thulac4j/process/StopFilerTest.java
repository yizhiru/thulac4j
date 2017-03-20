package io.github.yizhiru.thulac4j.process;

import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jyzheng
 */
public class StopFilerTest {

  @Test
  public void filterTest() throws FileNotFoundException {
    List<String> segmented = new ArrayList<>(Arrays.asList("此时", "我", "能做的事", "，",
            "绝不推诿", "到", "下", "一时", "刻", "；"));
    StopFilter stopFilter = new StopFilter();
    stopFilter.filter(segmented);
    String[] arr = {"我", "能做的事", "绝不推诿", "到", "下", "刻"};
    Assert.assertArrayEquals(segmented.toArray(), arr);
  }
}
