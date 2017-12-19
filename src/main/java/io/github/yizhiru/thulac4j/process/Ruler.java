package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.common.CharUtil;
import io.github.yizhiru.thulac4j.model.POC;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static io.github.yizhiru.thulac4j.common.CharUtil.convertHalfWidth;
import static io.github.yizhiru.thulac4j.common.CharUtil.isDigit;
import static io.github.yizhiru.thulac4j.common.CharUtil.isLetter;
import static io.github.yizhiru.thulac4j.common.CharUtil.isRemainPunctuation;
import static io.github.yizhiru.thulac4j.common.CharUtil.isSinglePunctuation;
import static io.github.yizhiru.thulac4j.common.CharUtil.isSkipped;

/**
 * 分词规则集合.
 */
public final class Ruler {

    /**
     * 清洗结果类.
     */
    public static class CleanedResult {
        /**
         * 清洗后字符、POC二元组
         */
        private List<CharPocTuple> tuples;

        /**
         * 最后一个tuple已提前添加.
         */
        private boolean isAppendAhead;

        public CleanedResult(int initialCapacity) {
            tuples = new ArrayList<>(initialCapacity);
            isAppendAhead = false;
        }

        private static class CharPocTuple {
            /**
             * 句子的原字符.
             */
            public char raw;

            /**
             * 清洗后字符（对应于raw），比如大写转小写、半角转全角.
             */
            public char cleaned;

            /**
             * 可能POC.
             */
            public POC poc;

            public CharPocTuple(char raw, char cleaned, POC poc) {
                this.raw = raw;
                this.cleaned = cleaned;
                this.poc = poc;
            }
        }

        public int getLastIndex() {
            return tuples.size() - 1;
        }

        public char[] getRawSentence() {
            int size = tuples.size();
            char[] rawSentence = new char[size];
            for (int i = 0; i < size; i++) {
                rawSentence[i] = tuples.get(i).raw;
            }
            return rawSentence;
        }

        public char[] getCleanedSentence() {
            int size = tuples.size();
            char[] cleanedSentence = new char[size];
            for (int i = 0; i < size; i++) {
                cleanedSentence[i] = tuples.get(i).cleaned;
            }
            return cleanedSentence;
        }

        public POC[] getSentencePoc() {
            return tuples.stream()
                    .map(t -> t.poc)
                    .toArray(POC[]::new);
        }

        public boolean isEmpty() {
            return tuples.isEmpty();
        }

        /**
         * 对于index位置的POC求交集
         *
         * @param index 所处位置
         * @param poc   可能POC
         */
        private void intersectPoc(int index, POC poc) {
            if (index < 0 || index >= tuples.size()) {
                return;
            }
            CharPocTuple tuple = tuples.get(index);
            tuple.poc = tuple.poc.intersect(poc);
        }

        /**
         * 设置最后CharPocTuple
         *
         * @param ch  字符
         * @param poc 可能的POC
         */
        private void append(char ch, POC poc) {
            if (isAppendAhead) {
                intersectPoc(getLastIndex(), poc);
                isAppendAhead = false;
            } else {
                tuples.add(new CharPocTuple(ch, convertHalfWidth(ch), poc));
            }
        }

        /**
         * 尾部提前追加元素.
         *
         * @param ch  字符
         * @param poc 可能的POC
         */
        private void appendAhead(char ch, POC poc) {
            tuples.add(new CharPocTuple(ch, convertHalfWidth(ch), poc));
            isAppendAhead = true;
        }
    }

    /**
     * 依据规则得到每个字符对应的POC
     *
     * @param sentence 待分词句子
     * @return 清洗后String
     */
    public static CleanedResult ruleClean(String sentence) {
        int len = sentence.length();
        char[] raw = sentence.toCharArray();
        CleanedResult result = new CleanedResult(sentence.length());
        boolean hasTitleBegin = false;
        int titleBegin = 0;
        for (int i = 0; i < len; ) {
            char ch = raw[i];
            // 1. Space or control character
            if (isSkipped(ch)) {
                result.intersectPoc(result.getLastIndex(), POC.ES_POC);
                for (i++; i < len; i++) {
                    if (!isSkipped(raw[i])) {
                        break;
                    }
                }
                // 处理后面字符
                if (i < len) {
                    result.appendAhead(raw[i], POC.BS_POC);
                }
            }

            // 2. Punctuation
            else if (isSinglePunctuation(ch)) {
                result.intersectPoc(result.getLastIndex(), POC.ES_POC);
                result.append(ch, POC.PUNCTUATION_POC);
                // 前书名号
                if (ch == '《') {
                    hasTitleBegin = true;
                    titleBegin = i;
                }
                // 后书名号
                else if (hasTitleBegin && ch == '》') {
                    if (isPossibleTitle(raw, titleBegin + 1, i - 1)) {
                        setWordPoc(result,
                                titleBegin + 1,
                                i - 1,
                                result.getLastIndex() - 1
                        );
                    }
                    hasTitleBegin = false;
                }
                i++;
                // 处理后面字符
                if (i < len && !isSkipped(raw[i])) {
                    result.appendAhead(raw[i], POC.BS_POC);
                }
            }

            // 3. English or Latin words
            else if (isLetter(ch)) {
                i = processWord(raw,
                        result,
                        i,
                        CharUtil::mayBeLetterWord,
                        false);
            }

            // 4. Numbers
            else if (isDigit(ch)) {
                i = processWord(raw,
                        result,
                        i,
                        CharUtil::mayBeNumeral,
                        true);
            }

            // 5. 余下标点
            else if (isRemainPunctuation(ch)) {
                result.intersectPoc(result.getLastIndex(), POC.ES_POC);
                result.append(ch, POC.PUNCTUATION_POC);
                result.appendAhead(raw[i + 1], POC.BS_POC);
                i++;
            }

            // 6. Else
            else {
                result.append(ch, POC.DEFAULT_POC);
                i++;
            }
        }
        result.intersectPoc(0, POC.BS_POC);
        result.intersectPoc(result.getLastIndex(), POC.ES_POC);
        return result;
    }

    /**
     * 判断前后书名号内的字符串是否为能成词
     *
     * @param sentence 待分词句子
     * @param start    前书名号《 后一个index
     * @param end      后书名号》前一个index
     * @return 若能则true
     */
    private static boolean isPossibleTitle(char[] sentence, int start, int end) {
        if (end - start > 8 || end - start <= 0) {
            return false;
        }
        for (int i = start; i <= end; i++) {
            if (isSinglePunctuation(sentence[i]) || isSkipped(sentence[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 设置单词或连续数字POC
     *
     * @param result    清洗句子结果
     * @param wordStart 词的起始位置（在句子中的index值）
     * @param wordEnd   词的结束位置（在句子中的index值）
     * @param endIndex  wordEnd对应在result的index值
     */
    private static void setWordPoc(
            CleanedResult result,
            int wordStart,
            int wordEnd,
            int endIndex) {
        // 单独字符成词
        if (wordStart == wordEnd) {
            result.intersectPoc(endIndex, POC.SINGLE_POC);
            return;
        }
        // 对应起始位置
        int startIndex = endIndex - wordEnd + wordStart;
        result.intersectPoc(startIndex, POC.BEGIN_POC);
        for (startIndex++; startIndex < endIndex; startIndex++) {
            result.intersectPoc(startIndex, POC.MIDDLE_POC);
        }
        result.intersectPoc(endIndex, POC.END_POC);
    }

    /**
     * 处理单词或连续数字
     *
     * @param wordStart 在字符串raw中的起始位置
     * @param condition 函数式接口，判断是否为字母或数字
     * @param isNumeral 单词or数字
     * @return 词结束后的下一个字符所处位置
     */
    private static int processWord(
            char[] sentence,
            CleanedResult result,
            int wordStart,
            Predicate<Character> condition,
            boolean isNumeral) {
        POC b, m, e, s;
        if (isNumeral) {
            b = POC.BEGIN_M_POC;
            m = POC.MIDDLE_M_POC;
            e = POC.END_M_POC;
            s = POC.SINGLE_M_POC;
        } else {
            b = POC.BEGIN_POC;
            m = POC.MIDDLE_POC;
            e = POC.END_POC;
            s = POC.SINGLE_POC;
        }

        // 处理前一字符
        result.intersectPoc(result.getLastIndex(), POC.ES_POC);

        int len = sentence.length;
        int i = wordStart;
        i++;
        // 单独成词
        if (i == len
                || (i < len && !condition.test(sentence[i]))) {
            result.append(sentence[i - 1], s);
        }
        // 连续成词
        else {
            result.append(sentence[i - 1], b);
            for (; i + 1 < len && condition.test(sentence[i + 1]); i++) {
                result.append(sentence[i], m);
            }
            result.append(sentence[i], e);
            i++;
        }
        // 处理成词后的下一字符
        if (i < len && !isSkipped(sentence[i])) {
            result.appendAhead(sentence[i], POC.BS_POC);
        }
        return i;
    }
}

