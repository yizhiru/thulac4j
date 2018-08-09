package io.github.yizhiru.thulac4j.process;

import io.github.yizhiru.thulac4j.common.DoubleArrayTrie;
import io.github.yizhiru.thulac4j.term.SegItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

/**
 * 基于词典黏结词.
 */
public final class DictCementer implements Serializable {

    private static final long serialVersionUID = 5479588292425956277L;

    /**
     * 词典DAT.
     */
    private final DoubleArrayTrie dat;

    /**
     * 词性.
     */
    private final String pos;

    /**
     * 加载序列化DAT模型.
     *
     * @param inputStream DAT输入流.
     * @param pos         词性.
     */
    public DictCementer(InputStream inputStream, String pos) throws IOException {
        dat = DoubleArrayTrie.loadDat(inputStream);
        this.pos = pos;
    }

    /**
     * 构造器.
     *
     * @param dat DAT.
     * @param pos 词性.
     */
    public DictCementer(DoubleArrayTrie dat, String pos) {
        this.dat = dat;
        this.pos = pos;
    }

    public void cement(List<SegItem> segItems) {
        int index;
        int j;
        for (int i = 0; i < segItems.size(); i++) {
            index = -dat.match(0, segItems.get(i).word);
            if (index <= 0) {
                continue;
            }
            StringBuilder builder = new StringBuilder(segItems.get(i).word);
            for (j = i + 1; j < segItems.size(); j++) {
                int preIndex = index;
                index = -dat.match(index, segItems.get(j).word);
                // 后面的词没有匹配上词典
                if (index <= 0) {
                    index = preIndex;
                    break;
                }
                builder.append(segItems.get(j).word);
            }
            // 若其后的词匹配上词典，则进行黏词
            String word = builder.toString();
            if (dat.isWordMatched(index)) {
                segItems.set(i, new SegItem(word, pos));
                for (j = j - 1; j > i; j--) {
                    segItems.remove(j);
                }
            }
        }
    }
}
