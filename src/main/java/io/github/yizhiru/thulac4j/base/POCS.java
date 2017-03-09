package io.github.yizhiru.thulac4j.base;

/**
 * @author jyzheng
 */
public enum POCS {
  // 各种组合情况下对应的POC
  PUN_POCS, B_M_POC, M_M_POC, E_M_POC, S_M_POC,
  B_POCS, M_POCS, E_POCS, S_POCS,
  BS_POCS, ES_POCS, DEFAULT_POCS;


  public POCS intersect(POCS another) {
    if (this.ordinal() < another.ordinal()) {
      if (this == BS_POCS && another == ES_POCS) return S_POCS;
      return this;
    }
    if (this == ES_POCS && another == BS_POCS) return S_POCS;
    return another;
  }


}
