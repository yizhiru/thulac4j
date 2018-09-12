package io.github.yizhiru.thulac4j.perceptron;

import io.github.yizhiru.thulac4j.process.RuleAnnotator;
import io.github.yizhiru.thulac4j.term.POC;
import io.github.yizhiru.thulac4j.term.AnnotatedTerms;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static io.github.yizhiru.thulac4j.SPChineseTokenizerTest.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class StructuredPerceptronModelTest {

	@Test
	public void loadSegmenterModel() throws IOException {
		StructuredPerceptronModel SPModel = new StructuredPerceptronModel(
				new FileInputStream(SEG_WEIGHTS_PATH),
				new FileInputStream(SEG_FEATURES_PATH),
				new FileInputStream(SEG_LABELS_PATH));
		assertEquals(2453880, SPModel.featureSize);
		assertEquals(4, SPModel.labelSize);
		assertEquals(-42717, SPModel.llWeights[0]);
		assertEquals(-4958, SPModel.flWeights[0]);

		assertArrayEquals(new String[]{"3"}, getPosValue(POC.SINGLE_NUMERAL_POC, SPModel));
		assertArrayEquals(new String[]{"0"}, getPosValue(POC.BEGIN_POC, SPModel));
		assertArrayEquals(new String[]{"1"}, getPosValue(POC.MIDDLE_POC, SPModel));
		assertArrayEquals(new String[]{"2"}, getPosValue(POC.END_POC, SPModel));
		assertArrayEquals(new String[]{"3"}, getPosValue(POC.SINGLE_POC, SPModel));
		assertArrayEquals(new String[]{"0", "3"}, getPosValue(POC.BEGIN_OR_SINGLE_POC, SPModel));
		assertArrayEquals(new String[]{"2", "3"}, getPosValue(POC.END_OR_SINGLE_POC, SPModel));
		assertArrayEquals(
				new String[]{"0", "2", "3", "1"},
				getPosValue(POC.DEFAULT_POC, SPModel));
	}

	@Test
	public void loadPosModel() throws IOException {
		StructuredPerceptronModel SPModel = new StructuredPerceptronModel(
				new FileInputStream(POS_WEIGHTS_PATH),
				new FileInputStream(POS_FEATURES_PATH),
				new FileInputStream(POS_LABELS_PATH));
		assertEquals(961470, SPModel.featureSize);
		assertEquals(96, SPModel.labelSize);
		assertEquals(-10615, SPModel.llWeights[0]);
		assertEquals(5481, SPModel.flWeights[0]);

		assertArrayEquals(new String[]{"3w"}, getPosValue(POC.PUNCTUATION_POC, SPModel));
		assertArrayEquals(new String[]{"0m"}, getPosValue(POC.BEGIN_NUMERAL_POC, SPModel));
		assertArrayEquals(new String[]{"1m"}, getPosValue(POC.MIDDLE_NUMERAL_POC, SPModel));
		assertArrayEquals(new String[]{"2m"}, getPosValue(POC.END_NUMERAL_POC, SPModel));
		assertArrayEquals(new String[]{"3m"}, getPosValue(POC.SINGLE_NUMERAL_POC, SPModel));
		assertArrayEquals(
				new String[]{"0v", "0n", "0ns", "0t", "0f", "0d", "0m", "0q", "0r", "0j", "0s", "0a",
						"0id", "0ni", "0p", "0c", "0np", "0nz", "0w", "0u", "0o", "0x", "0e", "0k"},
				getPosValue(POC.BEGIN_POC, SPModel));
		assertArrayEquals(
				new String[]{"1n", "1ns", "1t", "1v", "1m", "1j", "1id", "1ni", "1c", "1np", "1d", "1a",
						"1nz", "1w", "1q", "1s", "1f", "1r", "1x", "1o", "1p", "1e", "1u", "1k"},
				getPosValue(POC.MIDDLE_POC, SPModel));
		assertArrayEquals(
				new String[]{"2v", "2n", "2ns", "2t", "2f", "2d", "2m", "2q", "2r", "2j", "2s", "2a", "2id",
						"2ni", "2p", "2c", "2np", "2nz", "2w", "2u", "2o", "2x", "2e", "2k"},
				getPosValue(POC.END_POC, SPModel));
		assertArrayEquals(
				new String[]{"3p", "3v", "3w", "3f", "3u", "3a", "3c", "3g", "3m", "3q", "3d", "3n", "3r",
						"3j", "3np", "3x", "3k", "3o", "3e", "3h", "3t", "3ni", "3s", "3nz"},
				getPosValue(POC.SINGLE_POC, SPModel));
		assertArrayEquals(
				new String[]{"0v", "3p", "0n", "3v", "3w", "0ns", "0t", "0f", "0d", "3f", "3u", "0m", "0q", "0r",
						"0j", "0s", "3a", "3c", "3g", "3m", "3q", "3d", "3n", "0a", "0id", "3r", "0ni", "0p", "0c",
						"0np", "3j", "3np", "3x", "0nz", "0w", "0u", "3k", "3o", "0o", "0x", "3e", "3h", "3t", "0e",
						"3ni", "3s", "3nz", "0k"},
				getPosValue(POC.BEGIN_OR_SINGLE_POC, SPModel));
		assertArrayEquals(
				new String[]{"2v", "3p", "2n", "3v", "3w", "2ns", "2t", "2f", "2d", "3f", "3u", "2m", "2q", "2r",
						"2j", "2s", "3a", "3c", "3g", "3m", "3q", "3d", "3n", "2a", "2id", "3r", "2ni", "2p", "2c",
						"2np", "3j", "3np", "3x", "2nz", "2w", "2u", "3k", "3o", "2o", "2x", "3e", "3h", "3t", "2e",
						"3ni", "3s", "3nz", "2k"},
				getPosValue(POC.END_OR_SINGLE_POC, SPModel));
	}

	/**
	 * 根据POS 得到对应的所有label
	 *
	 * @param pos     enum POC 值
	 * @param SPModel StructuredPerceptronModel 对象
	 * @return pos 对应的所有label
	 */
	private String[] getPosValue(POC pos, StructuredPerceptronModel SPModel) {
		return Arrays.stream(SPModel.allowTabular[pos.ordinal()])
				.mapToObj(t -> SPModel.labelValues[t])
				.toArray(String[]::new);
	}

	@Test
	public void evaluateCharWeights() throws IOException {
		StructuredPerceptronModel SPModel = new StructuredPerceptronModel(
				new FileInputStream(SEG_WEIGHTS_PATH),
				new FileInputStream(SEG_FEATURES_PATH),
				new FileInputStream(SEG_LABELS_PATH));
		String[] sentences = new String[]{
				"鲜",
				"两块五一套，",
				"AT&T是"
		};
		String[] expectedWeights = new String[]{
				"[[0, 0, -2664, 0]]",
				"[[-4384, 0, 21415, 0], [-22789, 1568, 24039, -2808], [21771, -3627, -11546, -6585], [-13779, -3998, " +
						"7844, 9945], [0, 19768, 18906, 0], [0, 0, 40833, 0]]",
				"[[1857, 0, 0, 0], [0, 0, 0, 15367], [0, 0, 0, 8591], [0, 15227, 0, 0], [0, 0, 22574, 0]]",
		};

		for (int i = 0; i < sentences.length; i++) {
			AnnotatedTerms annotatedTerms = RuleAnnotator.annotate(sentences[i], true);
			char[] chars = annotatedTerms.appendBoundaryAround();

			POC[] pocs = annotatedTerms.getPocs();

			int[][] weights = new int[annotatedTerms.getAnnotatedLength()][];
			for (int j = 0; j < annotatedTerms.getAnnotatedLength(); j++) {
				int[] labelIndices = SPModel.allowTabular[pocs[j].ordinal()];
				weights[j] = SPModel.evaluateCharWeights(
						chars[j],
						chars[j + 1],
						chars[j + 2],
						chars[j + 3],
						chars[j + 4],
						labelIndices);
			}

			assertEquals(expectedWeights[i], Arrays.deepToString(weights));
		}

		SPModel = new StructuredPerceptronModel(
				new FileInputStream(POS_WEIGHTS_PATH),
				new FileInputStream(POS_FEATURES_PATH),
				new FileInputStream(POS_LABELS_PATH));
		String[] expected0Weights = new String[]{
				"[0, 0, -577, 0, 0, -6529, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -997, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0," +
						" 0, 0, 0, 12863, 0, 1074, -4387, 0, -1926, -2411, 0, 0, 0, 0, 0, -910, 0, 0, 0, 0, 0, 0, 0, " +
						"0, 0, 0, 0, -1841, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
						"0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]",
				"[-5754, 0, -1939, 813, 0, -4395, 0, 940, -3822, 0, 0, 166, 0, 0, 7178, 0, 979, 0, -1127, -639, 0, " +
						"9709, 0, 0, 3389, 0, 7075, 0, 6760, 0, 0, 3892, 0, 1710, -943, -7110, 31462, 5834, -472, " +
						"3806, -577, 0, 1626, 0, 0, -4558, -1971, 0, 0, 0, 0, -985, 0, 0, -1399, 0, 0, -1704, 0, " +
						"-775, 0, 0, -5751, 0, 0, 0, 0, 0, -147, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
						"0, 0, 0, 0, 0, 0, 0, 0, 0, 0]",
				"[-5660, 0, 0, -8860, 0, 0, 0, 0, 740, 0, 0, 0, 0, 0, 0, 0, -987, 0, 0, 0, 0, -2864, 0, 0, -953, 0, " +
						"0, 0, -1068, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -4410, 0, -2860, 0, 0, 0, 4055, 0, 0, 0, 0, " +
						"-996, 0, 0, 348, 0, 0, 0, 0, 0, 0, 0, 2363, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
						"13543, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]",
		};
		for (int i = 0; i < sentences.length; i++) {
			AnnotatedTerms annotatedTerms = RuleAnnotator.annotate(sentences[i], true);
			char[] chars = annotatedTerms.appendBoundaryAround();

			POC[] pocs = annotatedTerms.getPocs();
			int[] labelIndices = SPModel.allowTabular[pocs[0].ordinal()];
			int[] weights = SPModel.evaluateCharWeights(
					chars[0],
					chars[1],
					chars[2],
					chars[3],
					chars[4],
					labelIndices);
			assertEquals(expected0Weights[i], Arrays.toString(weights));
		}
	}
}