package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.common.DoubleArrayTrie;
import io.github.yizhiru.thulac4j.term.TokenItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

/**
 * 基于词典黏结词.
 */
public final class LexiconCementer implements Serializable {

	private static final long serialVersionUID = 5479588292425956277L;

	/**
	 * 词典DAT.
	 */
	private final DoubleArrayTrie dat;

	/**
	 * 词性.
	 */
	private final String pos;

	/**
	 * 加载序列化DAT模型.
	 *
	 * @param inputStream DAT输入流.
	 * @param pos         词性.
	 */
	public LexiconCementer(InputStream inputStream, String pos) throws IOException {
		dat = DoubleArrayTrie.loadDat(inputStream);
		this.pos = pos;
	}

	/**
	 * 构造器.
	 *
	 * @param dat DAT.
	 * @param pos 词性.
	 */
	public LexiconCementer(DoubleArrayTrie dat, String pos) {
		this.dat = dat;
		this.pos = pos;
	}

	public void cement(List<TokenItem> tokenItems) {
		List<TokenItem> source = tokenItems instanceof RandomAccess ? tokenItems : new ArrayList<>(tokenItems);
		List<TokenItem> result = new ArrayList<>(source.size());
		for (int i = 0; i < source.size(); ) {
			int index = 0;
			int lastWordEnd = -1;
			for (int j = i; j < source.size(); j++) {
				index = -dat.match(index, source.get(j).word);
				if (index <= 0) break;
				if (dat.isWordMatched(index)) lastWordEnd = j;
			}
			if (lastWordEnd < 0) {
				result.add(source.get(i++));
			} else {
				StringBuilder word = new StringBuilder();
				for (; i <= lastWordEnd; i++) word.append(source.get(i).word);
				result.add(new TokenItem(word.toString(), pos));
			}
		}
		tokenItems.clear();
		tokenItems.addAll(result);
	}
}
