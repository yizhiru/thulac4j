package io.github.yizhiru.thulac4j;

import java.io.FileInputStream;
import java.util.Locale;

/** One tokenizer initialization in a fresh JVM; filesystem cache is not flushed. */
public class ModelLoadProbe {
	public static void main(String[] args) throws Exception {
		long start = System.nanoTime();
		SPChineseTokenizer tokenizer = "pos".equals(args[0])
				? new POSTagger("models/model_c_model.bin", "models/model_c_dat.bin")
				: new SPChineseTokenizer(new FileInputStream("models/cws_model.bin"),
						new FileInputStream("models/cws_dat.bin"), new FileInputStream("models/cws_label.txt"));
		double milliseconds = (System.nanoTime() - start) / 1_000_000.0;
		System.out.printf(Locale.ROOT, "{\"initialization_ms\":%.6f}%n", milliseconds);
		// Keep the initialized tokenizer observable, outside the timed region.
		if (tokenizer.tokenize("测试").isEmpty()) throw new AssertionError("Empty tokenizer output");
	}
}
