package io.github.yizhiru.thulac4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** The repository's 117-sentence regression corpus; reported operations are sentences. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class CorpusBenchmark {
	@Param({"segment", "pos"}) public String mode;
	private SPChineseTokenizer tokenizer;
	private List<String> sentences;

	@Setup public void setup() throws Exception {
		tokenizer = "pos".equals(mode) ? new POSTagger("models/model_c_model.bin", "models/model_c_dat.bin")
				: new SPChineseTokenizer(new FileInputStream("models/cws_model.bin"),
						new FileInputStream("models/cws_dat.bin"), new FileInputStream("models/cws_label.txt"));
		tokenizer.enableTitleWord();
		sentences = Files.readAllLines(Paths.get("benchmarks/corpus.txt"), StandardCharsets.UTF_8);
		if (sentences.size() != 117) throw new IllegalStateException("Update OperationsPerInvocation when changing the corpus");
	}

	@Benchmark
	@OperationsPerInvocation(117)
	public void tokenizeCorpus(Blackhole blackhole) {
		for (String sentence : sentences) blackhole.consume(tokenizer.tokenize(sentence));
	}
}
