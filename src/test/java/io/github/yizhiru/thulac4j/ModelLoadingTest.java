package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.common.DoubleArrayTrie;
import io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel;
import io.github.yizhiru.thulac4j.util.IOUtils;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;

public class ModelLoadingTest {
	@Rule public TemporaryFolder temporary = new TemporaryFolder();
	private static final class TrackingInput extends ByteArrayInputStream {
		boolean closed;
		TrackingInput(byte[] bytes) { super(bytes); }
		@Override public void close() { closed = true; }
	}

	@Test public void readsAcrossBufferBoundaries() throws Exception {
		int[] expected = new int[8197];
		Random random = new Random(42);
		for (int i = 0; i < expected.length; i++) expected[i] = random.nextInt();
		DataInputStream input = new DataInputStream(new ByteArrayInputStream(TestModels.ints(expected)));
		assertArrayEquals(expected, IOUtils.readLittleEndianInts(input, expected.length));
		assertEquals(-1, input.read());
	}

	@Test public void readsFileBackedFeatureDatAndRejectsPartialPairs() throws Exception {
		java.nio.file.Path path = temporary.newFile().toPath();
		java.nio.file.Files.write(path, TestModels.featureDat());
		StructuredPerceptronModel model = new StructuredPerceptronModel(
				new ByteArrayInputStream(TestModels.weights(new int[16], new int[]{1, 2, 3, 4})),
				new FileInputStream(path.toFile()), new ByteArrayInputStream(TestModels.labels()));
		assertArrayEquals(new int[]{1, 2, 3, 4}, model.evaluateCharWeights('甲', '乙', '中', '丙', '丁', new int[]{0, 1, 2, 3}));
		java.nio.file.Files.write(path, new byte[7]);
		assertThrows(IOException.class, () -> new StructuredPerceptronModel(
				new ByteArrayInputStream(TestModels.weights(new int[16], new int[0])),
				new FileInputStream(path.toFile()), new ByteArrayInputStream(TestModels.labels())));
	}

	@Test public void readsLittleEndianWeightsAndClearsReusedBuffer() throws Exception {
		StructuredPerceptronModel model = new StructuredPerceptronModel(
				new ByteArrayInputStream(TestModels.weights(new int[16], new int[]{-10, 20, 30, 40})),
				new ByteArrayInputStream(TestModels.featureDat()), new ByteArrayInputStream(TestModels.labels()));
		int[] buffer = new int[4];
		model.evaluateCharWeights('甲', '乙', '中', '丙', '丁', new int[]{0, 2}, buffer);
		assertArrayEquals(new int[]{-10, 0, 30, 0}, buffer);
		model.evaluateCharWeights('甲', '乙', '戊', '丙', '丁', new int[]{1, 3}, buffer);
		assertArrayEquals(new int[4], buffer);
	}

	@Test public void closesEveryModelStreamOnSuccessAndOnFailure() throws Exception {
		for (boolean truncated : new boolean[]{false, true}) {
			TrackingInput weights = new TrackingInput(truncated ? TestModels.ints(4, 0)
					: TestModels.weights(new int[16], new int[0]));
			TrackingInput features = new TrackingInput(TestModels.ints(0, -1));
			TrackingInput labels = new TrackingInput(TestModels.labels());
			if (truncated) assertThrows(IOException.class, () -> new StructuredPerceptronModel(weights, features, labels));
			else new StructuredPerceptronModel(weights, features, labels);
			assertTrue(weights.closed && features.closed && labels.closed);
		}
		TrackingInput features = new TrackingInput(new byte[0]);
		TrackingInput labels = new TrackingInput(TestModels.labels());
		assertThrows(IOException.class, () -> new StructuredPerceptronModel(null, features, labels));
		assertTrue(features.closed && labels.closed);
	}

	@Test public void rejectsInvalidHeadersLabelsAndTruncatedFeatures() {
		byte[] validWeights = TestModels.weights(new int[16], new int[0]);
		for (byte[] weights : new byte[][]{TestModels.ints(-1, 0), TestModels.ints(4, -1),
				TestModels.ints(4, Integer.MAX_VALUE), TestModels.ints(3, 0), Arrays.copyOf(validWeights, validWeights.length - 1),
				Arrays.copyOf(validWeights, validWeights.length + 1)}) {
			assertThrows(IOException.class, () -> load(weights, TestModels.ints(0, -1), TestModels.labels()));
		}
		for (byte[] features : new byte[][]{new byte[0], TestModels.ints(0), new byte[7], new byte[9]}) {
			assertThrows(IOException.class, () -> load(validWeights, features, TestModels.labels()));
		}
		for (String labels : new String[]{"0\n1\n2\n2\n", "0\n1\n2\n4\n", "0\n1\n2\n\n", "0x\n1x\n2x\n3x\n"}) {
			assertThrows(IOException.class, () -> load(validWeights, TestModels.ints(0, -1), labels.getBytes(StandardCharsets.UTF_8)));
		}
	}

	@Test public void validatesDatLengthAndClosesOwnedStream() {
		for (byte[] bytes : new byte[][]{new byte[0], TestModels.ints(-1), TestModels.ints(2, 0),
				TestModels.ints(1, 0, -1, 9), TestModels.ints(1, 0, 99)}) {
			TrackingInput input = new TrackingInput(bytes);
			assertThrows(UncheckedIOException.class, () -> DoubleArrayTrie.loadDat(input));
			assertTrue(input.closed);
		}
	}

	@Test public void readsUtf8AndReportsMissingResources() throws Exception {
		TrackingInput input = new TrackingInput("中国\n繁體\n".getBytes(StandardCharsets.UTF_8));
		assertEquals(Arrays.asList("中国", "繁體"), IOUtils.readLines(input));
		assertTrue(input.closed);
		IOException error = assertThrows(IOException.class, () -> IOUtils.openResource("/missing-model.bin"));
		assertTrue(error.getMessage().contains("/missing-model.bin"));
		assertThrows(IOException.class, () -> IOUtils.toIntArray(new ByteArrayInputStream(new byte[3])));
	}

	private static StructuredPerceptronModel load(byte[] weights, byte[] features, byte[] labels) throws IOException {
		return new StructuredPerceptronModel(new ByteArrayInputStream(weights), new ByteArrayInputStream(features),
				new ByteArrayInputStream(labels));
	}
}
