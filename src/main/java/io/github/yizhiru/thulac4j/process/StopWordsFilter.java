package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.common.DoubleArrayTrie;
import io.github.yizhiru.thulac4j.common.ModelPaths;

import java.io.IOException;
import java.util.List;

/**
 * Stop words filter.
 */
public class StopWordsFilter {

    private DoubleArrayTrie stopDat;

    public StopWordsFilter() throws IOException {
        stopDat = DoubleArrayTrie.loadDat(
                this.getClass().getResourceAsStream(ModelPaths.STOP_WORDS_BIN_PATH));
    }

    public void filter(List<String> segmented) {
        for (int i = 0; i < segmented.size(); i++) {
            if (stopDat.isWordMatched(segmented.get(i))) {
                segmented.remove(i);
            }
        }
    }
}
