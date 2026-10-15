package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.term.TokenItem;
import org.openjdk.jmh.annotations.*;
import java.util.List;
import java.io.FileInputStream;
import java.util.concurrent.TimeUnit;

/** End-to-end benchmark requiring the actual model files. Loading is outside measurement. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(2)
public class TokenizerBenchmark {
	@Param({"segment", "pos"}) public String mode;
	@Param({"short", "long", "mixed"}) public String textKind;
	private SPChineseTokenizer tokenizer;
	private String text;

	@Setup public void setup() throws Exception {
		tokenizer = "pos".equals(mode) ? new POSTagger("models/model_c_model.bin", "models/model_c_dat.bin")
				: new SPChineseTokenizer(new FileInputStream("models/cws_model.bin"),
						new FileInputStream("models/cws_dat.bin"), new FileInputStream("models/cws_label.txt"));
		String sentence = "中文分词与词性标注用于自然语言处理。";
		text = sentence;
		if ("mixed".equals(textKind)) text = "2026年10月14日发布 Java API，支持 UTF-8、繁體中文以及 3.14 元。";
		if ("long".equals(textKind)) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < 200; i++) builder.append(sentence);
			text = builder.toString();
		}
	}

	@Benchmark public List<TokenItem> tokenize() { return tokenizer.tokenize(text); }
}
