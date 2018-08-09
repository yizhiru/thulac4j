package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.common.DoubleArrayTrie;
import io.github.yizhiru.thulac4j.process.DictCementer;

import java.util.List;
import java.util.stream.Collectors;

import static io.github.yizhiru.thulac4j.common.ModelPaths.*;

/**
 * 中文分词.
 */
public final class Segmenter {

    private static final SPChineseTokenizer TOKENIZER = new SPChineseTokenizer(
            Segmenter.class.getResourceAsStream(SEGMENTER_WEIGHT_PATH),
            Segmenter.class.getResourceAsStream(SEGMENTER_FEATURE_PATH),
            Segmenter.class.getResourceAsStream(SEGMENTER_LABEL_PATH));


    public static List<String> segment(String sentence) {
        return TOKENIZER.tokenize(sentence)
                .stream()
                .map(item -> (item.word))
                .collect(Collectors.toList());
    }

    public static void addUserWords(List<String> words) {
        DoubleArrayTrie dat = DoubleArrayTrie.make(words);
        TOKENIZER.uwCementer = new DictCementer(dat, "uw");
    }

    public static void enableTitleWord() {
        TOKENIZER.isEnableTileWord = true;
    }
}
