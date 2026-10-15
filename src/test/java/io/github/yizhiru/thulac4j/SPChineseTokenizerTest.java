package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.util.IOUtils;

import org.junit.Test;

import java.io.FileInputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class SPChineseTokenizerTest {

	/**
	 * Segmenter weights model path.
	 */
	public static final String SEG_WEIGHTS_PATH = "models/cws_model.bin";

	/**
	 * Segmenter features path.
	 */
	public static final String SEG_FEATURES_PATH = "models/cws_dat.bin";

	public static final String SEG_LABELS_PATH = "models/cws_label.txt";

	/**
	 * POSTagger weights model path.
	 */
	public static final String POS_WEIGHTS_PATH = "models/model_c_model.bin";

	/**
	 * POSTagger features path.
	 */
	public static final String POS_FEATURES_PATH = "models/model_c_dat.bin";

	public static final String POS_LABELS_PATH = "models/model_c_label.txt";

	@Test
	public void setPreviousTrans() throws Exception {
		int[][] previousTrans = SPChineseTokenizer.setPreviousTransitions(
				IOUtils.readLines(new FileInputStream(SEG_LABELS_PATH)).toArray(new String[0]));

		assertEquals("[[1, 2], [0, 3], [1, 2], [0, 3]]",
				Arrays.deepToString(previousTrans));

		previousTrans = SPChineseTokenizer.setPreviousTransitions(
				IOUtils.readLines(new FileInputStream(POS_LABELS_PATH)).toArray(new String[0]));
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
