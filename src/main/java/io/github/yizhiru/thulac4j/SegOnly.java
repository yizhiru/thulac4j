package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.model.SegItem;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SegOnly 分词，结果没有词性.
 */
public final class SegOnly extends BaseSegmenter<String> {

    public SegOnly(String weightPath, String featurePathh) throws IOException {
        super(weightPath, featurePathh);
    }

    @Override
    List<String> process(List<SegItem> segItems) {
        return segItems.stream()
                .map(item -> (item.word))
                .collect(Collectors.toList());
    }
}
