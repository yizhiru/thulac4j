package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel;
import io.github.yizhiru.thulac4j.term.TokenItem;
import io.github.yizhiru.thulac4j.util.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static io.github.yizhiru.thulac4j.util.ModelPaths.POS_TAGGING_LABEL_PATH;

/**
 * 中文词性标注.
 */
public class POSTagger extends SPChineseTokenizer {

	public POSTagger(String weightPath, String featurePath) throws IOException {
		this(weightPath, featurePath, TokenizerConfig.DEFAULT);
	}

	public POSTagger(String weightPath, String featurePath, TokenizerConfig config) throws IOException {
		super(loadModel(weightPath, featurePath), config);
	}

	private static StructuredPerceptronModel loadModel(
			String weightPath, String featurePath) throws IOException {
		try (InputStream weights = new FileInputStream(weightPath);
		     InputStream features = new FileInputStream(featurePath);
		     InputStream labels = IOUtils.openResource(POS_TAGGING_LABEL_PATH)) {
			return new StructuredPerceptronModel(weights, features, labels);
		}
	}

	/**
	 * 词性标注
	 *
	 * @param text 输入句子
	 * @return 词与词性结对结果
	 */
	public List<TokenItem> tagging(String text) {
		return tokenize(text);
	}
}
