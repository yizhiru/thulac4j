package io.github.yizhiru.thulac4j.model;

/**
 * POC (position of char) 种类，用以描述字符的可能标注 (label) 信息.
 */
public enum POC {

    /**
     * Punctuation POC.
     */
    PUNCTUATION_POC,

    /**
     * Begin of numeral.
     */
    BEGIN_M_POC,

    /**
     * Middle of numeral.
     */
    MIDDLE_M_POC,

    /**
     * End of numeral.
     */
    END_M_POC,

    /**
     * Single of numeral.
     */
    SINGLE_M_POC,

    /**
     * Word begin.
     */
    BEGIN_POC,

    /**
     * Word middle.
     */
    MIDDLE_POC,

    /**
     * Word end.
     */
    END_POC,

    /**
     * Single character as a word.
     */
    SINGLE_POC,

    /**
     * Begin or single.
     */
    BS_POC,

    /**
     * End or single.
     */
    ES_POC,

    /**
     * Default POC.
     */
    DEFAULT_POC;

    /**
     * 对可能标注求交集，比如，若某字符的标注既可能为BS_POC，也可能为ES_POC，
     * 则其标注为SINGLE_POC.
     *
     * @param that 另一种可能POC.
     * @return 交集POC.
     */
    public POC intersect(POC that) {
        if (this.ordinal() < that.ordinal()) {
            if (this == BS_POC && that == ES_POC) {
                return SINGLE_POC;
            }
            return this;
        } else if (this == ES_POC && that == BS_POC) {
            return SINGLE_POC;
        }
        return that;
    }
}
