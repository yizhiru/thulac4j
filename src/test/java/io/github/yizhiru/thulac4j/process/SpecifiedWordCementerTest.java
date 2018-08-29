package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.term.TokenItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpecifiedWordCementerTest {

    @Test
    public void cement() {
        List<TokenItem> tokenItems = new ArrayList<>(Arrays.asList(
                new TokenItem("?", "w"),
                new TokenItem("27", "m"),
                new TokenItem("日", "q"))
        );
        SpecifiedWordCementer.cementTimeWord(tokenItems);
        assertEquals(2, tokenItems.size());
        assertEquals("27日", tokenItems.get(1).word);
        assertEquals("t", tokenItems.get(1).pos);
    }
}