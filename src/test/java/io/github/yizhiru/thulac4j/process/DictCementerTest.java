package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.common.ModelPaths;
import io.github.yizhiru.thulac4j.term.SegItem;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DictCementerTest {

    @Test
    public void cement() throws IOException {
        DictCementer cementer = new DictCementer(
                this.getClass().getResourceAsStream(ModelPaths.NS_BIN_PATH),
                "ns");
        List<SegItem> segItems = new ArrayList<>(Arrays.asList(
                new SegItem("黑", null),
                new SegItem("龙", "n"),
                new SegItem("江", "j"))
        );
        cementer.cement(segItems);
        assertEquals("[黑龙江/ns]", segItems.toString());

        cementer = new DictCementer(
                this.getClass().getResourceAsStream(ModelPaths.IDIOM_BIN_PATH),
                "i");
        segItems = new ArrayList<>(Arrays.asList(
                new SegItem("掉", null),
                new SegItem("进", "n"),
                new SegItem("了", "j"),
                new SegItem("无", "n"),
                new SegItem("底洞", "j"))
        );
        cementer.cement(segItems);
        assertEquals("[掉, 进/n, 了/j, 无/n, 底洞/j]", segItems.toString());
    }
}
