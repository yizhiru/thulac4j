package io.github.yizhiru.thulac4j.model;

import org.junit.Test;

import static io.github.yizhiru.thulac4j.model.POC.BEGIN_M_POC;
import static io.github.yizhiru.thulac4j.model.POC.BEGIN_POC;
import static io.github.yizhiru.thulac4j.model.POC.BS_POC;
import static io.github.yizhiru.thulac4j.model.POC.DEFAULT_POC;
import static io.github.yizhiru.thulac4j.model.POC.END_M_POC;
import static io.github.yizhiru.thulac4j.model.POC.END_POC;
import static io.github.yizhiru.thulac4j.model.POC.ES_POC;
import static io.github.yizhiru.thulac4j.model.POC.MIDDLE_M_POC;
import static io.github.yizhiru.thulac4j.model.POC.MIDDLE_POC;
import static io.github.yizhiru.thulac4j.model.POC.PUNCTUATION_POC;
import static io.github.yizhiru.thulac4j.model.POC.SINGLE_M_POC;
import static io.github.yizhiru.thulac4j.model.POC.SINGLE_POC;
import static org.junit.Assert.assertEquals;

public class POCTest {

    @Test
    public void intersect() {
        assertEquals(PUNCTUATION_POC, PUNCTUATION_POC.intersect(BEGIN_POC));

        assertEquals(BEGIN_M_POC, BEGIN_POC.intersect(BEGIN_M_POC));
        assertEquals(END_M_POC, END_POC.intersect(END_M_POC));
        assertEquals(MIDDLE_M_POC, MIDDLE_M_POC.intersect(MIDDLE_POC));
        assertEquals(SINGLE_M_POC, SINGLE_M_POC.intersect(SINGLE_POC));

        assertEquals(SINGLE_POC, BS_POC.intersect(ES_POC));
        assertEquals(SINGLE_POC, ES_POC.intersect(BS_POC));

        assertEquals(SINGLE_POC, DEFAULT_POC.intersect(SINGLE_POC));
        assertEquals(BEGIN_M_POC, BEGIN_M_POC.intersect(DEFAULT_POC));
    }
}