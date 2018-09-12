package io.github.yizhiru.thulac4j.util;

import io.github.yizhiru.thulac4j.term.CharType;

import java.io.IOException;
import java.util.*;

public final class CharUtils {


	private static final Map<Character, CharType> CORE_CHAR_TYPE_MAP = loadCharTypeMap();

	/**
	 * 空格字符，ASCII码值为32
	 */
	private static final char LATIN_SPACE_CHAR = ' ';

	/**
	 * 前书名号
	 */
	public static final char LEFT_TITLE_QUOTATION_CHAR = '《';

	/**
	 * 后书名号
	 */
	public static final char RIGHT_TITLE_QUOTATION_CHAR = '》';

	/**
	 * 加载核心字符类型词典
	 *
	 * @return 核心字符映射到字符类型 Map
	 */
	private static Map<Character, CharType> loadCharTypeMap() {
		List<String> lines;
		try {
			lines = IOUtils.readLines(CharUtils.class.getResourceAsStream(ModelPaths.CORE_CHAR_PATH));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Map<Character, CharType> map = new HashMap<>(lines.size());
		for (String line : lines) {
			String[] arr = line.split("\t");
			map.put(arr[0].charAt(0), CharType.of(arr[1]));
		}
		return map;
	}

	/**
	 * 映射字符类型
	 *
	 * @param ch 字符
	 * @return 字符类型
	 */
	public static CharType getCharType(char ch) {
		if (isSpaceOrControl(ch)) {
			return CharType.SPACE_OR_CONTROL_CHAR;
		}
		return CORE_CHAR_TYPE_MAP.getOrDefault(ch, CharType.OTHER_CHAR);
	}


	/**
	 * 是否为控制字符或空格字符，在分词过程中忽略这样的字符.
	 *
	 * @param ch 字符
	 * @return 布尔值，若是则返回true
	 */
	public static boolean isSpaceOrControl(char ch) {
		return (ch < LATIN_SPACE_CHAR) || Character.isSpaceChar(ch);
	}


	/**
	 * 字符是否为数字
	 *
	 * @param ch 字符
	 * @return 布尔值
	 */
	public static boolean isNumeral(char ch) {
		CharType charType = getCharType(ch);
		return charType == CharType.CHINESE_NUMERAL_CHAR
				|| charType == CharType.ARABIC_NUMERAL_CHAR;
	}

	/**
	 * 半角字符转全角字符.
	 * 半角空格为32, 全角空格为12288;
	 * 其他半角字符(33-126)与全角字符(65281-65374)均相差 65248.
	 *
	 * @param ch 字符
	 * @return 半角转成的全角字符
	 */
	public static char convertHalfWidth(char ch) {
		if (ch == 32) {
			return (char) 12288;
		} else if (ch > 32 && ch < 127) {
			return (char) (ch + 65248);
		}
		return ch;
	}
}
