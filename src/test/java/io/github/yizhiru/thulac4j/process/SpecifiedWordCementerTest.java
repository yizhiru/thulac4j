package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.term.TokenItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpecifiedWordCementerTest {

	@Test public void preservesYearRuleAndCombinesLongRuns() {
		List<TokenItem> tokens = new ArrayList<>();
		for (String word : new String[]{"2", "年", "20", "01", "日", "—", "…", "…", "甲"}) {
			tokens.add(new TokenItem(word, null));
		}
		SpecifiedWordCementer.cementWord(tokens);
		assertEquals("[2, 年, 2001日/t, —, ……, 甲]", tokens.toString());
		tokens.clear();
		for (int i = 0; i < 10000; i++) tokens.add(new TokenItem("—", "w"));
		SpecifiedWordCementer.cementWord(tokens);
		assertEquals(1, tokens.size());
		assertEquals(10000, tokens.get(0).word.length());
	}

	@Test
	public void cement() {
		List<TokenItem> tokenItems = new ArrayList<>(Arrays.asList(
				new TokenItem("二○○一", "m"),
				new TokenItem("年", "q"),
				new TokenItem("27", "m"),
				new TokenItem("日", "q"))
		);
		SpecifiedWordCementer.cementWord(tokenItems);
		assertEquals(2, tokenItems.size());
		assertEquals("二○○一年", tokenItems.get(0).word);
		assertEquals("27日", tokenItems.get(1).word);
		assertEquals("t", tokenItems.get(1).pos);

		tokenItems = new ArrayList<>(Arrays.asList(
				new TokenItem("盛典", "n"),
				new TokenItem("—", "w"),
				new TokenItem("—", "w"),
				new TokenItem("—", "w"),
				new TokenItem("2001", "m"),
				new TokenItem("年", "q"))
		);
		SpecifiedWordCementer.cementWord(tokenItems);
		assertEquals(3, tokenItems.size());
		assertEquals("———", tokenItems.get(1).word);
		assertEquals("2001年", tokenItems.get(2).word);
	}
}
