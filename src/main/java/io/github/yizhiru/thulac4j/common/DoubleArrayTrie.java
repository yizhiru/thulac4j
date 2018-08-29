package io.github.yizhiru.thulac4j.common;

import io.github.yizhiru.thulac4j.util.IOUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Double Array Trie (DAT).
 */
public class DoubleArrayTrie implements Serializable {

    private static final long serialVersionUID = 8713857561296693244L;

    public static final int MATCH_FAILURE_INDEX = -1;

    /**
     * Base array.
     */
    protected int[] baseArray;

    /**
     * Check array.
     */
    protected int[] checkArray;


    /**
     * The size of DAT.
     */
    protected int size;

    public DoubleArrayTrie(int[] baseArray, int[] checkArray) {
        if (baseArray.length != checkArray.length) {
            throw new IllegalArgumentException(String.format("The getAnnotatedLength of base array %s != the getAnnotatedLength of check " +
                    "array %s", baseArray.length, checkArray.length));
        }
        this.baseArray = baseArray;
        this.checkArray = checkArray;
        size = baseArray.length;
    }

    public DoubleArrayTrie(int[] baseArray, int[] checkArray, int size) {
        this.baseArray = Arrays.copyOf(baseArray, size);
        this.checkArray = Arrays.copyOf(checkArray, size);
        this.size = size;
    }

    private DoubleArrayTrie() {
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
        return baseArray[index];
    }

    /**
     * Get check value by its index.
     *
     * @param index the index of check array.
     * @return the check value.
     */
    public int getCheckByIndex(int index) {
        ensureValidIndex(index);
        return checkArray[index];
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
        intBuffer.put(baseArray);
        intBuffer.put(checkArray);
        channel.write(byteBuffer);
        channel.close();
    }

    /**
     * 加载序列化DAT模型
     *
     * @param path 文件目录
     * @return DAT模型
     */
    public static DoubleArrayTrie loadDat(String path) throws IOException {
        return loadDat(new FileInputStream(path));
    }

    /**
     * 加载序列化DAT模型
     *
     * @param inputStream 文件输入流
     * @return DAT模型
     */
    public static DoubleArrayTrie loadDat(InputStream inputStream) {
        int[] array;
        try {
            array = IOUtils.toIntArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int arrayLen = array[0];
        int[] baseArr = Arrays.copyOfRange(array, 1, arrayLen + 1);
        int[] checkArr = Arrays.copyOfRange(array, arrayLen + 1, 2 * arrayLen + 1);
        return new DoubleArrayTrie(baseArr, checkArr);
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
        int index = baseArray[prefixIndex] + charValue;
        if (index >= size() || checkArray[index] != prefixIndex) {
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
        int base = baseArray[matchedIndex];
        return base < size() && checkArray[base] == matchedIndex;
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

    private static class Builder extends DoubleArrayTrie {

        private static final long serialVersionUID = 1675990036852836829L;

        /**
         * 标记可用的base index值.
         */
        private int availableBaseIndex;

        /**
         * Initial value.
         */
        private static final int INITIAL_VALUE = -1;

        private Builder() {
            baseArray = new int[]{0};
            checkArray = new int[]{INITIAL_VALUE};
            size = 1;
            availableBaseIndex = 0;
        }

        /**
         * Expand two size.
         */
        private void expand() {
            int oldCapacity = size;
            int newCapacity = oldCapacity << 1;
            baseArray = Arrays.copyOf(baseArray, newCapacity);
            Arrays.fill(baseArray, oldCapacity, newCapacity, INITIAL_VALUE);
            checkArray = Arrays.copyOf(checkArray, newCapacity);
            Arrays.fill(checkArray, oldCapacity, newCapacity, INITIAL_VALUE);

            size = newCapacity;
        }

        /**
         * Remove useless base and check.
         */
        private void shrink() {
            for (int i = checkArray.length - 1; i >= 0; i--) {
                if (checkArray[i] == INITIAL_VALUE) {
                    size--;
                } else {
                    break;
                }
            }
        }

        /**
         * 找到满足条件的baseIndex
         *
         * @param children 前缀的后一字符集合
         * @return baseIndex
         */
        private int findBaseIndex(List<Integer> children) {
            int cSize = children.size();
            for (int bi = availableBaseIndex; ; bi++) {
                if (bi == size()) {
                    expand();
                }
                if (cSize > 0) {
                    while (bi + children.get(cSize - 1) >= size()) {
                        expand();
                    }
                }
                // baseIndex应满足条件：
                // 1. 未被使用
                // 2. 满足所有children跳转到的node也未被使用
                if (checkArray[bi] >= 0) {
                    continue;
                }
                boolean isValid = true;
                for (Integer c : children) {
                    if (checkArray[bi + c] >= 0) {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    return bi;
                }
            }
        }

        /**
         * 插入到Trie树
         *
         * @param prefixIndex 前缀对应的index
         * @param children 前缀的后一字符集合
         * @param isWord   前缀是否为词
         */
        private void insert(int prefixIndex, List<Integer> children, boolean isWord) {
            int bi = findBaseIndex(children);
            baseArray[prefixIndex] = bi;
            if (isWord) {
                checkArray[bi] = prefixIndex;
                availableBaseIndex = bi + 1;
            }
            for (int c : children) {
                baseArray[bi + c] = 0;
                checkArray[bi + c] = prefixIndex;
            }
        }

        /**
         * 给定前缀生成后一字符集合
         *
         * @param sortedLexicon     按字典序排序后的词典
         * @param startLexiconIndex 词典开始时的索引位置
         * @param prefix            前缀
         * @return 后一字符集合
         */
        private List<Integer> generateChildren(List<String> sortedLexicon,
                                               int startLexiconIndex,
                                               String prefix) {
            List<Integer> children = new LinkedList<>();
            int prefixLen = prefix.length();
            for (int i = startLexiconIndex; i < sortedLexicon.size(); i++) {
                String word = sortedLexicon.get(i);
                // 停止循环条件：
                // 1. 词的长度小于前缀长度
                // 2. 词的前缀与给定前缀不一致
                if (word.length() < prefixLen
                        || !word.substring(0, prefixLen).equals(prefix)) {
                    return children;
                } else if (word.length() > prefixLen) {
                    int charValue = (int) word.charAt(prefixLen);
                    if (children.isEmpty() || charValue != children.get(children.size() - 1)) {
                        children.add(charValue);
                    }
                }
            }
            return children;
        }

        /**
         * 构建DAT
         *
         * @param lexicon 词典
         * @return 词典对应的DAT
         */
        private DoubleArrayTrie build(List<String> lexicon) {
            lexicon.sort(String::compareTo);
            String word, prefix;
            int preIndex;
            for (int i = 0; i < lexicon.size(); i++) {
                word = lexicon.get(i);
                int matched = match(word);
                matched = matched < 0 ? word.length() : matched;
                for (int j = matched; j <= word.length(); j++) {
                    prefix = word.substring(0, j);
                    preIndex = -match(prefix);
                    List<Integer> children = generateChildren(lexicon, i, prefix);
                    insert(preIndex, children, j == word.length());
                }
                matched = -match(word);
                baseArray[baseArray[matched]] = i;
            }
            shrink();
            return new DoubleArrayTrie(baseArray, checkArray, size);
        }
    }

    /**
     * Make DAT.
     *
     * @param path file path.
     * @return DAT
     */
    public static DoubleArrayTrie make(String path) throws FileNotFoundException {
        return make(new FileInputStream(path));
    }

    /**
     * Make DAT.
     *
     * @param inputStream input stream of file
     * @return DAT
     */
    public static DoubleArrayTrie make(InputStream inputStream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lexicon = br.lines()
                .map(String::trim)
                .collect(Collectors.toList());
        return make(lexicon);
    }

    public static DoubleArrayTrie make(List<String> lexicon) {
        return new Builder().build(lexicon);
    }

    /**
     * 从DAT 还原成词典.
     *
     * @param dat DAT
     */
    public static List<String> restore(DoubleArrayTrie dat) {
        String word;
        LinkedList<String> list = new LinkedList<>();
        for (int i = 0; i < dat.size(); i++) {
            if (dat.getCheckByIndex(i) >= 0) {
                word = restoreWord(dat, i);
                if (dat.isWordMatched(word)) {
                    list.add(word);
                }
            }
        }
        return list;
    }

    /**
     * Restore word by its last index.
     *
     * @param dat   Double Array Trie
     * @param index the last index of word, i.e. its check >= 0
     * @return word
     */
    private static String restoreWord(DoubleArrayTrie dat, int index) {
        int pre;
        int cur = index;
        StringBuilder sb = new StringBuilder();
        while (cur > 0 && cur < dat.size()) {
            pre = dat.getCheckByIndex(cur);
            if (pre == cur || dat.getBaseByIndex(pre) >= cur) {
                break;
            }
            sb.insert(0, (char) (cur - dat.getBaseByIndex(pre)));
            cur = pre;
        }
        return sb.toString();
    }
}
