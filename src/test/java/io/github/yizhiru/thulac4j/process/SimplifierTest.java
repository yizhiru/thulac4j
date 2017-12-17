package io.github.yizhiru.thulac4j.process;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class SimplifierTest {

    @Test
    public void t2s() throws IOException, URISyntaxException {
        Simplifier simplifier = new Simplifier();
        String[] traditions = new String[]{
                "為何曾加入日軍的他，一生無法原諒日本人？日導演用7年走遍台灣，拍下時代淚水",
                "「那些人哪裡像軍隊？根本不知是蔣介石從哪撿來的流氓！」",
                "明明課本上都說光復節是台灣人熱烈歡迎「祖國」到來的時刻，為何有一群受過日本統治的台灣人，到現在都不能接受中華民國？",
                "鯛魚是低脂肪、高蛋白的健康食材, 肉質軟嫩細緻。",
                "世界商機大發現：抓住泰國工頭的需求 就是臺灣手工具產業的福氣啦！",
                "房市買氣還沒回春，房價也還在向下修正，但土地交易熱度卻是燒燙燙，替地方政府的國庫充實不少"
        };
        String[] simples = new String[]{
                "为何曾加入日军的他，一生无法原谅日本人？日导演用7年走遍台湾，拍下时代泪水",
                "「那些人哪里像军队？根本不知是蒋介石从哪捡来的流氓！」",
                "明明课本上都说光复节是台湾人热烈欢迎「祖国」到来的时刻，为何有一群受过日本统治的台湾人，到现在都不能接受中华民国？",
                "鲷鱼是低脂肪、高蛋白的健康食材, 肉质软嫩细致。",
                "世界商机大发现：抓住泰国工头的需求 就是台湾手工具产业的福气啦！",
                "房市买气还没回春，房价也还在向下修正，但土地交易热度却是烧烫烫，替地方政府的国库充实不少"
        };

        for (int i = 0; i < traditions.length; i++) {
            assertEquals(simples[i], simplifier.t2s(traditions[i]));
        }
    }
}
