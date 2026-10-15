package io.github.yizhiru.thulac4j;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

/** Checks identical output from both benchmark artifacts without changing either version. */
public class CorpusSnapshot {
	public static void main(String[] args) throws Exception {
		String mode = args[0];
		SPChineseTokenizer tokenizer = "pos".equals(mode)
				? new POSTagger("models/model_c_model.bin", "models/model_c_dat.bin")
				: new SPChineseTokenizer(new FileInputStream("models/cws_model.bin"),
						new FileInputStream("models/cws_dat.bin"), new FileInputStream("models/cws_label.txt"));
		tokenizer.enableTitleWord();
		List<String> output = new ArrayList<>();
		List<String> sentences = Files.readAllLines(Paths.get("benchmarks/corpus.txt"), StandardCharsets.UTF_8);
		for (int i = 0; i < sentences.size(); i++) {
			output.add("corpus-" + i + "\t" + tokenizer.tokenize(sentences.get(i)).toString());
		}
		Files.write(Paths.get(args[1]), output, StandardCharsets.UTF_8);
	}
}
