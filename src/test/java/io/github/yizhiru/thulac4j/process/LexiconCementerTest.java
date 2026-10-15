package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.term.TokenItem;
import io.github.yizhiru.thulac4j.util.ModelPaths;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LexiconCementerTest {

	@Test
	public void fallsBackToLastCompleteWordAndPrefersLongestMatch() {
		LexiconCementer cementer = new LexiconCementer(
				io.github.yizhiru.thulac4j.common.DoubleArrayTrie.make(Arrays.asList("北京", "北京大学")), "uw");
		String[][] inputs = {{"北", "京", "大", "餐"}, {"北", "京", "大", "学"}, {"北", "京", "大"}, {"北", "海"}};
		String[] expected = {"[北京/uw, 大, 餐]", "[北京大学/uw]", "[北京/uw, 大]", "[北, 海]"};
		for (int i = 0; i < inputs.length; i++) {
			List<TokenItem> tokens = new ArrayList<>();
			for (String word : inputs[i]) tokens.add(new TokenItem(word, null));
			cementer.cement(tokens);
			assertEquals(expected[i], tokens.toString());
		}
	}

	@Test
	public void cement() throws IOException {
		LexiconCementer cementer = new LexiconCementer(
				this.getClass().getResourceAsStream(ModelPaths.NS_BIN_PATH),
				"ns");
		List<TokenItem> tokenItems = new ArrayList<>(Arrays.asList(
				new TokenItem("黑", null),
				new TokenItem("龙", "n"),
				new TokenItem("江", "j"))
		);
		cementer.cement(tokenItems);
		assertEquals("[黑龙江/ns]", tokenItems.toString());

		cementer = new LexiconCementer(
				this.getClass().getResourceAsStream(ModelPaths.IDIOM_BIN_PATH),
				"i");
		tokenItems = new ArrayList<>(Arrays.asList(
				new TokenItem("掉", null),
				new TokenItem("进", "n"),
				new TokenItem("了", "j"),
				new TokenItem("无", "n"),
				new TokenItem("底洞", "j"))
		);
		cementer.cement(tokenItems);
		assertEquals("[掉, 进/n, 了/j, 无/n, 底洞/j]", tokenItems.toString());
	}
}
