package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.common.ModelPaths;
import io.github.yizhiru.thulac4j.dat.Dat;

import java.io.IOException;
import java.util.List;

/**
 * Stop words filter.
 */
public class StopWordsFilter {

    private Dat stop;

    public StopWordsFilter() throws IOException, ClassNotFoundException {
        stop = Dat.loadDat(
                this.getClass().getResourceAsStream(ModelPaths.STOP_WORDS_BIN_PATH)
        );
    }

    public void filter(List<String> segmented) {
        for (int i = 0; i < segmented.size(); i++) {
            if (stop.isWordMatched(segmented.get(i))) {
                segmented.remove(i);
            }
        }
    }
}
