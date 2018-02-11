package io.github.yizhiru.thulac4j.dat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * To make double array trie.
 */
public final class DatMaker {

    private static class Builder extends Dat {

        private static final long serialVersionUID = 1675990036852836829L;

        /**
         * 标记可用的base index值.
         */
        private int availableBaseIndex;

        /**
         * Initial value.
         */
        private static final int INITIAL_VALUE = -1;

        public Builder() {
            baseArr = new int[]{0};
            checkArr = new int[]{INITIAL_VALUE};
            size = 1;
            availableBaseIndex = 0;
        }

        /**
         * Expand two size.
         */
        private void expand() {
            int oldCapacity = size;
            int newCapacity = oldCapacity << 1;
            baseArr = Arrays.copyOf(baseArr, newCapacity);
            Arrays.fill(baseArr, oldCapacity, newCapacity, INITIAL_VALUE);
            checkArr = Arrays.copyOf(checkArr, newCapacity);
            Arrays.fill(checkArr, oldCapacity, newCapacity, INITIAL_VALUE);

            size = newCapacity;
        }

        /**
         * Remove useless base and check.
         */
        private void shrink() {
            for (int i = checkArr.length - 1; i >= 0; i--) {
                if (checkArr[i] == INITIAL_VALUE) {
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
                if (checkArr[bi] >= 0) {
                    continue;
                }
                boolean isValid = true;
                for (Integer c : children) {
                    if (checkArr[bi + c] >= 0) {
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
         * @param preIndex 前缀对应的index
         * @param children 前缀的后一字符集合
         * @param isWord   前缀是否为词
         */
        private void insert(int preIndex, List<Integer> children, boolean isWord) {
            int bi = findBaseIndex(children);
            baseArr[preIndex] = bi;
            if (isWord) {
                checkArr[bi] = preIndex;
                availableBaseIndex = bi + 1;
            }
            for (int c : children) {
                baseArr[bi + c] = 0;
                checkArr[bi + c] = preIndex;
            }
        }

        /**
         * 给定前缀生成后一字符集合
         *
         * @param lexicon 词典
         * @param start   词典开始时的索引位置
         * @param prefix  前缀
         * @return 后一字符集合
         */
        private List<Integer> generateChildren(List<String> lexicon,
                                               int start,
                                               String prefix) {
            List<Integer> children = new LinkedList<>();
            int preLen = prefix.length();
            for (int i = start; i < lexicon.size(); i++) {
                String word = lexicon.get(i);
                if (word.length() < preLen
                        || !word.substring(0, preLen).equals(prefix)) {
                    return children;
                } else if (word.length() > preLen
                        && (children.isEmpty() || ((int) word.charAt(preLen)) != children.get(children.size() - 1))) {
                    children.add((int) word.charAt(preLen));
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
        private Dat build(List<String> lexicon) {
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
                baseArr[baseArr[matched]] = i;
            }
            shrink();
            return new Dat(baseArr, checkArr, size);
        }
    }

    /**
     * Make DAT.
     *
     * @param path file path.
     * @return DAT
     */
    public static Dat make(String path) throws FileNotFoundException {
        return make(new FileInputStream(path));
    }

    /**
     * Make DAT.
     *
     * @param inputStream input stream of file
     * @return DAT
     */
    public static Dat make(InputStream inputStream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        List<String> lexicon = br.lines()
                .map(String::trim)
                .collect(Collectors.toList());
        return new Builder().build(lexicon);
    }
}
