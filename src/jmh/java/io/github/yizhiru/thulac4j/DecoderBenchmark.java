package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.perceptron.*;
import io.github.yizhiru.thulac4j.process.RuleAnnotator;
import io.github.yizhiru.thulac4j.term.AnnotatedTerms;
import org.openjdk.jmh.annotations.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/** Synthetic decoder benchmark: measures decoding/allocation, not real-model accuracy. */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class DecoderBenchmark {
	@Param({"32", "512", "4096"}) public int length;
	@Param({"4", "96"}) public int labelCount;
	private StructuredPerceptronClassifier classifier;
	private AnnotatedTerms terms;
	private int[][] transitions;

	@Setup public void setup() throws IOException {
		ByteBuffer weights = ByteBuffer.allocate(8 + labelCount * labelCount * 4).order(ByteOrder.LITTLE_ENDIAN);
		weights.putInt(labelCount).putInt(0);
		Random random = new Random(1742);
		for (int i = 0; i < labelCount * labelCount; i++) weights.putInt(random.nextInt(2001) - 1000);
		StringBuilder labels = new StringBuilder();
		for (int tag = 0; tag < labelCount / 4; tag++) {
			String suffix = labelCount == 4 ? "" : tag == 0 ? "m" : tag == 1 ? "w" : "tag" + tag;
			for (int poc = 0; poc < 4; poc++) labels.append(poc).append(suffix).append('\n');
		}
		byte[] dat = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).putInt(-1).array();
		StructuredPerceptronModel model = new StructuredPerceptronModel(new ByteArrayInputStream(weights.array()),
				new ByteArrayInputStream(dat), new ByteArrayInputStream(labels.toString().getBytes(StandardCharsets.UTF_8)));
		classifier = new StructuredPerceptronClassifier(model);
		transitions = SPChineseTokenizer.setPreviousTransitions(model.labelValues);
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < length; i++) text.append('甲');
		terms = RuleAnnotator.annotate(text.toString(), false);
	}

	@Benchmark public int[] decode() { return classifier.classify(terms, transitions); }
}
