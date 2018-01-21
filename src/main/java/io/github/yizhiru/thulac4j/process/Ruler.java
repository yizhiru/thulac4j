package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.common.CharUtils;
import io.github.yizhiru.thulac4j.model.POC;

import java.util.Arrays;
import java.util.function.Predicate;

import static io.github.yizhiru.thulac4j.common.CharUtils.convertHalfWidth;
import static io.github.yizhiru.thulac4j.common.CharUtils.isDigit;
import static io.github.yizhiru.thulac4j.common.CharUtils.isLetter;
import static io.github.yizhiru.thulac4j.common.CharUtils.isRemainPunctuation;
import static io.github.yizhiru.thulac4j.common.CharUtils.isSinglePunctuation;
import static io.github.yizhiru.thulac4j.common.CharUtils.isSkipped;
import static io.github.yizhiru.thulac4j.model.CwsModel.NGramFeature.BOUNDARY;

/**
 * 分词规则集合.
 */
public final class Ruler {

    /**
     * 清洗结果类.
     */
    public static class CleanedSentence {

        /**
         * 句子的原字符串.
         */
        public char[] raw;

        /**
         * 清洗后字符串（对应于raw），为半角转全角.
         */
        public char[] cleaned;

        /**
         * 可能POC.
         */
        public POC[] pocs;

        /**
         * 清洗后句子长度.
         */
        private int length;

        /**
         * 最后一个tuple已提前添加.
         */
        private boolean isAppendAhead;

        public CleanedSentence(int initialCapacity) {
            raw = new char[initialCapacity];
            cleaned = new char[initialCapacity];
            pocs = new POC[initialCapacity];
            length = 0;
            isAppendAhead = false;
        }

        /**
         * 规则清洗后的句子长度.
         *
         * @return 清洗后句子长度
         */
        public int length() {
            return this.length;
        }

        public char[] getRawSentence() {
            return Arrays.copyOfRange(raw, 0, length);
        }

        public char[] getCleanedSentence() {
            return Arrays.copyOfRange(cleaned, 0, length);
        }

        public POC[] getSentencePoc() {
            return Arrays.copyOfRange(pocs, 0, length);
        }

        /**
         * 规则清洗后的字符串是否为空
         *
         * @return 若为空，则为true
         */
        public boolean isEmpty() {
            return length == 0;
        }

        /**
         * 首尾拼接BOUNDARY 字符
         *
         * @return 拼接后的字符串
         */
        public char[] insertBoundary() {
            char[] array = new char[length + 4];
            System.arraycopy(cleaned, 0, array, 2, length);
            array[0] = array[1] = array[length + 2] = array[length + 3] = BOUNDARY;
            return array;
        }

        /**
         * 对于index位置的POC求交集
         *
         * @param index 所处位置
         * @param poc   可能POC
         */
        private void intersectPoc(int index, POC poc) {
            if (index < 0 || index >= length) {
                return;
            }
            pocs[index] = pocs[index].intersect(poc);
        }

        /**
         * 按照index 值设置 poc
         *
         * @param index 索引值
         * @param poc   POC
         */
        private void setPocByIndex(int index, POC poc) {
            if (index < 0 || index >= length) {
                return;
            }
            pocs[index] = poc;
        }

        /**
         * 设置最后CharPocTuple
         *
         * @param ch  字符
         * @param poc 可能的POC
         */
        private void append(char ch, POC poc) {
            if (isAppendAhead) {
                intersectPoc(length - 1, poc);
                isAppendAhead = false;
            } else {
                raw[length] = ch;
                cleaned[length] = convertHalfWidth(ch);
                pocs[length] = poc;
                length++;
            }
        }

        /**
         * 尾部提前追加元素.
         *
         * @param ch  字符
         * @param poc 可能的POC
         */
        private void appendAhead(char ch, POC poc) {
            raw[length] = ch;
            cleaned[length] = convertHalfWidth(ch);
            pocs[length] = poc;
            length++;
            isAppendAhead = true;
        }
    }

    /**
     * 依据规则得到每个字符对应的POC
     *
     * @param sentence 待分词句子
     * @return 清洗后String
     */
    public static CleanedSentence ruleClean(String sentence, boolean isEnableTileWord) {
        int len = sentence.length();
        char[] raw = sentence.toCharArray();
        CleanedSentence cleanedSentence = new CleanedSentence(sentence.length());
        boolean hasTitleBegin = false;
        int titleBegin = 0;
        for (int i = 0; i < len; ) {
            char ch = raw[i];
            // 1. Space or control character
            if (isSkipped(ch)) {
                cleanedSentence.intersectPoc(cleanedSentence.length() - 1, POC.ES_POC);
                for (i++; i < len; i++) {
                    if (!isSkipped(raw[i])) {
                        break;
                    }
                }
                // 处理后面字符
                if (i < len) {
                    cleanedSentence.appendAhead(raw[i], POC.BS_POC);
                }
            }

            // 2. Punctuation
            else if (isSinglePunctuation(ch)) {
                cleanedSentence.intersectPoc(cleanedSentence.length() - 1, POC.ES_POC);
                cleanedSentence.append(ch, POC.PUNCTUATION_POC);
                if (isEnableTileWord) {
                    // 前书名号
                    if (ch == '《') {
                        hasTitleBegin = true;
                        titleBegin = i;
                    }
                    // 后书名号
                    else if (hasTitleBegin && ch == '》') {
                        if (isPossibleTitle(raw, titleBegin + 1, i - 1)) {
                            setTitleWordPoc(cleanedSentence,
                                    titleBegin + 1,
                                    i - 1,
                                    cleanedSentence.length() - 2);
                        }
                        hasTitleBegin = false;
                    }
                }
                i++;
                // 处理后面字符
                if (i < len && !isSkipped(raw[i])) {
                    cleanedSentence.appendAhead(raw[i], POC.BS_POC);
                }
            }

            // 3. English or Latin words
            else if (isLetter(ch)) {
                i = processWord(raw,
                        cleanedSentence,
                        i,
                        CharUtils::mayBeLetterWord,
                        false);
            }

            // 4. Numbers
            else if (isDigit(ch)) {
                i = processWord(raw,
                        cleanedSentence,
                        i,
                        CharUtils::mayBeNumeral,
                        true);
            }

            // 5. 余下标点
            else if (isRemainPunctuation(ch)) {
                cleanedSentence.intersectPoc(cleanedSentence.length() - 1, POC.ES_POC);
                cleanedSentence.append(ch, POC.PUNCTUATION_POC);
                i++;
                if (i < len && !isSkipped(raw[i])) {
                    cleanedSentence.appendAhead(raw[i], POC.BS_POC);
                }
            }

            // 6. Else
            else {
                cleanedSentence.append(ch, POC.DEFAULT_POC);
                i++;
            }
        }
        cleanedSentence.intersectPoc(0, POC.BS_POC);
        cleanedSentence.intersectPoc(cleanedSentence.length() - 1, POC.ES_POC);
        return cleanedSentence;
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
     * 设置书名号内为一个词.
     *
     * @param result    清洗句子结果
     * @param wordStart 词的起始位置（在句子中的index值）
     * @param wordEnd   词的结束位置（在句子中的index值）
     * @param endIndex  wordEnd对应在result的index值
     */
    private static void setTitleWordPoc(
            CleanedSentence result,
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
        result.setPocByIndex(startIndex, POC.BEGIN_POC);
        for (startIndex++; startIndex < endIndex; startIndex++) {
            result.setPocByIndex(startIndex, POC.MIDDLE_POC);
        }
        result.setPocByIndex(endIndex, POC.END_POC);
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
            CleanedSentence result,
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
        result.intersectPoc(result.length() - 1, POC.ES_POC);

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

