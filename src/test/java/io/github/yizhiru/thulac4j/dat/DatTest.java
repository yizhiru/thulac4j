package io.github.yizhiru.thulac4j.dat;

import io.github.yizhiru.thulac4j.base.Util;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;


/**
 * @author jyzheng
 */
public class DatTest {
  @Test
  public void nsMatch() throws FileNotFoundException {
    Dat dat = Dat.loadDat(Util.nsDat);
    System.out.println(dat.entries.size());
    Assert.assertTrue(dat.isPrefixMatched("黑龙"));
    Assert.assertTrue(dat.isWordMatched("黑龙江"));
    Assert.assertTrue(dat.isWordMatched("齐齐哈尔"));
    Assert.assertTrue(dat.isWordMatched("名古屋"));
    Assert.assertTrue(dat.isWordMatched("克拉约瓦"));
    Assert.assertTrue(dat.isWordMatched("１０月９日街"));
    Assert.assertTrue(dat.isWordMatched("鸡公？"));
    Assert.assertTrue(dat.isWordMatched("齐白石纪念馆"));
    Assert.assertTrue(dat.isWordMatched("龙格伦吉里"));
    Assert.assertTrue(dat.isWordMatched("特德本－圣玛丽"));
    Assert.assertFalse(dat.isWordMatched("首乌"));
  }
}
