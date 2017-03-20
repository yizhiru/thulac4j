package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.base.Util;
import io.github.yizhiru.thulac4j.dat.Dat;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * stop words filter
 */
public class StopFilter {

  private Dat stop;

  public StopFilter() throws FileNotFoundException {
    stop = Dat.loadDat(this.getClass().getResourceAsStream(Util.stopDat));
  }


  public void filter(List<String> segmented) {
    for (int i = 0; i < segmented.size(); i++) {
      if (stop.isWordMatched(segmented.get(i)))
        segmented.remove(i);
    }
  }
}
