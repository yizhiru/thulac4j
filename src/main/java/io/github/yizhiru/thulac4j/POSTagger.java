package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.common.DoubleArrayTrie;
import io.github.yizhiru.thulac4j.process.DictCementer;
import io.github.yizhiru.thulac4j.term.SegItem;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static io.github.yizhiru.thulac4j.common.ModelPaths.POS_TAGGING_LABEL_PATH;

/**
 * 中文词性标注.
 */
public class POSTagger {

    private SPChineseTokenizer tokenizer;

    public POSTagger(String weightPath, String featurePath) throws IOException {
        this.tokenizer = new SPChineseTokenizer(
                new FileInputStream(weightPath),
                new FileInputStream(featurePath),
                POSTagger.class.getResourceAsStream(POS_TAGGING_LABEL_PATH));
    }

    List<SegItem> tagging(String sentence) {
        return tokenizer.tokenize(sentence);
    }

    public void addUserWords(List<String> words) {
        DoubleArrayTrie dat = DoubleArrayTrie.make(words);
        tokenizer.uwCementer = new DictCementer(dat, "uw");
    }

    public void enableTitleWord() {
        tokenizer.isEnableTileWord = true;
    }
}
