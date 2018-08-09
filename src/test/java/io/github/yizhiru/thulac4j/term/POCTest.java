package io.github.yizhiru.thulac4j.term;

import org.junit.Test;

import static io.github.yizhiru.thulac4j.term.POC.BEGIN_NUMERAL_POC;
import static io.github.yizhiru.thulac4j.term.POC.BEGIN_POC;
import static io.github.yizhiru.thulac4j.term.POC.BEGIN_OR_SINGLE_POC;
import static io.github.yizhiru.thulac4j.term.POC.DEFAULT_POC;
import static io.github.yizhiru.thulac4j.term.POC.END_NUMERAL_POC;
import static io.github.yizhiru.thulac4j.term.POC.END_POC;
import static io.github.yizhiru.thulac4j.term.POC.END_OR_SINGLE_POC;
import static io.github.yizhiru.thulac4j.term.POC.MIDDLE_NUMERAL_POC;
import static io.github.yizhiru.thulac4j.term.POC.MIDDLE_POC;
import static io.github.yizhiru.thulac4j.term.POC.PUNCTUATION_POC;
import static io.github.yizhiru.thulac4j.term.POC.SINGLE_NUMERAL_POC;
import static io.github.yizhiru.thulac4j.term.POC.SINGLE_POC;
import static org.junit.Assert.assertEquals;

public class POCTest {

    @Test
    public void intersect() {
        assertEquals(PUNCTUATION_POC, PUNCTUATION_POC.intersect(BEGIN_POC));

        assertEquals(BEGIN_NUMERAL_POC, BEGIN_POC.intersect(BEGIN_NUMERAL_POC));
        assertEquals(END_NUMERAL_POC, END_POC.intersect(END_NUMERAL_POC));
        assertEquals(MIDDLE_NUMERAL_POC, MIDDLE_NUMERAL_POC.intersect(MIDDLE_POC));
        assertEquals(SINGLE_NUMERAL_POC, SINGLE_NUMERAL_POC.intersect(SINGLE_POC));

        assertEquals(SINGLE_POC, BEGIN_OR_SINGLE_POC.intersect(END_OR_SINGLE_POC));
        assertEquals(SINGLE_POC, END_OR_SINGLE_POC.intersect(BEGIN_OR_SINGLE_POC));

        assertEquals(SINGLE_POC, DEFAULT_POC.intersect(SINGLE_POC));
        assertEquals(BEGIN_NUMERAL_POC, BEGIN_NUMERAL_POC.intersect(DEFAULT_POC));
    }
}