package io.github.yizhiru.thulac4j.process;

import org.junit.Test;

/**
 * @author jyzheng
 */
public class RulerTest {

  @Test
  public void rulePocTest() {
    String[] sentences = new String[] {
            "4个月赚了20％多",
            "【开放式基金】",
            "大",
            "10大重仓股：贵州茅台、山东黄金、厦门钨业……这些",
            "鲜芋仙 3",
            "仅1只，为0.9923元",
            "大江大河《地方的",
            "●本次会议》无否决",
            "内容《》真实、准确、完整",
            "签定《供货协议书》的，",
            "昨日《上市公司证券发行管理办法》正式发布"
    };
    for(String sentence: sentences) {
      Ruler ruler = new Ruler(sentence.toCharArray());
      ruler.rulePoc();
      System.out.println(ruler.pocss);
    }
  }
}
