package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.term.TokenItem;
import io.github.yizhiru.thulac4j.util.CharUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 特定词的黏结
 */
public class SpecifiedWordCementer {

	/**
	 * 时间量词
	 */
	private static final Set<String> TIME_UNIT_WORDS = new HashSet<>(
			Arrays.asList("年", "月", "日", "号", "时", "点", "分", "秒"));

	/**
	 * Year time unit.
	 */
	private static final String YEAR_TIME_UNIT_WORD = "年";

	/**
	 * 可黏结的重复特定词
	 */
	private static final Set<String> CAN_FORM_REPEATED_WORDS = new HashSet<>(
			Arrays.asList("—", "…"));


	/**
	 * 黏结词
	 *
	 * @param tokenItems 分词中间结果
	 */
	public static void cementWord(List<TokenItem> tokenItems) {
		for (int i = tokenItems.size() - 1; i > 0; i--) {
			TokenItem item = tokenItems.get(i);
			String word = item.word;
			if (TIME_UNIT_WORDS.contains(word)) {
				i = cementTimeWord(tokenItems, item, i);
			} else if (CAN_FORM_REPEATED_WORDS.contains(word)) {
				i = cementRepeatedWord(tokenItems, item, i);
			}
		}
	}

	/**
	 * 黏结阿拉伯数字与时间量词
	 *
	 * @param tokenItems   分词中间结果
	 * @param timeUnitItem 时间单位词项
	 * @param endIndex     结束索引值
	 * @return 时间词的开始索引位置
	 */
	private static int cementTimeWord(List<TokenItem> tokenItems,
	                                  TokenItem timeUnitItem,
	                                  int endIndex) {
		String timeUit = timeUnitItem.word;
		if (endIndex - 1 >= 0) {
			String previousWord = tokenItems.get(endIndex - 1).word;
			if (isNumeralWord(previousWord)) {
				if (timeUit.equals(YEAR_TIME_UNIT_WORD) && previousWord.length() < 4) {
					return endIndex;
				}
				tokenItems.remove(endIndex);
				StringBuilder builder = new StringBuilder(previousWord + timeUnitItem.word);
				int j = endIndex - 2;
				for (; j >= 0; j--) {
					String w = tokenItems.get(j).word;
					if (isNumeralWord(w)) {
						tokenItems.remove(j + 1);
						builder.insert(0, w);
					} else {
						break;
					}
				}
				tokenItems.set(j + 1,
						new TokenItem(builder.toString(), "t"));
				return j + 1;
			}
		}
		return endIndex;
	}

	/**
	 * 黏结左右相同的特定词
	 *
	 * @param tokenItems   分词中间结果
	 * @param repeatedItem 重复的特定词项
	 * @param endIndex     结束索引值
	 * @return 词的开始索引位置
	 */
	private static int cementRepeatedWord(List<TokenItem> tokenItems,
	                                      TokenItem repeatedItem,
	                                      int endIndex) {
		String word = repeatedItem.word;
		int i = endIndex - 1;
		if (i >= 0 && tokenItems.get(i).word.equals(word)) {
			StringBuilder builder = new StringBuilder(word + word);
			tokenItems.remove(endIndex);
			for (i--; i >= 0 && tokenItems.get(i).word.equals(word); i--) {
				builder.insert(0, word);
				tokenItems.remove(i + 1);
			}
			tokenItems.set(i + 1,
					new TokenItem(builder.toString(), repeatedItem.pos));
		}
		return i + 1;
	}

	/**
	 * 一个词是否全为阿拉伯数字组成.
	 *
	 * @param word 词.
	 * @return 布尔值
	 */
	private static boolean isNumeralWord(String word) {
		for (char ch : word.toCharArray()) {
			if (!CharUtils.isNumeral(ch)) {
				return false;
			}
		}
		return true;
	}
}
