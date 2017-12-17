package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.model.SegItem;

import java.io.IOException;
import java.util.List;

/**
 * SegPos 分词，结果带有词性.
 */
public class SegPos extends BaseSegmenter<SegItem> {

    public SegPos(String weightPath, String featurePath) throws IOException {
        super(weightPath, featurePath);
    }

    @Override
    List<SegItem> process(List<SegItem> segItems) {
        return segItems;
    }
}
