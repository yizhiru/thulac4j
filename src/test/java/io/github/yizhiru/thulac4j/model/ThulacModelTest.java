package io.github.yizhiru.thulac4j.model;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ThulacModelTest {

    @Test
    public void segOnly() throws IOException {
        ThulacModel thulac = new ThulacModel("train/cws_model.bin",
                "train/cws_dat.bin");
        assertEquals(2453880, thulac.featureSize);
        assertEquals(4, thulac.labelSize);
        assertEquals(-42717, thulac.llWeights[0]);
        assertEquals(-4958, thulac.flWeights[0]);
        assertEquals(7643071, thulac.featureDatSize);
        assertEquals(51, thulac.featureDat[5]);

        CwsModel cwsModel = thulac.convert2CwsModel();
        assertEquals(-20126, cwsModel.llWeights[2][3]);
        assertEquals(-14039, cwsModel.llWeights[3][0]);
        assertEquals(0, cwsModel.flWeights[2323572][0]);
        assertEquals(984, cwsModel.flWeights[245389][3]);

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
    public void segPos() throws IOException {
        ThulacModel thulac = new ThulacModel("train/model_c_model.bin",
                "train/model_c_dat.bin");
        assertEquals(961470, thulac.featureSize);
        assertEquals(96, thulac.labelSize);
        assertEquals(-10615, thulac.llWeights[0]);
        assertEquals(5481, thulac.flWeights[0]);
        assertEquals(2969081, thulac.featureDatSize);
        assertEquals(51, thulac.featureDat[5]);

        CwsModel cwsModel = thulac.convert2CwsModel();
        assertEquals(-12070, cwsModel.llWeights[3][0]);
        assertEquals(0, cwsModel.flWeights[961469][2]);
        assertEquals(-3538, cwsModel.flWeights[245389][3]);

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
}
