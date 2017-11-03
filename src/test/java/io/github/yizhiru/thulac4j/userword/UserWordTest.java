package io.github.yizhiru.thulac4j.userword;

import io.github.yizhiru.thulac4j.SegPos;
import io.github.yizhiru.thulac4j.base.SegItem;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserWordTest {

    @Test
    public void test() throws IOException {
        String sentence = "滔滔的流水，名海春向着波士顿湾飞个去无声逝去";
        SegPos pos = new SegPos("models/seg_pos.bin");
        pos.addUserWordsPath(this.getClass().getClassLoader().getResource("").getFile() + "noun.dict", "n");
        pos.addUserWordsPath(this.getClass().getClassLoader().getResource("").getFile() + "verb.dict", "v");
        List<SegItem> segItems = pos.segment(sentence);
        Map<String, String> segItemMap = segItems.stream().collect(Collectors.toMap(s -> s.word, s -> s.pos));
        Assert.assertEquals(segItemMap.get("名海春"), "n");
        Assert.assertEquals(segItemMap.get("飞个去"), "v");
    }
}
