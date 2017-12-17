package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.common.ModelPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

/**
 * 繁体转简体.
 */
public final class Simplifier {

    private HashMap<Integer, Integer> t2sMap;

    public Simplifier() throws IOException, URISyntaxException {
        File file = new File(this.getClass()
                .getResource(ModelPath.T2S_PATH)
                .toURI());
        int charNum = (int) file.length() / 8;

        FileChannel channel = new FileInputStream(file).getChannel();
        IntBuffer intBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                .order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        int[] traditions = new int[charNum];
        int[] simples = new int[charNum];
        intBuffer.get(traditions);
        intBuffer.get(simples);
        t2sMap = new HashMap<>(charNum);
        for (int i = 0; i < charNum; i++) {
            t2sMap.put(traditions[i], simples[i]);
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
            int simplifiedChar = t2sMap.getOrDefault((int) ch, (int) ch);
            builder.append((char) simplifiedChar);
        }
        return builder.toString();
    }
}
