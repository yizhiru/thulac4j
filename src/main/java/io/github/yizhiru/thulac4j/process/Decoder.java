package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.model.CwsModel;
import io.github.yizhiru.thulac4j.model.POC;

/**
 * Viterbi 解码.
 */
public final class Decoder {

    /**
     * Null previous label.
     */
    private static final int NULL_PREVIOUS_LABEL = -5;

    /**
     * Initial score.
     */
    private static final int INITIAL_SCORE = 0;

    /**
     * Initial previous label.
     */
    private static final int INITIAL_PREVIOUS_LABEL = -1;

    private static class Alpha {
        /**
         * Score.
         */
        private int score;

        /**
         * Previous Label.
         */
        private int preLabel;

        public Alpha() {
            score = INITIAL_SCORE;
            preLabel = NULL_PREVIOUS_LABEL;
        }

        @Override
        public String toString() {
            return score + ", " + preLabel;
        }
    }

    /**
     * Viterbi算法解码
     *
     * @param cwsModel        训练模型
     * @param cleanedSentence 规则处理后的句子Label 类
     * @param previousTrans   前向转移label
     * @return 最优路径对应的labels
     */
    public static int[] viterbiDecode(
            CwsModel cwsModel,
            Ruler.CleanedSentence cleanedSentence,
            int[][] previousTrans) {
        int len = cleanedSentence.length();
        // 最优路径对应的label
        int[] bestPath = new int[len];
        int labelSize = cwsModel.labelSize;
        int optimalLastScore = Integer.MIN_VALUE;
        int optimalLastLabel = 2;
        Alpha alpha;
        // 记录在位置i时类别为y的最优路径
        // [current index][current Label] -> Alpha(score, preLabel)
        Alpha[][] pathTabular = new Alpha[len][];
        for (int i = 0; i < len; i++) {
            pathTabular[i] = new Alpha[labelSize];
            for (int j = 0; j < labelSize; j++) {
                pathTabular[i][j] = new Alpha();
            }
        }

        char[] chars = cleanedSentence.insertBoundary();
        POC[] pocs = cleanedSentence.getSentencePoc();

        // DP求解
        for (int i = 0; i < len; i++) {
            int[] labelIndices = cwsModel.allowTabular[pocs[i].ordinal()];
            int[] weights = cwsModel.evaluateCharWeights(chars[i],
                    chars[i + 1],
                    chars[i + 2],
                    chars[i + 3],
                    chars[i + 4],
                    labelIndices);
            for (int labelIndex : labelIndices) {
                alpha = pathTabular[i][labelIndex];
                if (i == 0) {
                    alpha.preLabel = INITIAL_PREVIOUS_LABEL;
                } else {
                    int[] preLabels = previousTrans[labelIndex];
                    for (int pre : preLabels) {
                        if (pathTabular[i - 1][pre].preLabel == NULL_PREVIOUS_LABEL) {
                            continue;
                        }
                        int score = pathTabular[i - 1][pre].score
                                + cwsModel.llWeights[pre * cwsModel.labelSize + labelIndex];
                        if (alpha.preLabel == NULL_PREVIOUS_LABEL || score > alpha.score) {
                            alpha.score = score;
                            alpha.preLabel = pre;
                        }
                    }
                }
                alpha.score += weights[labelIndex];
                if (i == len - 1 && optimalLastScore < alpha.score) {
                    optimalLastScore = alpha.score;
                    optimalLastLabel = labelIndex;
                }
            }
        }
        // 尾节点的最优label
        alpha = pathTabular[len - 1][optimalLastLabel];
        bestPath[len - 1] = optimalLastLabel;
        // 回溯最优路径，保留label到数组
        for (int i = len - 2; i >= 0; i--) {
            bestPath[i] = alpha.preLabel;
            alpha = pathTabular[i][alpha.preLabel];
        }
        return bestPath;
    }
}
