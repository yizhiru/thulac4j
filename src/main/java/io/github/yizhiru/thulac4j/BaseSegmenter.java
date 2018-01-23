package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.common.Nullable;
import io.github.yizhiru.thulac4j.dat.Dat;
import io.github.yizhiru.thulac4j.dat.DatMaker;
import io.github.yizhiru.thulac4j.model.CwsModel;
import io.github.yizhiru.thulac4j.model.SegItem;
import io.github.yizhiru.thulac4j.process.DATCementer;
import io.github.yizhiru.thulac4j.process.Decoder;
import io.github.yizhiru.thulac4j.process.Ruler;
import io.github.yizhiru.thulac4j.process.TimeCementer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.github.yizhiru.thulac4j.model.CwsModel.PocMark.POS_B_CHAR;
import static io.github.yizhiru.thulac4j.model.CwsModel.PocMark.POS_E_CHAR;
import static io.github.yizhiru.thulac4j.model.CwsModel.PocMark.POS_M_CHAR;
import static io.github.yizhiru.thulac4j.model.CwsModel.PocMark.POS_S_CHAR;
import static io.github.yizhiru.thulac4j.process.Ruler.CleanedSentence;

/**
 * Base class.
 */
public abstract class BaseSegmenter<T> {
    /**
     * 中文分词模型
     */
    protected CwsModel model;

    /**
     * 前向Label 二维数组
     */
    protected int[][] previousTrans;

    /**
     * 是否开启黏结书名号内的词.
     */
    protected boolean isEnableTileWord = false;

    /**
     * 自定义词典，可为null
     */
    @Nullable
    protected DATCementer uw;

    protected BaseSegmenter(String weightPath, String featurePath) throws IOException {
        model = new CwsModel(weightPath, featurePath);
        previousTrans = setPreviousTrans(model.labelValues);
    }

    /**
     * Label 前向转移图
     *
     * @param labelValues label值
     * @return 前向转移二维数组，每行表示该label的所有前向label
     */
    private int[][] setPreviousTrans(String[] labelValues) {
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
     * 设置自定义词典路径
     *
     * @param path 自定义词典路径
     */
    public void setUserWordsPath(String path) throws IOException {
        Dat dat = DatMaker.make(path);
        uw = new DATCementer(dat, "uw");
    }

    /**
     * 序列标注分词
     *
     * @param sentence 输入句子
     * @return 序列标注结果
     */
    public List<T> segment(String sentence) {
        List<SegItem> segItems = new ArrayList<>();
        if (sentence.length() == 0) {
            return process(segItems);
        }
        CleanedSentence cleanedSentence = Ruler.ruleClean(sentence, isEnableTileWord);
        if (cleanedSentence.isEmpty()) {
            return process(segItems);
        }

        int[] labels = Decoder.viterbiDecode(
                model,
                cleanedSentence,
                previousTrans);

        char[] rawSentence = cleanedSentence.getRawSentence();
        for (int i = 0, offset = 0; i < rawSentence.length; i++) {
            String label = model.labelValues[labels[i]];
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
        model.nsCementer.cement(segItems);
        model.idiomCementer.cement(segItems);
        TimeCementer.cement(segItems);
        if (uw != null) {
            uw.cement(segItems);
        }
        return process(segItems);
    }

    public void enableTitleWord() {
        isEnableTileWord = true;
    }

    /**
     * 处理序列标注结果
     *
     * @param segItems 序列标注结果
     * @return 分词结果
     */
    abstract List<T> process(List<SegItem> segItems);
}
