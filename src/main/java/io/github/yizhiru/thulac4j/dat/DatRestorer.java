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
            if (dat.get(i).check >= 0) {
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
        int r, s = index;
        StringBuilder sb = new StringBuilder();
        while (s > 0 && s < dat.size()) {
            r = dat.get(s).check;
            if (r == s || dat.get(r).base >= s) {
                break;
            }
            sb.insert(0, (char) (s - dat.get(r).base));
            s = r;
        }
        return sb.toString();
    }
}
