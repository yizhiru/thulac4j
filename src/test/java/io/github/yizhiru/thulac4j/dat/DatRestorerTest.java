package io.github.yizhiru.thulac4j.dat;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jyzheng
 */
public class DatRestorerTest {

  @Test
  public void restoreTest() throws IOException {
    Dat stop = Dat.loadDat("models/stop_dat.bin");
    Set<String> dict = new HashSet<>(DatMakerTest.readDict(
            "dicts/stop_words.dict"));
    List<String> words = DatRestorer.restore(stop);
    System.out.println("stop words size: " + dict.size());
    for(String word: words) {
      Assert.assertTrue(dict.contains(word));
    }
    Assert.assertTrue(dict.size() == words.size());
  }
}
