package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.util.ModelPaths;
import io.github.yizhiru.thulac4j.term.TokenItem;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LexiconCementerTest {

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
