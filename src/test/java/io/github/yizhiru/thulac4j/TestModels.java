package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel;

import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;

/** Small deterministic models, independent of downloaded training artifacts. */
public final class TestModels {
	public static byte[] ints(int... values) {
		ByteBuffer bytes = ByteBuffer.allocate(values.length * 4).order(ByteOrder.LITTLE_ENDIAN);
		for (int value : values) bytes.putInt(value);
		return bytes.array();
	}

	public static byte[] weights(int[] transitions, int[] emissions) {
		int[] values = new int[2 + transitions.length + emissions.length];
		values[0] = 4;
		values[1] = emissions.length / 4;
		System.arraycopy(transitions, 0, values, 2, transitions.length);
		System.arraycopy(emissions, 0, values, 2 + transitions.length, emissions.length);
		return ints(values);
	}

	public static byte[] labels() { return "0\n1\n2\n3\n".getBytes(StandardCharsets.UTF_8); }

	public static StructuredPerceptronModel model(int[] transitions) throws IOException {
		return new StructuredPerceptronModel(new ByteArrayInputStream(weights(transitions, new int[0])),
				new ByteArrayInputStream(ints(0, -1)), new ByteArrayInputStream(labels()));
	}

	public static SPChineseTokenizer tokenizer() throws IOException {
		int[] transitions = new int[16];
		transitions[15] = 100;
		return new SPChineseTokenizer(model(transitions), TokenizerConfig.DEFAULT);
	}

	/** One feature: 中 + space + '1', with its terminal storing feature ID zero. */
	public static byte[] featureDat() {
		int[] values = new int[('中' + 1) * 2];
		for (int i = 1; i < values.length; i += 2) values[i] = -1;
		values[32 * 2 + 1] = '中';
		values[49 * 2 + 1] = 32;
		return ints(values);
	}
}
