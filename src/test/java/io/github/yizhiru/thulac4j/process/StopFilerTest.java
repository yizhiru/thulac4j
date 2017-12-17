package io.github.yizhiru.thulac4j.process;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class StopFilerTest {

    @Test
    public void filterTest() throws IOException, ClassNotFoundException {
        List<String> segmented = new ArrayList<>(Arrays.asList(
                "此时",
                "我",
                "能做的事",
                "，",
                "绝不推诿",
                "到",
                "下",
                "一时",
                "刻",
                "；"));

        StopWordsFilter stopFilter = new StopWordsFilter();
        stopFilter.filter(segmented);
        assertArrayEquals(
                new String[]{"我", "能做的事", "绝不推诿", "到", "下", "刻"},
                segmented.toArray());
    }
}
