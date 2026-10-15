package io.github.yizhiru.thulac4j.common;

import io.github.yizhiru.thulac4j.util.ModelPaths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class DoubleArrayTrieTest {
	@Rule public TemporaryFolder temporary = new TemporaryFolder();

	@Test public void copiesInputAcceptsImmutableListsAndDeduplicates() {
		List<String> words = new java.util.ArrayList<>(java.util.Arrays.asList("甲", "乙", "甲"));
		DoubleArrayTrie dat = DoubleArrayTrie.make(java.util.Collections.unmodifiableList(words));
		assertEquals(java.util.Arrays.asList("甲", "乙", "甲"), words);
		assertTrue(dat.isWordMatched("甲"));
		assertTrue(dat.isWordMatched("乙"));
		assertEquals(2, DoubleArrayTrie.restore(dat).size());
		assertFalse(DoubleArrayTrie.make(java.util.Collections.emptyList()).isWordMatched("甲"));
		assertThrows(IllegalArgumentException.class, () -> DoubleArrayTrie.make(java.util.Arrays.asList("甲", "")));
		assertThrows(IllegalArgumentException.class, () -> DoubleArrayTrie.make(java.util.Arrays.asList("甲", null)));
	}

	@Test
	public void isMatched() throws IOException {
		DoubleArrayTrie dat = DoubleArrayTrie.loadDat("." + ModelPaths.NS_BIN_PATH);
		assertTrue(dat.isPrefixMatched("黑龙江"));
		assertTrue(dat.isWordMatched("黑龙江"));
		assertTrue(dat.isWordMatched("齐齐哈尔"));
		assertTrue(dat.isWordMatched("名古屋"));
		assertTrue(dat.isWordMatched("克拉约瓦"));
		assertTrue(dat.isWordMatched("１０月９日街"));
		assertTrue(dat.isWordMatched("鸡公？"));
		assertTrue(dat.isWordMatched("齐白石纪念馆"));
		assertTrue(dat.isWordMatched("龙格伦吉里"));
		assertTrue(dat.isWordMatched("特德本－圣玛丽"));
		assertFalse(dat.isWordMatched("首乌"));
	}

	@Test
	public void serialize() throws IOException {
		String[] dictPaths = new String[]{
				ModelPaths.IDIOM_DICT_PATH,
				ModelPaths.NS_DICT_PATH,
				ModelPaths.STOP_WORDS_DICT_PATH,
		};
		String[] binPaths = new String[]{
				"." + ModelPaths.IDIOM_BIN_PATH,
				"." + ModelPaths.NS_BIN_PATH,
				"." + ModelPaths.STOP_WORDS_BIN_PATH,
		};
		for (int i = 0; i < dictPaths.length; i++) {
			DoubleArrayTrie expect = DoubleArrayTrie.make(dictPaths[i]);
			String output = temporary.newFile().getAbsolutePath();
			expect.serialize(output);
			DoubleArrayTrie actual = DoubleArrayTrie.loadDat(output);

			assertEquals(expect.size(), actual.size());
			for (int j = 0; j < expect.size(); j++) {
				assertEquals(expect.getBaseByIndex(j), actual.getBaseByIndex(j));
				assertEquals(expect.getCheckByIndex(j), actual.getCheckByIndex(j));
			}
		}
	}

	@Test
	public void make() throws IOException {
		String[] paths = new String[]{
				ModelPaths.NS_DICT_PATH,
				ModelPaths.IDIOM_DICT_PATH,
				ModelPaths.STOP_WORDS_DICT_PATH
		};
		for (String path : paths) {
			List<String> lexicon = Files.readAllLines(Paths.get(path)).stream()
					.map(String::trim)
					.collect(Collectors.toList());
			DoubleArrayTrie dat = DoubleArrayTrie.make(path);
			for (String word : lexicon) {
				if (word.length() > 1) {
					assertTrue(dat.isPrefixMatched(word.substring(0, word.length() - 1)));
				}
				assertTrue(dat.isWordMatched(word));
			}
		}
	}

	@Test
	public void restore() throws IOException {
		String[] binPaths = new String[]{
				"." + ModelPaths.NS_BIN_PATH,
				"." + ModelPaths.IDIOM_BIN_PATH,
				"." + ModelPaths.STOP_WORDS_BIN_PATH
		};
		String[] dictPaths = new String[]{
				ModelPaths.NS_DICT_PATH,
				ModelPaths.IDIOM_DICT_PATH,
				ModelPaths.STOP_WORDS_DICT_PATH
		};

		for (int i = 0; i < binPaths.length; i++) {
			DoubleArrayTrie dat = DoubleArrayTrie.loadDat(binPaths[i]);
			Set<String> dict = Files.readAllLines(Paths.get(dictPaths[i])).stream()
					.map(String::trim)
					.collect(Collectors.toSet());
			List<String> words = DoubleArrayTrie.restore(dat);
			for (String word : words) {
				assertTrue(dict.contains(word));
			}
			assertEquals(dict.size(), words.size());
		}
	}
}
