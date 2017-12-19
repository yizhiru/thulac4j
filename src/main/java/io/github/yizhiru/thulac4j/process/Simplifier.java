package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.common.IOUtils;
import io.github.yizhiru.thulac4j.common.ModelPath;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;

/**
 * 繁体转简体.
 */
public final class Simplifier {

    private HashMap<Character, Character> t2sMap;

    public Simplifier() throws IOException {
        int[] array = IOUtils.toIntArray(
                this.getClass().getResourceAsStream(ModelPath.T2S_PATH)
        );
        // t2s.dat 文件包含繁体字符共有2800个
        int traditionNum = array.length / 2;
        t2sMap = new HashMap<>(traditionNum);
        for (int i = 0; i < traditionNum; i++) {
            t2sMap.put((char) array[i], (char) array[i + traditionNum]);
        }
    }

    /**
     * 将繁体汉字转为简体汉字
     *
     * @param sentence 输入句子
     * @return 简体字化句子
     */
    public String t2s(String sentence) {
        StringBuilder builder = new StringBuilder(sentence.length());
        for (char ch : sentence.toCharArray()) {
            builder.append(t2sMap.getOrDefault(ch, ch));
        }
        return builder.toString();
    }
}
