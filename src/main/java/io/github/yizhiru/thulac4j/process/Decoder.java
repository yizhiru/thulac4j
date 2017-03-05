package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.base.ALLowLabels;
import io.github.yizhiru.thulac4j.base.CwsModel;

import java.util.HashMap;
import java.util.List;

/**
 * @author jyzheng
 */
public class Decoder {

  private static class Alpha {
    public int score; // 打分
    public int preLabel; // 前向label

    public Alpha(int score, int preLabel) {
      this.score = score;
      this.preLabel = preLabel;
    }
  }

  /**
   * Viterbi算法解码
   *
   * @param model         CwsModel训练模型
   * @param sentence      待分词句子
   * @param allows        每个char允许的label
   * @param values        特征模板对应的权值加权之和F(y_{t+1}=y,C)
   * @param labelPreTrans label的前向label
   * @return
   */
  public static int[] viterbi(CwsModel model, char[] sentence, ALLowLabels[] allows,
                              int[][] values, List<Integer>[] labelPreTrans) {
    int len = sentence.length;
    int[] bestLabels = new int[len]; // 最优路径对应的label
    Alpha alpha = new Alpha(0, -1);
    int maxScore = 0;
    // 记录在位置i时类别为y的最优路径
    // HashMap: label -> (score, preLabel)]
    @SuppressWarnings("unchecked")
    HashMap<Integer, Alpha>[] tabular = new HashMap[len];
    //初始化
    tabular[0] = new HashMap<>(allows[0].labels.length);
    for (int lab : allows[0].labels) {
      tabular[0].put(lab, new Alpha(values[0][lab], -1));
    }
    // DP求解
    for (int i = 1; i < len; i++) {
      tabular[i] = new HashMap<>(allows[i].labels.length);
      for (int lab : allows[i].labels) {
        List<Integer> preLabels = labelPreTrans[lab];
        for (int pre : preLabels) {
          maxScore = tabular[i - 1].getOrDefault(pre, alpha).score +
                  model.llWeights[pre][lab] + values[i][lab];
          if (!tabular[i].containsKey(lab) || maxScore > tabular[i].get(lab).score) {
            tabular[i].put(lab, new Alpha(maxScore, pre));
          }
        }
      }
    }
    // 找到尾节点的最优label
    for (int lab : allows[len - 1].labels) {
      if (tabular[len - 1].get(lab).score > alpha.score) {
        alpha = tabular[len - 1].get(lab);
        bestLabels[len - 1] = lab;
      }
    }
    // 回溯最优路径，保留label到数组
    for (int i = len - 2, j = len - 2; i >= 0; i--, j--) {
      bestLabels[i] = alpha.preLabel;
      alpha = tabular[j].get(alpha.preLabel);
    }
    return bestLabels;
  }

}
