package io.github.yizhiru.thulac4j.util;

import io.github.yizhiru.thulac4j.common.ModelPaths;

import java.io.IOException;
import java.util.HashMap;

public final class ChineseUtils {

    private static final HashMap<Character, Character> T2S_MAP = parseT2sMap();

    private static HashMap<Character, Character> parseT2sMap() {
        int[] array;
        try {
            array = IOUtils.toIntArray(ChineseUtils.class.getResourceAsStream(ModelPaths.T2S_PATH));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // simplified.dat 文件包含繁体字符共有2800个
        int traditionNum = array.length / 2;
        HashMap<Character, Character> t2sMap = new HashMap<>(traditionNum);
        for (int i = 0; i < traditionNum; i++) {
            t2sMap.put((char) array[i], (char) array[i + traditionNum]);
        }
        return t2sMap;
    }

    /**
     * 将繁体汉字转为简体汉字
     *
     * @param sentence 输入句子
     * @return 简体字化句子
     */
    public static String simplified(String sentence) {
        StringBuilder builder = new StringBuilder(sentence.length());
        for (char ch : sentence.toCharArray()) {
            builder.append(T2S_MAP.getOrDefault(ch, ch));
        }
        return builder.toString();
    }
}
