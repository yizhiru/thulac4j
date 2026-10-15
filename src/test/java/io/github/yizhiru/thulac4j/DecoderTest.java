package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.perceptron.*;
import io.github.yizhiru.thulac4j.process.RuleAnnotator;
import io.github.yizhiru.thulac4j.term.AnnotatedTerms;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.*;

import static org.junit.Assert.*;

public class DecoderTest {
	@Test public void matchesExhaustiveSearchOnShortSequences() throws Exception {
		Random random = new Random(1742);
		for (int trial = 0; trial < 80; trial++) {
			int[] transitions = new int[16];
			for (int i = 0; i < 16; i++) transitions[i] = random.nextInt(2001) - 1000;
			int[] emissions = new int[4];
			for (int i = 0; i < 4; i++) emissions[i] = random.nextInt(2001) - 1000;
			StructuredPerceptronModel model = new StructuredPerceptronModel(
					new ByteArrayInputStream(TestModels.weights(transitions, emissions)),
					new ByteArrayInputStream(TestModels.featureDat()), new ByteArrayInputStream(TestModels.labels()));
			int[][] previous = SPChineseTokenizer.setPreviousTransitions(model.labelValues);
			AnnotatedTerms terms = RuleAnnotator.annotate("中中中中中", false);
			int[] actual = new StructuredPerceptronClassifier(model).classify(terms, previous);
			long actualScore = score(actual, transitions, emissions);
			long best = Long.MIN_VALUE;
			for (int bits = 0; bits < (1 << 10); bits++) {
				int[] path = new int[5];
				for (int i = 0; i < 5; i++) path[i] = (bits >> (i * 2)) & 3;
				boolean valid = true;
				for (int i = 0; i < 5; i++) {
					valid &= contains(model.allowTabular[terms.getPocs()[i].ordinal()], path[i]);
					if (i > 0) valid &= contains(previous[path[i]], path[i - 1]);
				}
				if (valid) best = Math.max(best, score(path, transitions, emissions));
			}
			assertEquals(best, actualScore);
			assertTrue(contains(model.allowTabular[terms.getPocs()[0].ordinal()], actual[0]));
			for (int i = 1; i < actual.length; i++) assertTrue(contains(previous[actual[i]], actual[i - 1]));
		}
	}

	@Test public void retainsTieOrderAndDoesNotOverflowOnLongScores() throws Exception {
		StructuredPerceptronModel model = TestModels.model(new int[16]);
		int[][] previous = SPChineseTokenizer.setPreviousTransitions(model.labelValues);
		assertArrayEquals(new int[]{0, 2}, new StructuredPerceptronClassifier(model)
				.classify(RuleAnnotator.annotate("甲乙", false), previous));
		int[] transitions = new int[16];
		transitions[15] = Integer.MAX_VALUE;
		model = TestModels.model(transitions);
		assertArrayEquals(new int[]{3, 3, 3, 3}, new StructuredPerceptronClassifier(model)
				.classify(RuleAnnotator.annotate("甲乙丙丁", false), previous));
	}

	@Test public void reportsUnreachablePathsAndAcceptsEmptyInput() throws Exception {
		StructuredPerceptronModel model = TestModels.model(new int[16]);
		StructuredPerceptronClassifier classifier = new StructuredPerceptronClassifier(model);
		int[][] unreachable = {new int[0], new int[0], new int[0], new int[0]};
		assertThrows(IllegalStateException.class, () -> classifier.classify(RuleAnnotator.annotate("甲乙", false), unreachable));
		assertEquals(0, classifier.classify(RuleAnnotator.annotate("", false), unreachable).length);
	}

	private static boolean contains(int[] values, int value) {
		for (int candidate : values) if (candidate == value) return true;
		return false;
	}
	private static long score(int[] path, int[] transitions, int[] emissions) {
		long score = 0;
		for (int label : path) score += emissions[label];
		for (int i = 1; i < path.length; i++) score += transitions[path[i - 1] * 4 + path[i]];
		return score;
	}
}
