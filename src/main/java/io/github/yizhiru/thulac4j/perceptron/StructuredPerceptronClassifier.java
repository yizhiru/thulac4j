package io.github.yizhiru.thulac4j.perceptron;

import io.github.yizhiru.thulac4j.term.AnnotatedTerms;
import io.github.yizhiru.thulac4j.term.POC;

import java.util.Arrays;
import java.util.Objects;

public final class StructuredPerceptronClassifier {
	private final StructuredPerceptronModel model;

	public StructuredPerceptronClassifier(StructuredPerceptronModel model) {
		this.model = Objects.requireNonNull(model, "model");
	}

	/** Viterbi decoding with rolling scores and a flat primitive backtrace table. */
	public int[] classify(AnnotatedTerms annotatedTerms, int[][] previousTransition) {
		int len = annotatedTerms.getAnnotatedLength();
		int[] bestPath = new int[len];
		if (len == 0) return bestPath;
		int labelSize = model.labelSize;
		if (previousTransition.length != labelSize) {
			throw new IllegalArgumentException("Transition table does not match model labels");
		}
		long cells = (long) len * labelSize;
		if (cells > Integer.MAX_VALUE - 8) {
			throw new IllegalArgumentException("Text is too long for the model's backtrace table");
		}
		int[] previousLabels = new int[(int) cells];
		Arrays.fill(previousLabels, -1);
		long[] previousScores = new long[labelSize];
		long[] currentScores = new long[labelSize];
		int[] weights = new int[labelSize];
		char[] chars = annotatedTerms.appendBoundaryAround();
		POC[] pocs = annotatedTerms.getPocs();

		for (int i = 0; i < len; i++) {
			Arrays.fill(currentScores, Long.MIN_VALUE);
			int[] allowed = model.allowTabular[pocs[i].ordinal()];
			model.evaluateCharWeights(chars[i], chars[i + 1], chars[i + 2], chars[i + 3],
					chars[i + 4], allowed, weights);
			boolean reachable = false;
			for (int label : allowed) {
				if (i == 0) {
					currentScores[label] = weights[label];
					reachable = true;
					continue;
				}
				long best = Long.MIN_VALUE;
				int predecessor = -1;
				for (int pre : previousTransition[label]) {
					if (previousScores[pre] == Long.MIN_VALUE) continue;
					long score = previousScores[pre] + model.llWeights[pre * labelSize + label];
					if (predecessor < 0 || score > best) {
						best = score;
						predecessor = pre;
					}
				}
				if (predecessor >= 0) {
					currentScores[label] = best + weights[label];
					previousLabels[i * labelSize + label] = predecessor;
					reachable = true;
				}
			}
			if (!reachable) throw new IllegalStateException("No valid label path at character " + i);
			long[] swap = previousScores;
			previousScores = currentScores;
			currentScores = swap;
		}
		int last = -1;
		for (int label : model.allowTabular[pocs[len - 1].ordinal()]) {
			if (previousScores[label] != Long.MIN_VALUE
					&& (last < 0 || previousScores[label] > previousScores[last])) last = label;
		}
		bestPath[len - 1] = last;
		for (int i = len - 1; i > 0; i--) {
			bestPath[i - 1] = previousLabels[i * labelSize + bestPath[i]];
		}
		return bestPath;
	}

	public String[] getLabelValues() {
		return model.labelValues.clone();
	}
}
