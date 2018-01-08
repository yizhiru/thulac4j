package io.github.yizhiru.thulac4j.model;

import io.github.yizhiru.thulac4j.process.Ruler;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static io.github.yizhiru.thulac4j.BaseSegmenterTest.SEG_ONLY_FEATURES_PATH;
import static io.github.yizhiru.thulac4j.BaseSegmenterTest.SEG_ONLY_WEIGHTS_PATH;
import static io.github.yizhiru.thulac4j.BaseSegmenterTest.SEG_POS_FEATURES_PATH;
import static io.github.yizhiru.thulac4j.BaseSegmenterTest.SEG_POS_WEIGHTS_PATH;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CwsModelTest {

    @Test
    public void loadSegOnlyModel() throws IOException {
        CwsModel cwsModel = new CwsModel(SEG_ONLY_WEIGHTS_PATH, SEG_ONLY_FEATURES_PATH);
        assertEquals(2453880, cwsModel.featureSize);
        assertEquals(4, cwsModel.labelSize);
        assertEquals(-42717, cwsModel.llWeights[0]);
        assertEquals(-4958, cwsModel.flWeights[0]);

        assertArrayEquals(new String[]{"3"}, getPosValue(POC.SINGLE_M_POC, cwsModel));
        assertArrayEquals(new String[]{"0"}, getPosValue(POC.BEGIN_POC, cwsModel));
        assertArrayEquals(new String[]{"1"}, getPosValue(POC.MIDDLE_POC, cwsModel));
        assertArrayEquals(new String[]{"2"}, getPosValue(POC.END_POC, cwsModel));
        assertArrayEquals(new String[]{"3"}, getPosValue(POC.SINGLE_POC, cwsModel));
        assertArrayEquals(new String[]{"0", "3"}, getPosValue(POC.BS_POC, cwsModel));
        assertArrayEquals(new String[]{"2", "3"}, getPosValue(POC.ES_POC, cwsModel));
        assertArrayEquals(
                new String[]{"0", "2", "3", "1"},
                getPosValue(POC.DEFAULT_POC, cwsModel));
    }

    @Test
    public void loadSegPosModel() throws IOException {
        CwsModel cwsModel = new CwsModel(SEG_POS_WEIGHTS_PATH, SEG_POS_FEATURES_PATH);
        assertEquals(961470, cwsModel.featureSize);
        assertEquals(96, cwsModel.labelSize);
        assertEquals(-10615, cwsModel.llWeights[0]);
        assertEquals(5481, cwsModel.flWeights[0]);

        assertArrayEquals(new String[]{"3w"}, getPosValue(POC.PUNCTUATION_POC, cwsModel));
        assertArrayEquals(new String[]{"0m"}, getPosValue(POC.BEGIN_M_POC, cwsModel));
        assertArrayEquals(new String[]{"1m"}, getPosValue(POC.MIDDLE_M_POC, cwsModel));
        assertArrayEquals(new String[]{"2m"}, getPosValue(POC.END_M_POC, cwsModel));
        assertArrayEquals(new String[]{"3m"}, getPosValue(POC.SINGLE_M_POC, cwsModel));
        assertArrayEquals(
                new String[]{"0v", "0n", "0ns", "0t", "0f", "0d", "0m", "0q", "0r", "0j", "0s", "0a",
                        "0id", "0ni", "0p", "0c", "0np", "0nz", "0w", "0u", "0o", "0x", "0e", "0k"},
                getPosValue(POC.BEGIN_POC, cwsModel));
        assertArrayEquals(
                new String[]{"1n", "1ns", "1t", "1v", "1m", "1j", "1id", "1ni", "1c", "1np", "1d", "1a",
                        "1nz", "1w", "1q", "1s", "1f", "1r", "1x", "1o", "1p", "1e", "1u", "1k"},
                getPosValue(POC.MIDDLE_POC, cwsModel));
        assertArrayEquals(
                new String[]{"2v", "2n", "2ns", "2t", "2f", "2d", "2m", "2q", "2r", "2j", "2s", "2a", "2id",
                        "2ni", "2p", "2c", "2np", "2nz", "2w", "2u", "2o", "2x", "2e", "2k"},
                getPosValue(POC.END_POC, cwsModel));
        assertArrayEquals(
                new String[]{"3p", "3v", "3w", "3f", "3u", "3a", "3c", "3g", "3m", "3q", "3d", "3n", "3r",
                        "3j", "3np", "3x", "3k", "3o", "3e", "3h", "3t", "3ni", "3s", "3nz"},
                getPosValue(POC.SINGLE_POC, cwsModel));
        assertArrayEquals(
                new String[]{"0v", "3p", "0n", "3v", "3w", "0ns", "0t", "0f", "0d", "3f", "3u", "0m", "0q", "0r",
                        "0j", "0s", "3a", "3c", "3g", "3m", "3q", "3d", "3n", "0a", "0id", "3r", "0ni", "0p", "0c",
                        "0np", "3j", "3np", "3x", "0nz", "0w", "0u", "3k", "3o", "0o", "0x", "3e", "3h", "3t", "0e",
                        "3ni", "3s", "3nz", "0k"},
                getPosValue(POC.BS_POC, cwsModel));
        assertArrayEquals(
                new String[]{"2v", "3p", "2n", "3v", "3w", "2ns", "2t", "2f", "2d", "3f", "3u", "2m", "2q", "2r",
                        "2j", "2s", "3a", "3c", "3g", "3m", "3q", "3d", "3n", "2a", "2id", "3r", "2ni", "2p", "2c",
                        "2np", "3j", "3np", "3x", "2nz", "2w", "2u", "3k", "3o", "2o", "2x", "3e", "3h", "3t", "2e",
                        "3ni", "3s", "3nz", "2k"},
                getPosValue(POC.ES_POC, cwsModel));
    }

    /**
     * 根据POS 得到对应的所有label
     *
     * @param pos      enum POC 值
     * @param cwsModel CwsModel 对象
     * @return pos 对应的所有label
     */
    private String[] getPosValue(POC pos, CwsModel cwsModel) {
        return Arrays.stream(cwsModel.allowTabular[pos.ordinal()])
                .mapToObj(t -> cwsModel.labelValues[t])
                .toArray(String[]::new);
    }

    @Test
    public void evaluateSentenceWeights() throws IOException {
        CwsModel cwsModel = new CwsModel(SEG_ONLY_WEIGHTS_PATH, SEG_ONLY_FEATURES_PATH);
        String[] sentences = new String[]{
                "鲜",
                "两块五一套，",
                "AT&T是"
        };
        String[] expectedWeights = new String[]{
                "[[2401, -281, -2664, 547]]",
                "[[-4384, -14282, 21415, -2743], [-22789, 1568, 24039, -2808], [21771, -3627, -11546, -6585], " +
                        "[-13779, -3998, 7844, 9945], [-30721, 19768, 18906, -7940], [-9648, -22974, 40833, -8202]]",
                "[[1857, -32, -4633, 2815], [2091, -8697, -8749, 15367], [-8248, 4248, -4578, 8591], [636, 15227, " +
                        "-10528, -5327], [-16878, -5630, 22574, -60]]",
        };

        for (int i = 0; i < sentences.length; i++) {
            Ruler.CleanedSentence cleanedSentence = Ruler.ruleClean(sentences[i], true);
            char[] chars = cleanedSentence.getCleanedSentence();
            int[][] weights = cwsModel.evaluateSentenceWeights(chars);
            assertEquals(expectedWeights[i], Arrays.deepToString(weights));
        }

        cwsModel = new CwsModel(SEG_POS_WEIGHTS_PATH, SEG_POS_FEATURES_PATH);
        String[] expected0Weights = new String[]{
                "[-2405, -1923, -577, 5888, -2766, -6529, 338, 0, -1958, -1213, 5171, 0, 0, 0, 0, 0, 0, -1946, 0, " +
                        "-997, 0, -919, 0, 0, 0, 0, 0, 0, -1713, 2141, -999, -435, 0, 12863, 0, 1074, -4387, 0, " +
                        "-1926, -2411, 4628, 2363, 3102, -2073, 1346, -910, 0, 0, 0, 0, -796, 0, 0, 0, -703, 914, " +
                        "345, -1841, 0, 0, 162, 0, 261, -1514, 368, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                        "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]",
                "[-5754, -4934, -1939, 813, -10331, -4395, -1417, 940, -3822, -4856, -997, 166, -5368, 0, 7178, 0, " +
                        "979, -1682, -1127, -639, 0, 9709, 3087, 8436, 3389, -957, 7075, -997, 6760, 884, -5098, " +
                        "3892, 0, 1710, -943, -7110, 31462, 5834, -472, 3806, -577, -520, 1626, -3899, 1147, -4558, " +
                        "-1971, -763, 0, 0, -4128, -985, 0, 4129, -1399, -2816, -2425, -1704, -821, -775, -1731, 0, " +
                        "-5751, -1934, -3221, 0, 0, 0, -147, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0," +
                        " 0, 0, 0, 0, 0, 0, 0, 0]",
                "[-5660, -5256, 3821, -8860, -1044, -5284, 12803, -1945, 740, -867, -995, 0, -2462, -1829, 0, 0, " +
                        "-987, 0, 0, -1359, 0, -2864, -8599, 157, -953, -1887, 0, 0, -1068, 1780, -1170, 0, 0, -1666," +
                        " -2384, -4576, 2046, -4508, 1849, -2645, -4410, -2669, -2860, -3859, 0, -980, 4055, 3228, " +
                        "2791, 0, 0, -996, 0, 0, 348, -665, 371, 2014, 0, -749, 1807, 13988, 2363, 3601, 6876, 0, 0, " +
                        "0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 13543, 5615, 2311, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                        "0, 0]",
        };
        for (int i = 0; i < sentences.length; i++) {
            Ruler.CleanedSentence cleanedSentence = Ruler.ruleClean(sentences[i], true);
            char[] chars = cleanedSentence.getCleanedSentence();
            int[][] weights = cwsModel.evaluateSentenceWeights(chars);
            assertEquals(expected0Weights[i], Arrays.toString(weights[0]));
        }
    }
}