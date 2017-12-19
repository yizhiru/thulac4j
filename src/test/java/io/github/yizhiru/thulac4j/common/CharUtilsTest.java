package io.github.yizhiru.thulac4j.common;

import org.junit.Test;

import static io.github.yizhiru.thulac4j.common.CharUtils.isHan;
import static org.junit.Assert.assertEquals;

public class CharUtilsTest {

    @Test
    public void checkIsHan() {
        assertEquals(false, isHan('%'));
        assertEquals(false, isHan('&'));
        assertEquals(false, isHan('i'));
        assertEquals(false, isHan('？'));
        assertEquals(true, isHan('是'));
        assertEquals(true, isHan('一'));
        assertEquals(true, isHan('〇'));
        assertEquals(true, isHan('甘'));
    }
}
