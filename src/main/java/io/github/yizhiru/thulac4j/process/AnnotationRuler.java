package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.term.ResultTerms;
import io.github.yizhiru.thulac4j.util.CharUtils;
import io.github.yizhiru.thulac4j.term.POC;

import java.util.function.Predicate;

import static io.github.yizhiru.thulac4j.util.CharUtils.isDigit;
import static io.github.yizhiru.thulac4j.util.CharUtils.isLetter;
import static io.github.yizhiru.thulac4j.util.CharUtils.isRemainPunctuation;
import static io.github.yizhiru.thulac4j.util.CharUtils.isSinglePunctuation;
import static io.github.yizhiru.thulac4j.util.CharUtils.isSkipped;

/**
 * 借助标点符号、数字等信息，提前确定字符的label.
 */
public final class AnnotationRuler {

    /**
     * 依据规则得到每个字符对应的POC
     *
     * @param sentence 待分词句子
     * @return 清洗后String
     */
    public static ResultTerms annotate(String sentence, boolean isEnableTileWord) {
        int len = sentence.length();
        char[] raw = sentence.toCharArray();
        ResultTerms resultTerms = new ResultTerms(sentence.length());
        boolean hasTitleBegin = false;
        int titleBegin = 0;
        for (int i = 0; i < len; ) {
            char ch = raw[i];
            // 1. Space or control character
            if (isSkipped(ch)) {
                resultTerms.intersectPoc(resultTerms.length() - 1, POC.END_OR_SINGLE_POC);
                for (i++; i < len; i++) {
                    if (!isSkipped(raw[i])) {
                        break;
                    }
                }
                // 处理后面字符
                if (i < len) {
                    resultTerms.appendTailAhead(raw[i], POC.BEGIN_OR_SINGLE_POC);
                }
            }
            // 2. Punctuation
            else if (isSinglePunctuation(ch)) {
                resultTerms.intersectPoc(resultTerms.length() - 1, POC.END_OR_SINGLE_POC);
                resultTerms.append(ch, POC.PUNCTUATION_POC);
                if (isEnableTileWord) {
                    // 前书名号
                    if (ch == '《') {
                        hasTitleBegin = true;
                        titleBegin = i;
                    }
                    // 后书名号
                    else if (hasTitleBegin && ch == '》') {
                        if (isPossibleTitle(raw, titleBegin + 1, i - 1)) {
                            setTitleWordPoc(resultTerms,
                                    titleBegin + 1,
                                    i - 1,
                                    resultTerms.length() - 2);
                        }
                        hasTitleBegin = false;
                    }
                }
                i++;
                // 处理后面字符
                if (i < len && !isSkipped(raw[i])) {
                    resultTerms.appendTailAhead(raw[i], POC.BEGIN_OR_SINGLE_POC);
                }
            }
            // 3. English or Latin words
            else if (isLetter(ch)) {
                i = processWord(raw,
                        resultTerms,
                        i,
                        CharUtils::mayBeLetterWord,
                        false);
            }
            // 4. Numbers
            else if (isDigit(ch)) {
                i = processWord(raw,
                        resultTerms,
                        i,
                        CharUtils::mayBeNumeral,
                        true);
            }
            // 5. 余下标点
            else if (isRemainPunctuation(ch)) {
                resultTerms.intersectPoc(resultTerms.length() - 1, POC.END_OR_SINGLE_POC);
                resultTerms.append(ch, POC.PUNCTUATION_POC);
                i++;
                if (i < len && !isSkipped(raw[i])) {
                    resultTerms.appendTailAhead(raw[i], POC.BEGIN_OR_SINGLE_POC);
                }
            }
            // 6. Else
            else {
                resultTerms.append(ch, POC.DEFAULT_POC);
                i++;
            }
        }
        resultTerms.intersectPoc(0, POC.BEGIN_OR_SINGLE_POC);
        resultTerms.intersectPoc(resultTerms.length() - 1, POC.END_OR_SINGLE_POC);
        return resultTerms;
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
            ResultTerms result,
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
            ResultTerms resultTerms,
            int wordStart,
            Predicate<Character> condition,
            boolean isNumeral) {
        POC b, m, e, s;
        if (isNumeral) {
            b = POC.BEGIN_NUMERAL_POC;
            m = POC.MIDDLE_NUMERAL_POC;
            e = POC.END_NUMERAL_POC;
            s = POC.SINGLE_NUMERAL_POC;
        } else {
            b = POC.BEGIN_POC;
            m = POC.MIDDLE_POC;
            e = POC.END_POC;
            s = POC.SINGLE_POC;
        }

        // 处理前一字符
        resultTerms.intersectPoc(resultTerms.length() - 1, POC.END_OR_SINGLE_POC);

        int len = sentence.length;
        int i = wordStart;
        i++;
        // 单独成词
        if (i == len
                || (i < len && !condition.test(sentence[i]))) {
            resultTerms.append(sentence[i - 1], s);
        }
        // 连续成词
        else {
            resultTerms.append(sentence[i - 1], b);
            for (; i + 1 < len && condition.test(sentence[i + 1]); i++) {
                resultTerms.append(sentence[i], m);
            }
            resultTerms.append(sentence[i], e);
            i++;
        }
        // 处理成词后的下一字符
        if (i < len && !isSkipped(sentence[i])) {
            resultTerms.appendTailAhead(sentence[i], POC.BEGIN_OR_SINGLE_POC);
        }
        return i;
    }
}

