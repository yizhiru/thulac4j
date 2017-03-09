package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.base.SegItem;
import io.github.yizhiru.thulac4j.base.Util;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author jyzheng
 */
public class CementerTest {

  @Test
  public void cementNs() throws FileNotFoundException {
    Cementer cementer = new Cementer(Util.nsDat, "ns");
    List<String> arr = new ArrayList<>(Arrays.asList("黑", "龙", "江"));
    cementer.cement(arr);
    assertArrayEquals(arr.toArray(), new String[]{"黑龙江"});
  }

  @Test
  public void cementNsPos() throws FileNotFoundException {
    Cementer cementer = new Cementer(Util.nsDat, "ns");
    List<SegItem> arr = new ArrayList<>(Arrays.asList(
            new SegItem("黑", null),
            new SegItem("龙", "n"),
            new SegItem("江", "j")));
    cementer.cementPos(arr);
    assertArrayEquals(arr.toArray(), new SegItem[]{new SegItem("黑龙江", "ns")});
  }

}
