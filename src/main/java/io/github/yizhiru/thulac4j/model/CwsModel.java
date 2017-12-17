package io.github.yizhiru.thulac4j.model;

import io.github.yizhiru.thulac4j.dat.Dat;
import io.github.yizhiru.thulac4j.process.DATCementer;

import java.io.IOException;
import java.io.Serializable;

import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.BIGRAM_FEATURE_1;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.BIGRAM_FEATURE_2;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.BIGRAM_FEATURE_3;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.BIGRAM_FEATURE_4;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.BOUNDARY;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.SPACE;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.UNIGRAM_FEATURE_1;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.UNIGRAM_FEATURE_2;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.UNIGRAM_FEATURE_3;

/**
 * 分词模型.
 */
public final class CwsModel implements Serializable {

    private static final long serialVersionUID = -5324153272692800765L;

    /**
     * label数量.
     */
    public final int labelSize;

    /**
     * 特征数量.
     */
    public final int featureSize;

    /**
     * label转移到label的权重.
     */
    public final int[][] llWeights;

    /**
     * 特征对应某label的权重.
     */
    public final int[][] flWeights;

    /**
     * Feature DAT
     */
    private final Dat featureDat;

    /**
     * Label.
     */
    public final String[] labelValues;

    /**
     * map to enum POC
     */
    public final int[][] allowTabular;

    /**
     * ns 词典
     */
    public final DATCementer ns;

    /**
     * idiom 词典
     */
    public final DATCementer idiom;

    public CwsModel(
            int labelSize,
            int featureSize,
            int[][] llWeights,
            int[][] flWeights,
            Dat featureDat,
            String[] labelValues,
            int[][] allowTabular,
            DATCementer ns,
            DATCementer idiom) {
        this.labelSize = labelSize;
        this.featureSize = featureSize;
        this.llWeights = llWeights;
        this.flWeights = flWeights;
        this.featureDat = featureDat;
        this.labelValues = labelValues;
        this.allowTabular = allowTabular;
        this.ns = ns;
        this.idiom = idiom;
    }

    public static CwsModel loadModel(String weightPath, String featurePath) throws IOException {
        return new ThulacModel(weightPath, featurePath)
                .convert2CwsModel();
    }

    /**
     * N-gram 特征.
     * THULAC采用的分词模型为结构化感知器（Structured Perceptron, SP），以最大熵准则
     * 建模序列标注的得分函数.
     */
    public static class NGramFeature {

        /**
         * 超越边界的统一字符'#'
         */
        public static final char BOUNDARY = 65283;

        /**
         * feature的一部分
         */
        public static final char SPACE = ' ';

        /**
         * Unigram 特征种类1，对应于特征 mid + SPACE + '1'，即标注对应的当前字符
         */
        public static final char UNIGRAM_FEATURE_1 = '1';

        /**
         * Unigram 特征种类2，对应于特征 left + SPACE + '2'，即标注的前一字符
         */
        public static final char UNIGRAM_FEATURE_2 = '2';

        /**
         * Unigram 特征种类3，对应于特征 right + SPACE + '3'，即标注的后一字符
         */
        public static final char UNIGRAM_FEATURE_3 = '3';

        /**
         * Bigram 特征种类1，对应于特征 left + mid + SPACE + '1'，
         * 即标注的前一字符加上当前字符
         */
        public static final char BIGRAM_FEATURE_1 = '1';

        /**
         * Bigram 特征种类2，对应于特征 mid + right + SPACE + '2'，
         * 即标注对应的当前字符加上后一字符
         */
        public static final char BIGRAM_FEATURE_2 = '2';

        /**
         * Bigram 特征种类3，对应于特征 left2 + left1 + SPACE + '3'，
         * 即标注的前二字符加上前一字符
         */
        public static final char BIGRAM_FEATURE_3 = '3';

        /**
         * Bigram 特征种类4，对应于特征 right + right2 + SPACE + '4'，
         * 即标注的后一字符加上后二字符.
         */
        public static final char BIGRAM_FEATURE_4 = '4';
    }

    /**
     * 寻找Unigram特征对应于DAT中的base.
     *
     * @param ch   字符
     * @param mark 标识属于3种特征中的一种: '1', '2', '3'
     * @return 若存在则返回base，否则则返回-1
     */
    private int findUnigramFeat(char ch, char mark) {
        int index = (int) ch;
        index = featureDat.transition(index, SPACE);
        index = featureDat.transition(index, mark);
        if (index == -1) {
            return -1;
        }
        return featureDat.get(index).base;
    }

    /**
     * 寻找Bigram特征对应于DAT中的base
     *
     * @param c1   第一个字符
     * @param c2   第二个字符
     * @param mark 标识属于4种特征中的一种: '1', '2', '3', '4'
     * @return 若存在则返回对应的base值，否则返回-1
     */
    private int findBigramFeat(char c1, char c2, char mark) {
        int index1 = (int) c1;
        int index2 = (int) c2;
        int index = featureDat.transition(index1, index2);
        index = featureDat.transition(index, SPACE);
        index = featureDat.transition(index, mark);
        if (index == -1) {
            return -1;
        }
        return featureDat.get(index).base;
    }

    /**
     * 根据featureDAT的base值，更新特征权重之和数组
     *
     * @param value label权重之和数组
     * @param base  featureDAT base值
     */
    private void addWeights(int[] value, int base) {
        for (int i = 0; i < labelSize; i++) {
            value[i] += flWeights[base][i];
        }
    }

    /**
     * 根据前后一起的五个字符，计算加权之和数组
     *
     * @param left1  前一字符
     * @param mid    当前字符
     * @param right1 后一字符
     * @param right2 后二字符
     * @return 一维数组，表示当前字符的各label对应的特征权值加权之和
     */
    private int[] evaluateCharWeights(
            char left2,
            char left1,
            char mid,
            char right1,
            char right2) {
        int[] weights = new int[labelSize];
        int base;
        if ((base = findUnigramFeat(mid, UNIGRAM_FEATURE_1)) != -1) {
            addWeights(weights, base);
        }
        if ((base = findUnigramFeat(left1, UNIGRAM_FEATURE_2)) != -1) {
            addWeights(weights, base);
        }
        if ((base = findUnigramFeat(right1, UNIGRAM_FEATURE_3)) != -1) {
            addWeights(weights, base);
        }
        if ((base = findBigramFeat(left1, mid, BIGRAM_FEATURE_1)) != -1) {
            addWeights(weights, base);
        }
        if ((base = findBigramFeat(mid, right1, BIGRAM_FEATURE_2)) != -1) {
            addWeights(weights, base);
        }
        if ((base = findBigramFeat(left2, left1, BIGRAM_FEATURE_3)) != -1) {
            addWeights(weights, base);
        }
        if ((base = findBigramFeat(right1, right2, BIGRAM_FEATURE_4)) != -1) {
            addWeights(weights, base);
        }

        return weights;
    }

    /**
     * 计算句子特征权重之和二维数组，其中行数为句子长度，列数为label数；
     * 每一行表示当前字符的特征权重之和数组
     *
     * @param sentence 句子字符串
     * @return 二维数组
     */
    public int[][] evaluateSentenceWeights(char[] sentence) {
        int len = sentence.length;
        int[][] weights = new int[len][];
        char[] chs = new char[len + 4];
        System.arraycopy(sentence, 0, chs, 2, len);
        // 首尾拼接BOUNDARY 字符
        chs[0] = chs[1] = chs[chs.length - 2] = chs[chs.length - 1] = BOUNDARY;
        for (int i = 0; i < len; i++) {
            weights[i] = evaluateCharWeights(
                    chs[i],
                    chs[i + 1],
                    chs[i + 2],
                    chs[i + 3],
                    chs[i + 4]);
        }
        return weights;
    }
}
