package io.github.yizhiru.thulac4j.util;

import org.junit.Test;

import static io.github.yizhiru.thulac4j.util.CharUtils.isHan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CharUtilsTest {

    @Test
    public void checkIsHan() {
        assertFalse(isHan('%'));
        assertFalse(isHan('&'));
        assertFalse(isHan('i'));
        assertFalse(isHan('？'));

        assertTrue(isHan('是'));
        assertTrue(isHan('一'));
        assertTrue(isHan('〇'));
        assertTrue(isHan('甘'));
    }
}
