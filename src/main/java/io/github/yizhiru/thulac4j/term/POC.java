package io.github.yizhiru.thulac4j.term;

/**
 * POC (position of char) 种类，用以描述字符的标注 (label) 信息.
 */
public enum POC {

    /**
     * Punctuation POC.
     */
    PUNCTUATION_POC,

    /**
     * Begin of numeral.
     */
    BEGIN_NUMERAL_POC,

    /**
     * Middle of numeral.
     */
    MIDDLE_NUMERAL_POC,

    /**
     * End of numeral.
     */
    END_NUMERAL_POC,

    /**
     * Single of numeral.
     */
    SINGLE_NUMERAL_POC,

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
    BEGIN_OR_SINGLE_POC,

    /**
     * End or single.
     */
    END_OR_SINGLE_POC,

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
            if (this == BEGIN_OR_SINGLE_POC && that == END_OR_SINGLE_POC) {
                return SINGLE_POC;
            }
            return this;
        } else if (this == END_OR_SINGLE_POC && that == BEGIN_OR_SINGLE_POC) {
            return SINGLE_POC;
        }
        return that;
    }
}
