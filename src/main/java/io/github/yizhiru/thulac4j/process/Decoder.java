package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.model.CwsModel;
import io.github.yizhiru.thulac4j.model.POC;

import java.util.List;

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
     * @param model         训练模型
     * @param pocs          POC数组
     * @param weights       特征模板对应的权值加权之和F(y_{t+1}=y,C)
     * @param previousTrans 前向转移label
     * @return 最优路径对应的labels
     */
    public static int[] viterbiDecode(
            CwsModel model,
            POC[] pocs,
            int[][] weights,
            int[][] previousTrans) {
        int len = pocs.length;
        // 最优路径对应的label
        int[] bestPath = new int[len];
        int labelSize = model.labelSize;
        int bestLastScore = Integer.MIN_VALUE;
        int bestLastLabel = 2;
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
        // DP求解
        for (int i = 0; i < len; i++) {
            int[] labels = model.allowTabular[pocs[i].ordinal()];
            int label;
            for (int j = 0; j < labels.length; j++) {
                label = labels[j];
                alpha = pathTabular[i][label];
                if (i == 0) {
                    alpha.preLabel = INITIAL_PREVIOUS_LABEL;
                } else {
                    int[] preLabels = previousTrans[label];
                    for (int pre : preLabels) {
                        if (pathTabular[i - 1][pre].preLabel == NULL_PREVIOUS_LABEL) {
                            continue;
                        }
                        int score = pathTabular[i - 1][pre].score
                                + model.llWeights[pre * model.labelSize + label];
                        if (alpha.preLabel == NULL_PREVIOUS_LABEL || score > alpha.score) {
                            alpha.score = score;
                            alpha.preLabel = pre;
                        }
                    }
                }
                alpha.score += weights[i][label];
                if (i == len - 1 && bestLastScore < alpha.score) {
                    bestLastScore = alpha.score;
                    bestLastLabel = label;
                }
            }
        }
        // 尾节点的最优label
        alpha = pathTabular[len - 1][bestLastLabel];
        bestPath[len - 1] = bestLastLabel;
        // 回溯最优路径，保留label到数组
        for (int i = len - 2; i >= 0; i--) {
            bestPath[i] = alpha.preLabel;
            alpha = pathTabular[i][alpha.preLabel];
        }
        return bestPath;
    }
}
