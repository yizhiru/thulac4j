package io.github.yizhiru.thulac4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseSegmenter.class)
public class BaseSegmenterTest {

    /**
     * SegOnly weights model path.
     */
    public static final String SEG_ONLY_WEIGHTS_PATH = "models/cws_model.bin";

    /**
     * SegOnly features path.
     */
    public static final String SEG_ONLY_FEATURES_PATH = "models/cws_dat.bin";

    /**
     * SegPos weights model path.
     */
    public static final String SEG_POS_WEIGHTS_PATH = "models/model_c_model.bin";

    /**
     * SegPos features path.
     */
    public static final String SEG_POS_FEATURES_PATH = "models/model_c_dat.bin";

    @Test
    public void setPreviousTrans() throws Exception {
        BaseSegmenter segmenter = new SegOnly(SEG_ONLY_WEIGHTS_PATH, SEG_ONLY_FEATURES_PATH);
        int[][] previousTrans = WhiteboxImpl.invokeMethod(
                segmenter,
                "setPreviousTrans",
                new Class<?>[]{String[].class},
                (Object) segmenter.model.labelValues);

        assertEquals("[[1, 2], [0, 3], [1, 2], [0, 3]]",
                Arrays.deepToString(previousTrans));

        segmenter = new SegPos(SEG_POS_WEIGHTS_PATH, SEG_POS_FEATURES_PATH);
        previousTrans = WhiteboxImpl.invokeMethod(
                segmenter,
                "setPreviousTrans",
                new Class<?>[]{String[].class},
                (Object) segmenter.model.labelValues);
        assertEquals("[1, 2, 4, 5, 7, 10, 13, 15, 17, 18, 19, 23, 25, 27, " +
                        "30, 32, 33, 34, 35, 36, 37, 38, 39, 41, 44, 45, 48, 50, 53, " +
                        "56, 57, 59, 61, 63, 67, 69, 72, 74, 76, 80, 81, 82, 83, 88, " +
                        "89, 90, 91, 95]",
                Arrays.toString(previousTrans[0]));
        assertEquals("[0, 20]", Arrays.toString(previousTrans[1]));
        assertEquals("[54, 55]", Arrays.toString(previousTrans[56]));
        assertEquals("[93, 94]", Arrays.toString(previousTrans[95]));
    }
}