package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.term.POC;
import io.github.yizhiru.thulac4j.term.AnnotatedTerms;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

/**
 * RuleAnnotator Test.
 */
public class RuleAnnotatorTest {

	@Test
	public void annotate() {
		Map<POC, String> pocStringHashMap = new HashMap<>(POC.values().length);
		pocStringHashMap.put(POC.PUNCTUATION_POC, "w");
		pocStringHashMap.put(POC.BEGIN_NUMERAL_POC, "bm");
		pocStringHashMap.put(POC.MIDDLE_NUMERAL_POC, "mm");
		pocStringHashMap.put(POC.END_NUMERAL_POC, "em");
		pocStringHashMap.put(POC.SINGLE_NUMERAL_POC, "sm");
		pocStringHashMap.put(POC.BEGIN_POC, "b");
		pocStringHashMap.put(POC.MIDDLE_POC, "m");
		pocStringHashMap.put(POC.END_POC, "e");
		pocStringHashMap.put(POC.SINGLE_POC, "s");
		pocStringHashMap.put(POC.BEGIN_OR_SINGLE_POC, "bs");
		pocStringHashMap.put(POC.END_OR_SINGLE_POC, "es");
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
				"昨日《上市公司证券发行管理办法》发布",
				"《21世纪》：",
				"《探索·发现》栏目",
				"《麦亚hee》",
				"日系＆动漫",
		};
		String[] expectedPocString = new String[]{
				"sm,bs,d,d,es,bm,mm,em,s",
				"w,bs,d,d,d,es,w",
				"s",
				"bm,em,bs,d,d,es,w,bs,d,d,es,w,w,bs,es",
				"bs,d,es,sm",
				"s,sm,s,w,s,bm,mm,mm,mm,mm,em,s",
				"bs,es,w,bs,d,es",
				"w,bs,es,w,bs,d,es",
				"b,m,m,e,bs,d,es",
				"s,bm,mm,mm,mm,mm,mm,mm,mm,mm,em,bs,d,es",
				"b,m,e,w,sm",
				"bs,d,es,sm",
				"bs,d,es,w,s",
				"bs,es,w,w,bs,es,w,w",
				"bs,es,w,b,m,m,m,e,w,s,w",
				"bs,es,w,bs,d,d,d,d,d,d,d,d,d,d,es,w,bs,es",
				"w,b,m,m,e,w,w",
				"w,b,m,m,m,e,w,bs,es",
				"w,b,m,m,m,e,w",
				"bs,es,s,bs,es",
		};

		for (int i = 0; i < sentences.length; i++) {
			AnnotatedTerms annotatedTerms = RuleAnnotator.annotate(sentences[i], true);
			String result = Stream.of(annotatedTerms.getPocs())
					.map(pocStringHashMap::get)
					.collect(Collectors.joining(","));
			if (!expectedPocString[i].equals(result)) {
				System.out.println(sentences[i]);
			}
			assertEquals(expectedPocString[i], result);
		}
	}
}
