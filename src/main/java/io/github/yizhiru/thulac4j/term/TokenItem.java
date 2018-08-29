package io.github.yizhiru.thulac4j.term;

import io.github.yizhiru.thulac4j.common.Nullable;

/**
 * Word Segment item.
 */
public final class TokenItem {

    /**
     * Tokenized word.
     */
    public final String word;

    /**
     * Part-of-speech.
     */
    @Nullable
    public final String pos;

    public TokenItem(String word, String pos) {
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

        TokenItem tokenItem = (TokenItem) o;
        return (word != null ? word.equals(tokenItem.word) : tokenItem.word == null)
                && (pos != null ? pos.equals(tokenItem.pos) : tokenItem.pos == null);
    }

    @Override
    public int hashCode() {
        int result = word != null ? word.hashCode() : 0;
        result = 31 * result + (pos != null ? pos.hashCode() : 0);
        return result;
    }
}
