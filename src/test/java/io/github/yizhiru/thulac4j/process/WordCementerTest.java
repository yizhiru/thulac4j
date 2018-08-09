package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.term.SegItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WordCementerTest {

    @Test
    public void cement() {
        List<SegItem> segItems = new ArrayList<>(Arrays.asList(
                new SegItem("?", "w"),
                new SegItem("27", "m"),
                new SegItem("日", "q"))
        );
        WordCementer.cementTimeWord(segItems);
        assertEquals(2, segItems.size());
        assertEquals("27日", segItems.get(1).word);
        assertEquals("t", segItems.get(1).pos);
    }
}