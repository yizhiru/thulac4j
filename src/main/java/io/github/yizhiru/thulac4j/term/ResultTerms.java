package io.github.yizhiru.thulac4j.term;

import java.util.Arrays;

import static io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel.NGramFeature.BOUNDARY;
import static io.github.yizhiru.thulac4j.util.CharUtils.convertHalfWidth;

public class ResultTerms {

    /**
     * 句子的原字符串.
     */
    private char[] raw;

    /**
     * 清洗后字符串（对应于raw），为半角转全角.
     */
    private char[] cleaned;

    /**
     * 可能POC.
     */
    public POC[] pocs;

    /**
     * 清洗后句子长度.
     */
    private int length;

    /**
     * 最后一个tuple是否已提前添加.
     */
    private boolean isAppendAhead;

    public ResultTerms(int initialCapacity) {
        this.raw = new char[initialCapacity];
        this.cleaned = new char[initialCapacity];
        this.pocs = new POC[initialCapacity];
        this.length = 0;
        this.isAppendAhead = false;
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
     * 结果字符串是否为空
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
    public char[] appendBoundaryAround() {
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
    public void intersectPoc(int index, POC poc) {
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
    public void setPocByIndex(int index, POC poc) {
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
    public void append(char ch, POC poc) {
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
    public void appendTailAhead(char ch, POC poc) {
        raw[length] = ch;
        cleaned[length] = convertHalfWidth(ch);
        pocs[length] = poc;
        length++;
        isAppendAhead = true;
    }
}
