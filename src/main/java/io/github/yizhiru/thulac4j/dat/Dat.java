package io.github.yizhiru.thulac4j.dat;

import io.github.yizhiru.thulac4j.common.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Double Array Trie (DAT).
 */
public class Dat implements Serializable {

    private static final long serialVersionUID = 8713857561296693244L;

    public static final int MATCH_FAILURE_INDEX = -1;

    /**
     * Base array.
     */
    protected int[] baseArr;

    /**
     * Check array.
     */
    protected int[] checkArr;


    /**
     * The size of DAT.
     */
    protected int size;

    public Dat(int[] baseArr, int[] checkArr) {
        if (baseArr.length != checkArr.length) {
            throw new IllegalArgumentException(String.format("The length of base array %s != the length of check " +
                    "array %s", baseArr.length, checkArr.length));
        }
        this.baseArr = baseArr;
        this.checkArr = checkArr;
        size = baseArr.length;
    }

    public Dat(int[] baseArr, int[] checkArr, int size) {
        this.baseArr = Arrays.copyOf(baseArr, size);
        this.checkArr = Arrays.copyOf(checkArr, size);
        this.size = size;
    }

    public Dat() {
    }

    /**
     * The size of DAT.
     *
     * @return size
     */
    public int size() {
        return size;
    }

    /**
     * Ensure the index is not out bound.
     *
     * @param index the index value.
     */
    private void ensureValidIndex(int index) {
        if (index >= size()) {
            throw new RuntimeException(String.format("The index %s is out of bound [%s].",
                    index, size()));
        }
    }

    /**
     * Get base value by its index.
     *
     * @param index the index of base array.
     * @return the base value.
     */
    public int getBaseByIndex(int index) {
        ensureValidIndex(index);
        return baseArr[index];
    }

    /**
     * Get check value by its index.
     *
     * @param index the index of check array.
     * @return the check value.
     */
    public int getCheckByIndex(int index) {
        ensureValidIndex(index);
        return checkArr[index];
    }

    /**
     * 序列化.
     *
     * @param path 文件路径
     */
    public void serialize(String path) throws IOException {
        FileChannel channel = new FileOutputStream(path).getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * (2 * size() + 1));
        IntBuffer intBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        intBuffer.put(size());
        intBuffer.put(baseArr);
        intBuffer.put(checkArr);
        channel.write(byteBuffer);
        channel.close();
    }

    /**
     * 加载序列化DAT模型
     *
     * @param path 文件目录
     * @return DAT模型
     */
    public static Dat loadDat(String path) throws IOException {
        return loadDat(new FileInputStream(path));
    }

    /**
     * 加载序列化DAT模型
     *
     * @param inputStream 文件输入流
     * @return DAT模型
     */
    public static Dat loadDat(InputStream inputStream) throws IOException {
        int[] array = IOUtils.toIntArray(inputStream);
        int arrayLen = array[0];
        int[] baseArr = Arrays.copyOfRange(array, 1, arrayLen + 1);
        int[] checkArr = Arrays.copyOfRange(array, arrayLen + 1, 2 * arrayLen + 1);
        return new Dat(baseArr, checkArr);
    }

    /**
     * 按照DAT的转移方程进行转移: ROOT_PATH[r] + c = s, check[s] = r
     *
     * @param prefixIndex 前缀在DAT中的index
     * @param charValue   转移字符的int值
     * @return 在DAT中的index，若不在则为-1
     */
    public int transition(int prefixIndex, int charValue) {
        if (prefixIndex < 0 || prefixIndex >= size()) {
            return MATCH_FAILURE_INDEX;
        }
        int index = baseArr[prefixIndex] + charValue;
        if (index >= size() || checkArr[index] != prefixIndex) {
            return MATCH_FAILURE_INDEX;
        }
        return index;
    }

    /**
     * 词是否在trie树中
     *
     * @param word 词
     * @return 若存在，则为true
     */
    public boolean isWordMatched(String word) {
        return isWordMatched(-match(word));
    }

    /**
     * 词是否在trie树中
     *
     * @param matchedIndex 已匹配上词前缀的index
     * @return 若存在，则为true
     */
    public boolean isWordMatched(int matchedIndex) {
        if (matchedIndex <= 0) {
            return false;
        }
        int base = baseArr[matchedIndex];
        return base < size() && checkArr[base] == matchedIndex;
    }

    /**
     * 前缀是否在trie树中
     *
     * @param prefix 前缀
     * @return 若存在，则为true
     */
    public boolean isPrefixMatched(String prefix) {
        return match(prefix) < 0;
    }

    /**
     * 匹配字符串.
     *
     * @param str 字符串
     * @return 若匹配上，则为转移后index的负值；否则，则返回已匹配上的字符数
     */
    protected int match(String str) {
        return match(0, str);
    }

    /**
     * 匹配字符串.
     *
     * @param startIndex DAT的开始index
     * @param str        字符串
     * @return 若匹配上，则为转移后index的负值；否则，则返回已匹配上的字符数
     */
    public int match(int startIndex, String str) {
        int index = startIndex;
        for (int i = 0; i < str.length(); i++) {
            index = transition(index, str.charAt(i));
            if (index == MATCH_FAILURE_INDEX) {
                return i;
            }
        }
        return -index;
    }
}
