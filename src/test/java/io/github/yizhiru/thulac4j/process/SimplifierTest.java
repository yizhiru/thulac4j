package io.github.yizhiru.thulac4j.process;

import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * @author jyzheng
 */
public class SimplifierTest {

  @Test
  public void t2sTest() throws FileNotFoundException {
    Simplifier simplifier = new Simplifier();
    String[] traditions = new String[]{
            "為何曾加入日軍的他，一生無法原諒日本人？日導演用7年走遍台灣，拍下時代淚水",
            "「那些人哪裡像軍隊？根本不知是蔣介石從哪撿來的流氓！」",
            "明明課本上都說光復節是台灣人熱烈歡迎「祖國」到來的時刻，為何有一群受過日本統治的台灣人，到現在都不能接受中華民國？",
            "鯛魚是低脂肪、高蛋白的健康食材, 肉質軟嫩細緻。",
            "世界商機大發現：抓住泰國工頭的需求 就是臺灣手工具產業的福氣啦！",
            "房市買氣還沒回春，房價也還在向下修正，但土地交易熱度卻是燒燙燙，替地方政府的國庫充實不少"
    };
    for (String raw : traditions) {
      System.out.println(simplifier.t2s(raw));
    }

  }
}
