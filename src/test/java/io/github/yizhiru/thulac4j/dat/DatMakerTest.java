package io.github.yizhiru.thulac4j.dat;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

  public static List<String> readDict(String path) throws IOException {
    List<String> dict = new ArrayList<>();
    BufferedReader br = new BufferedReader(new InputStreamReader(
            new FileInputStream(path)));
    String line;
    while ((line = br.readLine()) != null) {
      line = line.trim();
      if (line.length() == 0) continue;
      dict.add(line);
    }
    return dict;
  }


  @Test
  public void make() throws IOException {
    String path = "dicts/idiom.dict";
    List<String> lexicon = readDict(path);
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
