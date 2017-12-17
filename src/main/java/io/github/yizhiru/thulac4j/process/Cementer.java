package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.model.SegItem;

import java.io.Serializable;
import java.util.List;

/**
 * 词黏结.
 */
public interface Cementer extends Serializable {

    /**
     * 黏结分词结果.
     *
     * @param segItems 序列标注结果.
     */
    void cement(List<SegItem> segItems);
}
