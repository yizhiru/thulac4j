package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.term.TokenItem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static io.github.yizhiru.thulac4j.util.ModelPaths.POS_TAGGING_LABEL_PATH;

/**
 * 中文词性标注.
 */
public class POSTagger extends SPChineseTokenizer {

	public POSTagger(String weightPath, String featurePath) throws IOException {
		super(new FileInputStream(weightPath),
				new FileInputStream(featurePath),
				POSTagger.class.getResourceAsStream(POS_TAGGING_LABEL_PATH));
	}

	/**
	 * 词性标注
	 *
	 * @param text 输入句子
	 * @return 词与词性结对结果
	 */
	List<TokenItem> tagging(String text) {
		return tokenize(text);
	}
}
