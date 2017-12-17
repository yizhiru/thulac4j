package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.model.SegItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimeCementerTest {

    @Test
    public void cement() {
        List<SegItem> segItems = new ArrayList<>(Arrays.asList(
                new SegItem("?", "w"),
                new SegItem("27", "m"),
                new SegItem("日", "q"))
        );
        TimeCementer.cement(segItems);
        assertTrue(segItems.size() == 2);
        assertEquals("27日", segItems.get(1).word);
        assertEquals("t", segItems.get(1).pos);
    }
}