package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.model.POC;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * Ruler Test.
 */
public class RulerTest {

    @Test
    public void rulePocTest() {
        Map<POC, String> pocStringHashMap = new HashMap<>(12);
        pocStringHashMap.put(POC.PUNCTUATION_POC, "w");
        pocStringHashMap.put(POC.BEGIN_M_POC, "bm");
        pocStringHashMap.put(POC.MIDDLE_M_POC, "mm");
        pocStringHashMap.put(POC.END_M_POC, "em");
        pocStringHashMap.put(POC.SINGLE_M_POC, "sm");
        pocStringHashMap.put(POC.BEGIN_POC, "b");
        pocStringHashMap.put(POC.MIDDLE_POC, "m");
        pocStringHashMap.put(POC.END_POC, "e");
        pocStringHashMap.put(POC.SINGLE_POC, "s");
        pocStringHashMap.put(POC.BS_POC, "bs");
        pocStringHashMap.put(POC.ES_POC, "es");
        pocStringHashMap.put(POC.DEFAULT_POC, "d");

        String[] sentences = new String[]{
                "4个月赚了20％多",
                "【开放式基金】",
                "大",
                "10大重仓股：厦门钨业……这些",
                "鲜芋仙 3",
                "仅1只，为0.9923元",
                "大河《地方的",
                "●会议》无否决",
                "AT&T是一家",
                "在2017-12-12 这一天",
                "UTF-8",
                "鲜芋仙 3",
                "最右面.再",
                "内容《》真实、、",
                "签定《供货协议书》的，",
                "昨日《上市公司证券发行管理办法》发布"
        };
        String[] expectedPocString = new String[]{
                "sm,bs,d,d,es,bm,mm,em,s",
                "w,bs,d,d,d,es,w",
                "s",
                "bm,em,bs,d,d,es,w,bs,d,d,es,w,w,bs,es",
                "bs,d,es,sm",
                "s,sm,s,w,s,bm,mm,mm,mm,mm,em,s",
                "bs,es,w,bs,d,es",
                "bs,d,es,w,bs,d,es",
                "b,m,m,e,bs,d,es",
                "s,bm,mm,mm,mm,mm,mm,mm,mm,mm,em,bs,d,es",
                "b,m,m,m,e",
                "bs,d,es,sm",
                "bs,d,es,w,s",
                "bs,es,w,w,bs,es,w,w",
                "bs,es,w,b,m,m,m,e,w,s,w",
                "bs,es,w,bs,d,d,d,d,d,d,d,d,d,d,es,w,bs,es",
        };

        for (int i = 0; i < sentences.length; i++) {
            Ruler.CleanedResult cleanedResult = Ruler.ruleClean(sentences[i]);
            String result = Stream.of(cleanedResult.getSentencePoc())
                    .map(pocStringHashMap::get)
                    .collect(Collectors.joining(","));
            assertEquals(expectedPocString[i], result);
        }
    }
}
