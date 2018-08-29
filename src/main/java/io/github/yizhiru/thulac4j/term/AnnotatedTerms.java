package io.github.yizhiru.thulac4j.term;

import io.github.yizhiru.thulac4j.util.CharUtils;

import java.util.Arrays;

import static io.github.yizhiru.thulac4j.perceptron.StructuredPerceptronModel.NGramFeature.BOUNDARY;
import static io.github.yizhiru.thulac4j.util.CharUtils.convertHalfWidth;

public final class AnnotatedTerms {

    /**
     * 待分词文本对应的原字符串.
     */
    private char[] rawChars;

    /**
     * 待分词文本对应的字符类型
     */
    private CharType[] rawCharTypes;

    /**
     * 规则标注处理前的字符串，长度与 @annotatedChars相等
     */
    private char[] preAnnotateChars;

    /**
     * 规则标注处理后字符串（对应于raw），包含操作：去除空格字符、半角转全角
     */
    private char[] annotatedChars;

    /**
     * 可能POC.
     */
    public POC[] pocs;

    /**
     * 规则标注后后句子长度.
     */
    private int annotatedLength;

    /**
     * 最后一个tuple是否已提前添加.
     */
    private boolean isAppendAhead;

    /**
     * 构造器
     *
     * @param rawChars 待分词文本字符串
     */
    public AnnotatedTerms(char[] rawChars) {
        this.rawChars = rawChars;
        int textLength = rawChars.length;
        // get char type
        this.rawCharTypes = new CharType[textLength];
        for (int i = 0; i < textLength; i++) {
            rawCharTypes[i] = CharUtils.getCharType(rawChars[i]);
        }
        this.preAnnotateChars = new char[textLength];
        this.annotatedChars = new char[textLength];
        this.pocs = new POC[textLength];
        this.annotatedLength = 0;
        this.isAppendAhead = false;
    }

    public char[] getPreAnnotateChars() {
        return Arrays.copyOfRange(preAnnotateChars, 0, annotatedLength);
    }

    public char[] getAnnotatedChars() {
        return Arrays.copyOfRange(annotatedChars, 0, annotatedLength);
    }

    /**
     * 规则标注后的长度.
     *
     * @return 长度
     */
    public int getAnnotatedLength() {
        return this.annotatedLength;
    }

    public POC[] getPocs() {
        return Arrays.copyOfRange(pocs, 0, annotatedLength);
    }

    /**
     * 根据原字符串的索引位置得到字符类型
     *
     * @param rawIndex 原字符串的索引位置
     * @return 字符类型
     */
    public char getRawCharByIndex(int rawIndex) {
        return rawChars[rawIndex];
    }

    /**
     * 根据原字符串的索引位置得到字符类型
     *
     * @param rawIndex 原字符串的索引位置
     * @return 字符类型
     */
    public CharType getCharTypeByIndex(int rawIndex) {
        return rawCharTypes[rawIndex];
    }

    /**
     * 原始字符串长度
     *
     * @return 整数值长度
     */
    public int getRawCharsLength() {
        return rawChars.length;
    }

    /**
     * 结果字符串是否为空
     *
     * @return 若为空，则为true
     */
    public boolean isEmpty() {
        return annotatedLength == 0;
    }

    /**
     * 首尾拼接BOUNDARY 字符
     *
     * @return 拼接后的字符串
     */
    public char[] appendBoundaryAround() {
        char[] array = new char[annotatedLength + 4];
        System.arraycopy(annotatedChars, 0, array, 2, annotatedLength);
        array[0] = array[1] = array[annotatedLength + 2] = array[annotatedLength + 3] = BOUNDARY;
        return array;
    }

    /**
     * 对于index位置的POC求交集
     *
     * @param annotatedIndex 标注字符串的索引位置
     * @param poc            POC值
     */
    public void intersectPocByIndex(int annotatedIndex, POC poc) {
        if (annotatedIndex < 0 || annotatedIndex >= annotatedLength) {
            return;
        }
        pocs[annotatedIndex] = pocs[annotatedIndex].intersect(poc);
    }

    /**
     * 对最后位置POC求交集
     *
     * @param poc POC值
     */
    public void intersectLastPoc(POC poc) {
        intersectPocByIndex(annotatedLength - 1, poc);
    }

    /**
     * 按照index 值设置 poc
     *
     * @param annotatedIndex 标注字符串的索引位置
     * @param poc            POC值
     */
    public void setPocByIndex(int annotatedIndex, POC poc) {
        if (annotatedIndex < 0 || annotatedIndex >= annotatedLength) {
            return;
        }
        pocs[annotatedIndex] = poc;
    }

    /**
     * 添加最后一个
     *
     * @param rawIndex 原字符串的索引位置
     * @param poc      可能的POC
     */
    public void append(int rawIndex, POC poc) {
        if (isAppendAhead) {
            intersectLastPoc(poc);
            isAppendAhead = false;
        } else {
            char ch = rawChars[rawIndex];
            preAnnotateChars[annotatedLength] = ch;
            annotatedChars[annotatedLength] = convertHalfWidth(ch);
            pocs[annotatedLength] = poc;
            annotatedLength++;
        }
    }

    /**
     * 尾部提前追加元素.
     *
     * @param rawIndex 原字符串的索引位置
     * @param poc      可能的POC
     */
    public void appendAhead(int rawIndex, POC poc) {
        char ch = rawChars[rawIndex];
        preAnnotateChars[annotatedLength] = ch;
        annotatedChars[annotatedLength] = convertHalfWidth(ch);
        pocs[annotatedLength] = poc;
        annotatedLength++;
        isAppendAhead = true;
    }
}
