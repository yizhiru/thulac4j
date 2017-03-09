package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.base.CwsModel;
import io.github.yizhiru.thulac4j.base.POCS;

import java.util.Arrays;
import java.util.List;

/**
 * @author jyzheng
 */
public class Decoder {

  private static final int NULL = -5;

  private static class Alpha {
    public int score; // 打分
    public int preLabel; // 前向label

    public Alpha(int score, int preLabel) {
      this.score = score;
      this.preLabel = preLabel;
    }

    public Alpha() {
      score = 0;
      preLabel = NULL;
    }
  }

  /**
   * Viterbi算法解码
   *
   * @param model         CwsModel训练模型
   * @param len           待分词句子
   * @param pocses        每个char允许的POCS
   * @param values        特征模板对应的权值加权之和F(y_{t+1}=y,C)
   * @param labelPreTrans label的前向label
   * @return 最优路径对应的labels
   */
  public static int[] viterbi(CwsModel model, int len, List<POCS> pocses,
                              int[][] values, int[][] labelPreTrans) {
    int[] bestPath = new int[len]; // 最优路径对应的label
    int lSize = model.labelSize, score, bestScore = Integer.MIN_VALUE, bestLabel = 2;
    int[] preLabels, labels;
    Alpha alpha;
    // 记录在位置i时类别为y的最优路径
    // [current index][current Label]: Alpha(score, preLabel)
    Alpha[][] tabular = new Alpha[len][];
    for (int i = 0; i < len; i++) {
      tabular[i] = new Alpha[lSize];
      for (int j = 0; j < lSize; j++)
        tabular[i][j] = new Alpha();
    }
    // DP求解
    for (int i = 0; i < len; i++) {
      labels = model.allowTabular[pocses.get(i).ordinal()];
      for (int label : labels) {
        alpha = tabular[i][label];
        if (i == 0) {
          alpha.preLabel = -1;
        } else {
          preLabels = labelPreTrans[label];
          for (int pre : preLabels) {
            if (tabular[i - 1][pre].preLabel == NULL) continue;
            score = tabular[i - 1][pre].score + model.llWeights[pre][label];
            if (alpha.preLabel == NULL || score > alpha.score) {
              alpha.score = score;
              alpha.preLabel = pre;
            }
          }
        }
        alpha.score += values[i][label];
        if (i == len - 1 && bestScore < alpha.score) {
          bestScore = alpha.score;
          bestLabel = label;
        }
      }
    }
    // 尾节点的最优label
    alpha = tabular[len - 1][bestLabel];
    bestPath[len - 1] = bestLabel;
    // 回溯最优路径，保留label到数组
    for (int i = len - 2; i >= 0; i--) {
      bestPath[i] = alpha.preLabel;
      alpha = tabular[i][alpha.preLabel];
    }
    return bestPath;
  }

}
