package io.github.yizhiru.thulac4j;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TokenizerOptionsTest {
	private static List<String> words(SPChineseTokenizer tokenizer, String text) {
		return tokenizer.tokenize(text).stream().map(t -> t.word).collect(Collectors.toList());
	}

	@Test public void isolatesOptionsAndSupportsDisabling() throws Exception {
		SPChineseTokenizer first = TestModels.tokenizer();
		SPChineseTokenizer second = TestModels.tokenizer();
		first.enableConvertToSimplifiedCHN();
		first.enableTitleWord();
		first.enableFilterStopWords();
		assertEquals(Collections.singletonList("台"), words(first, "臺"));
		assertEquals(Collections.singletonList("臺"), words(second, "臺"));
		assertFalse(second.getConfig().titleWord);
		assertFalse(second.getConfig().filterStopWords);
		TokenizerConfig snapshot = first.getConfig();
		first.setConvertToSimplifiedCHN(false);
		first.setTitleWord(false);
		first.setFilterStopWords(false);
		assertTrue(snapshot.convertToSimplifiedChinese);
		assertEquals(Collections.singletonList("臺"), words(first, "臺"));
	}

	@Test public void dictionariesAccumulateReplaceAndClearWithoutAffectingOtherInstances() throws Exception {
		SPChineseTokenizer first = TestModels.tokenizer();
		SPChineseTokenizer second = TestModels.tokenizer();
		List<String> input = new ArrayList<>(Arrays.asList("乙甲", "甲乙", "甲乙"));
		first.addUserWords(Collections.unmodifiableList(input));
		first.addUserWords(Collections.singletonList("丙丁"));
		assertEquals(Arrays.asList("乙甲", "甲乙", "甲乙"), input);
		assertEquals(Collections.singletonList("甲乙"), words(first, "甲乙"));
		assertEquals(Collections.singletonList("丙丁"), words(first, "丙丁"));
		assertEquals(Arrays.asList("甲", "乙"), words(second, "甲乙"));
		first.setUserWords(Collections.singletonList("丙丁"));
		assertEquals(Arrays.asList("甲", "乙"), words(first, "甲乙"));
		first.setUserWords(Collections.emptyList());
		assertEquals(Arrays.asList("丙", "丁"), words(first, "丙丁"));
	}

	@Test public void invalidUpdateLeavesPreviousDictionaryIntact() throws Exception {
		SPChineseTokenizer tokenizer = TestModels.tokenizer();
		tokenizer.addUserWords(Collections.singletonList("甲乙"));
		assertThrows(IllegalArgumentException.class, () -> tokenizer.setUserWords(Arrays.asList("丙丁", " ")));
		assertEquals(Collections.singletonList("甲乙"), words(tokenizer, "甲乙"));
	}

	@Test public void filtersAfterMatchingSoPunctuationRemainsABoundary() throws Exception {
		SPChineseTokenizer tokenizer = TestModels.tokenizer();
		tokenizer.setUserWords(Collections.singletonList("甲乙"));
		tokenizer.enableFilterStopWords();
		assertEquals(Arrays.asList("甲", "乙"), words(tokenizer, "甲，乙"));
		assertEquals(Collections.singletonList("甲乙"), words(tokenizer, "甲乙"));
	}

	@Test public void handlesEmptyWhitespaceAndNull() throws Exception {
		SPChineseTokenizer tokenizer = TestModels.tokenizer();
		assertTrue(tokenizer.tokenize("").isEmpty());
		assertTrue(tokenizer.tokenize(" \t\n").isEmpty());
		assertThrows(NullPointerException.class, () -> tokenizer.tokenize(null));
	}

	@Test public void concurrentCallsObserveCompleteDictionarySnapshots() throws Exception {
		SPChineseTokenizer tokenizer = TestModels.tokenizer();
		ExecutorService pool = Executors.newFixedThreadPool(4);
		try {
			List<Callable<Void>> tasks = new ArrayList<>();
			tasks.add(() -> {
				for (int i = 0; i < 100; i++) {
					tokenizer.setUserWords(i % 2 == 0 ? Collections.singletonList("甲乙") : Collections.emptyList());
				}
				return null;
			});
			for (int t = 0; t < 3; t++) tasks.add(() -> {
				for (int i = 0; i < 300; i++) {
					List<String> result = words(tokenizer, "甲乙");
					assertTrue(result.equals(Arrays.asList("甲", "乙")) || result.equals(Collections.singletonList("甲乙")));
				}
				return null;
			});
			for (Future<Void> future : pool.invokeAll(tasks)) future.get();
		} finally { pool.shutdownNow(); }
	}

	@Test public void concurrentAdditionsAreNotLost() throws Exception {
		SPChineseTokenizer tokenizer = TestModels.tokenizer();
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			Future<?> one = pool.submit(() -> tokenizer.addUserWords(Collections.singletonList("甲乙")));
			Future<?> two = pool.submit(() -> tokenizer.addUserWords(Collections.singletonList("丙丁")));
			one.get(); two.get();
			assertEquals(Collections.singletonList("甲乙"), words(tokenizer, "甲乙"));
			assertEquals(Collections.singletonList("丙丁"), words(tokenizer, "丙丁"));
		} finally { pool.shutdownNow(); }
	}
}
