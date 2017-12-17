package io.github.yizhiru.thulac4j.dat;

import io.github.yizhiru.thulac4j.common.ModelPath;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatTest {

    @Test
    public void isMatched() throws IOException {
        Dat dat = Dat.loadDat("." + ModelPath.NS_BIN_PATH);
        assertTrue(dat.isPrefixMatched("黑龙江"));
        assertTrue(dat.isWordMatched("黑龙江"));
        assertTrue(dat.isWordMatched("齐齐哈尔"));
        assertTrue(dat.isWordMatched("名古屋"));
        assertTrue(dat.isWordMatched("克拉约瓦"));
        assertTrue(dat.isWordMatched("１０月９日街"));
        assertTrue(dat.isWordMatched("鸡公？"));
        assertTrue(dat.isWordMatched("齐白石纪念馆"));
        assertTrue(dat.isWordMatched("龙格伦吉里"));
        assertTrue(dat.isWordMatched("特德本－圣玛丽"));
        assertFalse(dat.isWordMatched("首乌"));
    }

    @Test
    public void serialize() throws IOException {
        String[] dictPaths = new String[]{
                ModelPath.IDIOM_DICT_PATH,
                ModelPath.NS_DICT_PATH,
                ModelPath.STOP_WORDS_DICT_PATH,
        };
        String[] binPaths = new String[]{
                "." + ModelPath.IDIOM_BIN_PATH,
                "." + ModelPath.NS_BIN_PATH,
                "." + ModelPath.STOP_WORDS_BIN_PATH,
        };
        for (int i = 0; i < dictPaths.length; i++) {
            Dat expect = DatMaker.make(dictPaths[i]);
            expect.serialize(binPaths[i]);
            Dat actual = Dat.loadDat(binPaths[i]);

            assertTrue(expect.size() == actual.size());
            for (int j = 0; j < expect.size(); j++) {
                assertEquals(expect.get(j), actual.get(j));
            }
        }
    }
}
