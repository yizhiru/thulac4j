package io.github.yizhiru.thulac4j.base;

import io.github.yizhiru.thulac4j.dat.Dat;
import io.github.yizhiru.thulac4j.dat.DatMaker;

import java.io.IOException;

/**
 * @author jyzheng
 */
public class Serialization {

  public static void main(String[] args) throws IOException {
    DatMaker maker = new DatMaker();
    Dat dat = maker.make("dicts/ns.dict");
    Util.serialize(dat, "models/ns_dat.bin");
    DatMaker maker1 = new DatMaker();
    dat = maker.make("dicts/idiom.dict");
    Util.serialize(dat, "models/idiom_dat.bin");
  }

}
