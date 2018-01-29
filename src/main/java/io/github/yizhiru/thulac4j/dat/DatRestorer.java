package io.github.yizhiru.thulac4j.dat;

import java.util.LinkedList;
import java.util.List;

public final class DatRestorer {

    /**
     * 从DAT 还原成词典.
     *
     * @param dat DAT
     */
    public static List<String> restore(Dat dat) {
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
    private static String restoreWord(Dat dat, int index) {
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
