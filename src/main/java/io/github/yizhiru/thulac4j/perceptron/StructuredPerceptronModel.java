package io.github.yizhiru.thulac4j.perceptron;

import io.github.yizhiru.thulac4j.common.DoubleArrayTrie;
import io.github.yizhiru.thulac4j.util.IOUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static io.github.yizhiru.thulac4j.common.DoubleArrayTrie.MATCH_FAILURE_INDEX;
import static io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel.NGramFeature.*;

/**
 * 结构感知器模型.
 */
public final class StructuredPerceptronModel implements Serializable {

	private static final long serialVersionUID = -5324153272692800765L;

	/**
	 * Label数量.
	 */
	public final int labelSize;

	/**
	 * 特征数量.
	 */
	public final int featureSize;

	/**
	 * label转移到label的权重.
	 */
	public final int[] llWeights;

	/**
	 * 特征对应某label的权重.
	 * Why use one-dimensional array but not two-dimensional array? Please Refer to
	 * <a>
	 * https://stackoverflow.com/questions/2512082/java-multi-dimensional-array-vs-one-dimensional
	 * </a>
	 */
	public final int[] flWeights;

	/**
	 * Feature DAT
	 */
	private final DoubleArrayTrie featureDat;

	/**
	 * Label, 对应于cws_label.txt 或者 model_c_label.txt
	 */
	public final String[] labelValues;

	/**
	 * 映射 enum POC 对应的所有label 索引值.
	 * 其中，行为POC的ordinal值，列为索引值
	 */
	public final int[][] allowTabular;

	/**
	 * 加载训练模型
	 *
	 * @param weightInput  label转移权重、特征label权重, cws_model.bin
	 *                     *                    或者model_c_model.bin
	 * @param featureInput 特征DAT cws_dat.bin 或者 model_c_dat.bin
	 * @param labelInput   label
	 * @throws IOException if an I/O error occurs
	 */
	public StructuredPerceptronModel(InputStream weightInput, InputStream featureInput, InputStream labelInput) throws IOException {
		// Own all streams before validation so failures close every supplied resource.
		try (InputStream weights = weightInput; InputStream features = featureInput; InputStream labels = labelInput) {
			if (weights == null || features == null || labels == null) {
				throw new IOException("Missing model weights, features or labels; see README.md for model setup");
			}
			labelValues = IOUtils.readLines(labels).toArray(new String[0]);
			HashSet<String> unique = new HashSet<>();
			for (String label : labelValues) {
				if (label.isEmpty() || label.charAt(0) < '0' || label.charAt(0) > '3' || !unique.add(label)) {
					throw new IOException("Invalid or duplicate model label: " + label);
				}
			}
			DataInputStream input = new DataInputStream(new BufferedInputStream(weights));
			labelSize = IOUtils.readLittleEndianInt(input);
			featureSize = IOUtils.readLittleEndianInt(input);
			if (labelSize <= 0 || labelSize != labelValues.length || featureSize < 0) {
				throw new IOException("Invalid model dimensions or label count");
			}
			int transitionCount = IOUtils.checkedArrayLength(labelSize, labelSize);
			int featureCount = IOUtils.checkedArrayLength(featureSize, labelSize);
			llWeights = IOUtils.readLittleEndianInts(input, transitionCount);
			flWeights = IOUtils.readLittleEndianInts(input, featureCount);
			if (input.read() != -1) throw new IOException("Unexpected trailing model weights");
			featureDat = readFeatureDat(features);
		}

		// 记录label 集合，能与allowTabular 映射起来
		List<List<Integer>> posTags = getPosTags();

		// allowTabular 表示enum POC 对应的所有允许label，比如：
		// PUNCTUATION_POC 对应的允许label为 3 或 3w，
		// BEGIN_POC 对应的允许label为 0 或 0打头的label
		allowTabular = new int[12][];
		for (int i = 0; i < labelValues.length; i++) {
			// punctuation
			if ("3".equals(labelValues[i]) || "3w".equals(labelValues[i])) {
				allowTabular[0] = new int[]{i};
			}
			// single of numeral
			if ("3".equals(labelValues[i]) || "3m".equals(labelValues[i])) {
				allowTabular[4] = new int[]{i};
			}
			// begin of numeral
			else if ("0".equals(labelValues[i]) || "0m".equals(labelValues[i])) {
				allowTabular[1] = new int[]{i};
			}
			// middle of numeral
			else if ("1".equals(labelValues[i]) || "1m".equals(labelValues[i])) {
				allowTabular[2] = new int[]{i};
			}
			// end of numeral
			else if ("2".equals(labelValues[i]) || "2m".equals(labelValues[i])) {
				allowTabular[3] = new int[]{i};
			}
		}
		int[] indices = {1, 2, 4, 8, 9, 12, 15};
		for (int i = 0; i < indices.length; i++) {
			allowTabular[i + 5] = posTags.get(indices[i])
					.stream()
					.mapToInt(x -> x)
					.toArray();
		}
		for (int[] allowed : allowTabular) {
			if (allowed == null || allowed.length == 0) {
				throw new IOException("Model labels do not cover all required character positions");
			}
		}
	}

	/** Decode interleaved DAT pairs directly; no full-file byte or interleaved int copy. */
	private static DoubleArrayTrie readFeatureDat(InputStream stream) throws IOException {
		DataInputStream input = new DataInputStream(new BufferedInputStream(stream));
		int capacity = 4096;
		if (stream instanceof FileInputStream) {
			java.nio.channels.FileChannel channel = ((FileInputStream) stream).getChannel();
			long remaining = channel.size() - channel.position();
			if (remaining % 8 != 0 || remaining / 8 > Integer.MAX_VALUE - 8) {
				throw new IOException("Invalid feature DAT byte length: " + remaining);
			}
			capacity = (int) Math.max(1, remaining / 8);
		}
		int[] base = new int[capacity];
		int[] check = new int[capacity];
		int size = 0;
		byte[] buffer = new byte[32768];
		IntBuffer integers = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		while (true) {
			int bytes = 0;
			int count;
			while (bytes < buffer.length && (count = input.read(buffer, bytes, buffer.length - bytes)) != -1) {
				bytes += count;
			}
			if (bytes == 0) break;
			if (bytes % 8 != 0) throw new IOException("Truncated feature DAT pair");
			long required = (long) size + bytes / 8;
			if (required > Integer.MAX_VALUE - 8) throw new IOException("Feature DAT is too large");
			if (required > base.length) {
				capacity = (int) Math.min(Integer.MAX_VALUE - 8L, Math.max(required, (long) base.length * 2));
				base = Arrays.copyOf(base, capacity);
				check = Arrays.copyOf(check, capacity);
			}
			integers.position(0);
			for (int i = 0; i < bytes / 8; i++) {
				base[size] = integers.get();
				check[size++] = integers.get();
			}
		}

		if (size == 0) throw new IOException("Empty feature DAT");
		return size == base.length ? new DoubleArrayTrie(base, check) : new DoubleArrayTrie(base, check, size);
	}

	/**
	 * 计算所有可能label 索引值集合，以二维数组表示
	 *
	 * @return 索引值二维数组
	 */
	private List<List<Integer>> getPosTags() {
		List<List<Integer>> posTagsList = new ArrayList<>();
		int defaultSize = 16;
		for (int i = 0; i < defaultSize; i++) {
			posTagsList.add(new ArrayList<>());
		}
		for (int i = 0; i < labelValues.length; i++) {
			int segIndex = labelValues[i].charAt(0) - '0';
			for (int j = 0; j < defaultSize; j++) {
				if (((1 << segIndex) & j) != 0) {
					posTagsList.get(j).add(i);
				}
			}
		}
		return posTagsList;
	}

	/**
	 * 训练模型文件中POC对应的标识.
	 */
	public static final class PocMark {
		/**
		 * 对应于 POC B 的char.
		 */
		public static final Character POS_B_CHAR = '0';

		/**
		 * 对应于 POC M 的char.
		 */
		public static final Character POS_M_CHAR = '1';

		/**
		 * 对应于 POC E 的char.
		 */
		public static final Character POS_E_CHAR = '2';

		/**
		 * 对应于 POC B 的char.
		 */
		public static final Character POS_S_CHAR = '3';
	}

	/**
	 * N-gram 特征.
	 * THULAC采用的分词模型为结构化感知器（Structured Perceptron, SP），以最大熵准则
	 * 建模序列标注的得分函数.
	 */
	public static class NGramFeature {

		/**
		 * 超越边界的统一字符'#'
		 */
		public static final char BOUNDARY = 65283;

		/**
		 * feature的一部分
		 */
		public static final char SPACE = ' ';

		/**
		 * Unigram 特征种类1，对应于特征 mid + SPACE + '1'，即标注对应的当前字符
		 */
		public static final char UNIGRAM_FEATURE_1 = '1';

		/**
		 * Unigram 特征种类2，对应于特征 left + SPACE + '2'，即标注的前一字符
		 */
		public static final char UNIGRAM_FEATURE_2 = '2';

		/**
		 * Unigram 特征种类3，对应于特征 right + SPACE + '3'，即标注的后一字符
		 */
		public static final char UNIGRAM_FEATURE_3 = '3';

		/**
		 * Bigram 特征种类1，对应于特征 left + mid + SPACE + '1'，
		 * 即标注的前一字符加上当前字符
		 */
		public static final char BIGRAM_FEATURE_1 = '1';

		/**
		 * Bigram 特征种类2，对应于特征 mid + right + SPACE + '2'，
		 * 即标注对应的当前字符加上后一字符
		 */
		public static final char BIGRAM_FEATURE_2 = '2';

		/**
		 * Bigram 特征种类3，对应于特征 left2 + left1 + SPACE + '3'，
		 * 即标注的前二字符加上前一字符
		 */
		public static final char BIGRAM_FEATURE_3 = '3';

		/**
		 * Bigram 特征种类4，对应于特征 right + right2 + SPACE + '4'，
		 * 即标注的后一字符加上后二字符.
		 */
		public static final char BIGRAM_FEATURE_4 = '4';
	}

	/**
	 * 寻找Unigram特征对应于DAT中的base.
	 *
	 * @param ch   字符
	 * @param mark 标识属于3种特征中的一种: '1', '2', '3'
	 * @return 若存在则返回base，否则则返回-1
	 */
	private int findUnigramFeat(char ch, char mark) {
		int index = (int) ch;
		index = featureDat.transition(index, SPACE);
		index = featureDat.transition(index, mark);
		if (index == MATCH_FAILURE_INDEX) {
			return MATCH_FAILURE_INDEX;
		}
		return featureDat.getBaseByIndex(index);
	}

	/**
	 * 寻找Bigram特征对应于DAT中的base
	 *
	 * @param c1   第一个字符
	 * @param c2   第二个字符
	 * @param mark 标识属于4种特征中的一种: '1', '2', '3', '4'
	 * @return 若存在则返回对应的base值，否则返回-1
	 */
	private int findBigramFeat(char c1, char c2, char mark) {
		int index1 = (int) c1;
		int index2 = (int) c2;
		int index = featureDat.transition(index1, index2);
		index = featureDat.transition(index, SPACE);
		index = featureDat.transition(index, mark);
		if (index == MATCH_FAILURE_INDEX) {
			return MATCH_FAILURE_INDEX;
		}
		return featureDat.getBaseByIndex(index);
	}

	/**
	 * 根据featureDAT的base值，更新特征权重之和数组
	 *
	 * @param weights      label权重之和数组
	 * @param base         featureDAT base值
	 * @param labelIndices 允许POS 索引值
	 */
	private void addWeights(int[] weights, int base, int[] labelIndices) {
		if (base < 0 || base >= featureSize) {
			throw new IllegalStateException("Feature DAT index outside model weights: " + base);
		}
		int offset = base * labelSize;
		for (int i : labelIndices) {
			weights[i] += flWeights[offset + i];
		}
	}

	/**
	 * 根据前后一起的五个字符，计算加权特征权重之和数组
	 *
	 * @param left2        前二字符
	 * @param left1        前一字符
	 * @param mid          当前字符
	 * @param right1       后一字符
	 * @param right2       后二字符
	 * @param labelIndices 允许label 索引值
	 * @return 一维数组，表示当前字符的各label对应的特征权值加权之和
	 */
	public int[] evaluateCharWeights(
			char left2,
			char left1,
			char mid,
			char right1,
			char right2,
			int[] labelIndices) {
		int[] weights = new int[labelSize];
		evaluateCharWeights(left2, left1, mid, right1, right2, labelIndices, weights);
		return weights;
	}

	/** Fills a caller-owned buffer; safe for concurrent calls using distinct buffers. */
	public void evaluateCharWeights(char left2, char left1, char mid, char right1, char right2,
	                                int[] labelIndices, int[] weights) {
		if (weights.length != labelSize) throw new IllegalArgumentException("Invalid weights buffer size");
		Arrays.fill(weights, 0);
		int base;
		if ((base = findUnigramFeat(mid, UNIGRAM_FEATURE_1)) != MATCH_FAILURE_INDEX) {
			addWeights(weights, base, labelIndices);
		}
		if ((base = findUnigramFeat(left1, UNIGRAM_FEATURE_2)) != MATCH_FAILURE_INDEX) {
			addWeights(weights, base, labelIndices);
		}
		if ((base = findUnigramFeat(right1, UNIGRAM_FEATURE_3)) != MATCH_FAILURE_INDEX) {
			addWeights(weights, base, labelIndices);
		}
		if ((base = findBigramFeat(left1, mid, BIGRAM_FEATURE_1)) != MATCH_FAILURE_INDEX) {
			addWeights(weights, base, labelIndices);
		}
		if ((base = findBigramFeat(mid, right1, BIGRAM_FEATURE_2)) != MATCH_FAILURE_INDEX) {
			addWeights(weights, base, labelIndices);
		}
		if ((base = findBigramFeat(left2, left1, BIGRAM_FEATURE_3)) != MATCH_FAILURE_INDEX) {
			addWeights(weights, base, labelIndices);
		}
		if ((base = findBigramFeat(right1, right2, BIGRAM_FEATURE_4)) != MATCH_FAILURE_INDEX) {
			addWeights(weights, base, labelIndices);
		}
	}
}
