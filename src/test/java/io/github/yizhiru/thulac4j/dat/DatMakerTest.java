package io.github.yizhiru.thulac4j.dat;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jyzheng
 */
public class DatMakerTest {

  @Test
  public void build() {
    List<String> lexicon = new ArrayList<>(Arrays.asList("不会", "不住", "不到", "不想",
            "不敢", "不是", "不能", "䃟头窑", "䃟头", "䃟头角"));
    DatMaker maker = new DatMaker();
    Dat dat = maker.build(lexicon);
    for (String word : lexicon) {
      Assert.assertTrue(dat.isPrefixMatched(word.substring(0, word.length() - 1)));
      Assert.assertTrue(dat.isWordMatched(word));
    }
  }


  @Test
  public void make() throws IOException {
    String path = "dicts/idiom.dict";
    BufferedReader br = new BufferedReader(new InputStreamReader(
            new FileInputStream(path)));
    String line;
    List<String> lexicon = new ArrayList<>();
    while ((line = br.readLine()) != null) {
      line = line.trim();
      if (line.length() == 0) continue;
      lexicon.add(line);
    }
    System.out.println("words size: " + lexicon.size());
    DatMaker maker = new DatMaker();
    Dat dat = maker.build(lexicon);
    System.out.println("DAT size: " + dat.entries.size());
    for (String word : lexicon) {
      Assert.assertTrue(dat.isPrefixMatched(word.substring(0, word.length() - 1)));
      Assert.assertTrue(dat.isWordMatched(word));
    }
  }
}
