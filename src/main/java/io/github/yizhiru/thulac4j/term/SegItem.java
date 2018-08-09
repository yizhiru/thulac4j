package io.github.yizhiru.thulac4j.term;

import io.github.yizhiru.thulac4j.common.Nullable;

/**
 * Word Segment item.
 */
public final class SegItem {

    /**
     * Segmented word.
     */
    public final String word;

    /**
     * Part-of-speech.
     */
    @Nullable
    public final String pos;

    public SegItem(String word, String pos) {
        this.word = word;
        this.pos = pos;
    }

    @Override
    public String toString() {
        if (pos == null) {
            return word;
        }
        return word + '/' + pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SegItem segItem = (SegItem) o;
        return (word != null ? word.equals(segItem.word) : segItem.word == null)
                && (pos != null ? pos.equals(segItem.pos) : segItem.pos == null);
    }

    @Override
    public int hashCode() {
        int result = word != null ? word.hashCode() : 0;
        result = 31 * result + (pos != null ? pos.hashCode() : 0);
        return result;
    }
}
