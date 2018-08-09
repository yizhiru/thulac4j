package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.common.ModelPaths;
import io.github.yizhiru.thulac4j.common.Nullable;
import io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronClassifier;
import io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel;
import io.github.yizhiru.thulac4j.process.AnnotationRuler;
import io.github.yizhiru.thulac4j.process.DictCementer;
import io.github.yizhiru.thulac4j.process.WordCementer;
import io.github.yizhiru.thulac4j.term.ResultTerms;
import io.github.yizhiru.thulac4j.term.SegItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel.PocMark.*;

public final class SPChineseTokenizer {

    /**
     * 结构感知器模型
     */
    private StructuredPerceptronClassifier classifier;

    /**
     * 前向Label 二维数组
     */
    protected int[][] previousTrans;

    /**
     * 地名 ns 词典黏结.
     */
    public final DictCementer nsCementer;

    /**
     * 习语 idiom 词典黏结.
     */
    public final DictCementer idiomCementer;

    /**
     * 是否开启黏结书名号内的词.
     */
    protected boolean isEnableTileWord = false;

    /**
     * 自定义词典，可为null
     */
    @Nullable
    protected DictCementer uwCementer = null;

    SPChineseTokenizer(InputStream weightInput, InputStream featureInput, InputStream labelInput) {
        try {
            this.classifier = new StructuredPerceptronClassifier(
                    new StructuredPerceptronModel(weightInput, featureInput, labelInput));
            this.nsCementer = new DictCementer(
                    this.getClass().getResourceAsStream(ModelPaths.NS_BIN_PATH), "ns");
            this.idiomCementer = new DictCementer(
                    this.getClass().getResourceAsStream(ModelPaths.IDIOM_BIN_PATH), "i");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.previousTrans = setPreviousTransitions(classifier.getLabelValues());
    }

    /**
     * Label 前向转移图
     *
     * @param labelValues label值
     * @return 前向转移二维数组，每行表示该label的所有前向label
     */
    private int[][] setPreviousTransitions(String[] labelValues) {
        int labelSize = labelValues.length;
        List<List<Integer>> labelTransitions = new ArrayList<>();
        for (int i = 0; i < labelSize; i++) {
            labelTransitions.add(new LinkedList<>());
        }
        for (int cur = 0; cur < labelSize; cur++) {
            for (int pre = 0; pre < labelSize; pre++) {
                String curString = labelValues[cur];
                String preString = labelValues[pre];
                char curPoc = curString.charAt(0);
                char prePoc = preString.charAt(0);
                // 如果有相同词性或者不带词性，按转移规则进行转移
                if (curString.substring(1).equals(preString.substring(1))) {
                    // B 前面只能是E 或S
                    if (curPoc == POS_B_CHAR) {
                        if (prePoc == POS_E_CHAR || prePoc == POS_S_CHAR) {
                            labelTransitions.get(cur).add(pre);
                        }
                    }
                    // M 前面只能是M 或 B
                    else if (curPoc == POS_M_CHAR) {
                        if (prePoc == POS_M_CHAR || prePoc == POS_B_CHAR) {
                            labelTransitions.get(cur).add(pre);
                        }
                    }
                    // E 前面只能是B 或 M
                    else if (curPoc == POS_E_CHAR) {
                        if (prePoc == POS_B_CHAR || prePoc == POS_M_CHAR) {
                            labelTransitions.get(cur).add(pre);
                        }
                    }
                    // S 前面只能是E 或 S
                    else if (curPoc == POS_S_CHAR) {
                        if (prePoc == POS_E_CHAR || prePoc == POS_S_CHAR) {
                            labelTransitions.get(cur).add(pre);
                        }
                    }
                }
                // 如果带有词性并且前后词性不相同，那么则按规则
                // B 前面只能是E 或S，S 前面只能是E 或S 进行转移
                else if (curString.length() > 1) {
                    if (curPoc == POS_B_CHAR || curPoc == POS_S_CHAR) {
                        if (prePoc == POS_E_CHAR || prePoc == POS_S_CHAR) {
                            labelTransitions.get(cur).add(pre);
                        }
                    }
                }
            }
        }
        // 将List 转成二维数组
        int[][] previousTrans = new int[labelSize][];
        for (int i = 0; i < labelSize; i++) {
            previousTrans[i] = new int[labelTransitions.get(i).size()];
            for (int j = 0; j < labelTransitions.get(i).size(); j++) {
                previousTrans[i][j] = labelTransitions.get(i).get(j);
            }
        }
        return previousTrans;
    }


    /**
     * 序列标注分词
     *
     * @param sentence 输入句子
     * @return 序列标注结果
     */
    public List<SegItem> tokenize(String sentence) {
        List<SegItem> segItems = new ArrayList<>();
        if (sentence.length() == 0) {
            return segItems;
        }
        ResultTerms resultTerms = AnnotationRuler.annotate(sentence, isEnableTileWord);
        if (resultTerms.isEmpty()) {
            return segItems;
        }

        int[] labels = classifier.viterbiDecode(resultTerms, previousTrans);

        char[] rawSentence = resultTerms.getRawSentence();
        String[] labelValues = classifier.getLabelValues();
        for (int i = 0, offset = 0; i < rawSentence.length; i++) {
            String label = labelValues[labels[i]];
            char pocChar = label.charAt(0);
            if (pocChar == POS_E_CHAR || pocChar == POS_S_CHAR) {
                String word = new String(rawSentence, offset, i + 1 - offset);
                if (label.length() >= 2) {
                    segItems.add(new SegItem(word, label.substring(1)));
                } else {
                    segItems.add(new SegItem(word, null));
                }
                offset = i + 1;
            }
        }
        nsCementer.cement(segItems);
        idiomCementer.cement(segItems);
        WordCementer.cementTimeWord(segItems);
        if (uwCementer != null) {
            uwCementer.cement(segItems);
        }
        return segItems;
    }
}
