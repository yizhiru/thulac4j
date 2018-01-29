package io.github.yizhiru.thulac4j.model;

import io.github.yizhiru.thulac4j.common.IOUtils;
import io.github.yizhiru.thulac4j.common.ModelPaths;
import io.github.yizhiru.thulac4j.dat.Dat;
import io.github.yizhiru.thulac4j.process.DATCementer;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static io.github.yizhiru.thulac4j.dat.Dat.MATCH_FAILURE_INDEX;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.BIGRAM_FEATURE_1;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.BIGRAM_FEATURE_2;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.BIGRAM_FEATURE_3;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.BIGRAM_FEATURE_4;
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
     * Label数量.
     */
    public final int labelSize;

    /**
     * 特征数量.
     */
    public final int featureSize;

    /**
     * label转移到label的权重.
     */
    public final int[] llWeights;

    /**
     * 特征对应某label的权重.
     * Why use one-dimensional array but not two-dimensional array? Please Refer to
     * <a>
     * https://stackoverflow.com/questions/2512082/java-multi-dimensional-array-vs-one-dimensional
     * </a>
     */
    public final int[] flWeights;

    /**
     * Feature DAT
     */
    private final Dat featureDat;

    /**
     * Label, 对应于cws_label.txt 或者 model_c_label.txt
     */
    public final String[] labelValues;

    /**
     * 映射 enum POC 对应的所有label 索引值.
     * 其中，行为POC的ordinal值，列为索引值
     */
    public final int[][] allowTabular;

    /**
     * 地址 ns 词典黏结.
     */
    public final DATCementer nsCementer;

    /**
     * 习语 idiom 词典黏结.
     */
    public final DATCementer idiomCementer;

    /**
     * 加载训练模型.
     *
     * @param weightPath  label转移权重、特征label权重文件路径, cws_model.bin
     *                    或者model_c_model.bin
     * @param featurePath 特征DAT文件路径, cws_dat.bin 或者 model_c_dat.bin
     * @throws IOException if an I/O error occurs
     */
    public CwsModel(String weightPath, String featurePath) throws IOException {
        // Load weights model
        ByteBuffer byteBuffer = IOUtils.mapToByteBuffer(weightPath);
        IntBuffer intBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        labelSize = intBuffer.get();
        featureSize = intBuffer.get();
        llWeights = new int[labelSize * labelSize];
        flWeights = new int[featureSize * labelSize];
        intBuffer.get(llWeights);
        intBuffer.get(flWeights);

        // Load feature DAT
        byteBuffer = IOUtils.mapToByteBuffer(featurePath);
        // int类型占4个字节
        int arrayLen = byteBuffer.remaining() / 4;
        int[] featureArray = new int[arrayLen];
        intBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        intBuffer.get(featureArray);
        // convert feature DAT
        int[] baseArr = new int[arrayLen / 2];
        int[] checkArr = new int[arrayLen / 2];
        for (int i = 0; i < arrayLen / 2; i++) {
            baseArr[i] = featureArray[2 * i];
            checkArr[i] = featureArray[2 * i + 1];
        }
        featureDat = new Dat(baseArr, checkArr);

        // Load label file
        String labelPath;
        if (labelSize == 4) {
            labelPath = ModelPaths.SEG_ONLY_LABEL_PATH;
        } else {
            labelPath = ModelPaths.SEG_POS_LABEL_PATH;
        }
        List<String> labelList = IOUtils.readLines(
                this.getClass().getResourceAsStream(labelPath));
        labelValues = new String[labelList.size()];
        labelList.toArray(labelValues);

        // 记录label 集合，能与allowTabular 映射起来
        int[][] posTags = getPosTags();

        // allowTabular 表示enum POC 对应的所有允许label，比如：
        // PUNCTUATION_POC 对应的允许label为 3 或 3w，
        // BEGIN_POC 对应的允许label为 0 或 0打头的label
        allowTabular = new int[12][];
        for (int i = 0; i < labelValues.length; i++) {
            // punctuation
            if ("3".equals(labelValues[i]) || "3w".equals(labelValues[i])) {
                allowTabular[0] = new int[]{i};
            }
            // single of numeral
            if ("3".equals(labelValues[i]) || "3m".equals(labelValues[i])) {
                allowTabular[4] = new int[]{i};
            }
            // begin of numeral
            else if ("0".equals(labelValues[i]) || "0m".equals(labelValues[i])) {
                allowTabular[1] = new int[]{i};
            }
            // middle of numeral
            else if ("1".equals(labelValues[i]) || "1m".equals(labelValues[i])) {
                allowTabular[2] = new int[]{i};
            }
            // end of numeral
            else if ("2".equals(labelValues[i]) || "2m".equals(labelValues[i])) {
                allowTabular[3] = new int[]{i};
            }
        }
        int[] indices = {1, 2, 4, 8, 9, 12, 15};
        for (int i = 0; i < indices.length; i++) {
            allowTabular[i + 5] = posTags[indices[i]];
        }

        nsCementer = new DATCementer(
                this.getClass().getResourceAsStream(ModelPaths.NS_BIN_PATH),
                "ns");
        idiomCementer = new DATCementer(
                this.getClass().getResourceAsStream(ModelPaths.IDIOM_BIN_PATH),
                "i");
    }

    /**
     * 计算所有可能label 索引值集合，以二维数组表示
     *
     * @return 索引值二维数组
     */
    private int[][] getPosTags() {
        List<List<Integer>> posTagsList = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            posTagsList.add(new ArrayList<>());
        }
        for (int i = 0; i < labelValues.length; i++) {
            int segIndex = labelValues[i].charAt(0) - '0';
            for (int j = 0; j < 16; j++) {
                if (((1 << segIndex) & j) != 0) {
                    posTagsList.get(j).add(i);
                }
            }
        }
        // 将posTagsList 转成二维数组
        int[][] posTags = new int[posTagsList.size()][];
        for (int i = 0; i < posTagsList.size(); i++) {
            List<Integer> list = posTagsList.get(i);
            posTags[i] = new int[list.size()];
            for (int j = 0; j < list.size(); j++) {
                posTags[i][j] = list.get(j);
            }
        }
        return posTags;
    }

    /**
     * 训练模型文件中POC对应的标识.
     */
    public static final class PocMark {
        /**
         * 对应于 POC B 的char.
         */
        public static final Character POS_B_CHAR = '0';

        /**
         * 对应于 POC M 的char.
         */
        public static final Character POS_M_CHAR = '1';

        /**
         * 对应于 POC E 的char.
         */
        public static final Character POS_E_CHAR = '2';

        /**
         * 对应于 POC B 的char.
         */
        public static final Character POS_S_CHAR = '3';
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
        if (index == MATCH_FAILURE_INDEX) {
            return MATCH_FAILURE_INDEX;
        }
        return featureDat.getBaseByIndex(index);
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
        if (index == MATCH_FAILURE_INDEX) {
            return MATCH_FAILURE_INDEX;
        }
        return featureDat.getBaseByIndex(index);
    }

    /**
     * 根据featureDAT的base值，更新特征权重之和数组
     *
     * @param weights      label权重之和数组
     * @param base         featureDAT base值
     * @param labelIndices 允许POS 索引值
     */
    private void addWeights(int[] weights, int base, int[] labelIndices) {
        int offset = base * labelSize;
        for (int i : labelIndices) {
            weights[i] += flWeights[offset + i];
        }
    }

    /**
     * 根据前后一起的五个字符，计算加权特征权重之和数组
     *
     * @param left2        前二字符
     * @param left1        前一字符
     * @param mid          当前字符
     * @param right1       后一字符
     * @param right2       后二字符
     * @param labelIndices 允许label 索引值
     * @return 一维数组，表示当前字符的各label对应的特征权值加权之和
     */
    public int[] evaluateCharWeights(
            char left2,
            char left1,
            char mid,
            char right1,
            char right2,
            int[] labelIndices) {
        int[] weights = new int[labelSize];
        int base;
        if ((base = findUnigramFeat(mid, UNIGRAM_FEATURE_1)) != MATCH_FAILURE_INDEX) {
            addWeights(weights, base, labelIndices);
        }
        if ((base = findUnigramFeat(left1, UNIGRAM_FEATURE_2)) != MATCH_FAILURE_INDEX) {
            addWeights(weights, base, labelIndices);
        }
        if ((base = findUnigramFeat(right1, UNIGRAM_FEATURE_3)) != MATCH_FAILURE_INDEX) {
            addWeights(weights, base, labelIndices);
        }
        if ((base = findBigramFeat(left1, mid, BIGRAM_FEATURE_1)) != MATCH_FAILURE_INDEX) {
            addWeights(weights, base, labelIndices);
        }
        if ((base = findBigramFeat(mid, right1, BIGRAM_FEATURE_2)) != MATCH_FAILURE_INDEX) {
            addWeights(weights, base, labelIndices);
        }
        if ((base = findBigramFeat(left2, left1, BIGRAM_FEATURE_3)) != MATCH_FAILURE_INDEX) {
            addWeights(weights, base, labelIndices);
        }
        if ((base = findBigramFeat(right1, right2, BIGRAM_FEATURE_4)) != MATCH_FAILURE_INDEX) {
            addWeights(weights, base, labelIndices);
        }
        return weights;
    }
}
