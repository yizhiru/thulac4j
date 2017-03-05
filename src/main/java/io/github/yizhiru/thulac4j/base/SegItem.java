package io.github.yizhiru.thulac4j.base;

/**
 * @author jyzheng
 */
public class SegItem {
  public String word;
  public String pos;

  public SegItem(String word, String pos) {
    this.word = word;
    this.pos = pos;
  }

  @Override
  public String toString() {
    return word + '/' + pos;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SegItem item = (SegItem) o;

    if (word != null ? !word.equals(item.word) : item.word != null) return false;
    return pos != null ? pos.equals(item.pos) : item.pos == null;
  }

  @Override
  public int hashCode() {
    int result = word != null ? word.hashCode() : 0;
    result = 31 * result + (pos != null ? pos.hashCode() : 0);
    return result;
  }
}
