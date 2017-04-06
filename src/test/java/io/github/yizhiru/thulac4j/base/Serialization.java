package io.github.yizhiru.thulac4j.base;

import io.github.yizhiru.thulac4j.dat.Dat;
import io.github.yizhiru.thulac4j.dat.DatMaker;

import java.io.IOException;

/**
 * @author jyzheng
 */
public class Serialization {

  public static void main(String[] args) throws IOException {
    Dat dat = DatMaker.make("dicts/stop_words.dict");
    Util.serialize(dat, "models/stop_dat.bin");
    dat = DatMaker.make("dicts/idiom.dict");
    Util.serialize(dat, "models/idiom_dat.bin");
  }

}
