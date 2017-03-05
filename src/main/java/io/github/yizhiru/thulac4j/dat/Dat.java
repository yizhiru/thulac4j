package io.github.yizhiru.thulac4j.dat;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.zip.InflaterInputStream;

/**
 * @author jyzheng
 */
public class Dat implements Serializable {
  public List<Entry> entries;

  public static class Entry implements Serializable {
    public int base;
    public int check;

    public Entry(int base, int check) {
      this.base = base;
      this.check = check;
    }

    public Entry() {
    }

    @Override
    public String toString() {
      return base + " " + check;
    }
  }

  public Dat(List<Entry> entries) {
    this.entries = entries;
  }

  public Dat() {
  }

  public static Dat loadDat(String path) throws FileNotFoundException {
    return loadDat(new FileInputStream(path));
  }

  public static Dat loadDat(InputStream in) {
    Kryo kryo = new Kryo();
    Input input = new Input(new InflaterInputStream(in));
    Dat dat = kryo.readObject(input, Dat.class);
    input.close();
    return dat;
  }

  /**
   * 按照DAT的转移方程进行转移: base[r] + c = s, check[s] = r
   *
   * @param r 前缀在DAT中的index
   * @param c 转移字符的index
   * @return 转移后在DAT中的index，若不在则为-1
   */
  public int transition(int r, int c) {
    if (r < 0 || r >= entries.size())
      return -1;
    int s = entries.get(r).base + c;
    if (s >= entries.size() || entries.get(s).check != r)
      return -1;
    return s;
  }


  /**
   * 词是否在trie树中
   *
   * @param word 词
   * @return 若存在，则为true
   */
  public boolean isWordMatched(String word) {
    int index = hasMatched(word);
    if (index >= 0) return false;
    index = -index;
    int base = entries.get(index).base;
    return base < entries.size() && entries.get(base).check == index;
  }

  /**
   * 前缀是否在trie树中
   *
   * @param prefix 前缀
   * @return 若存在，则为true
   */
  public boolean isPrefixMatched(String prefix) {
    return hasMatched(prefix) < 0;
  }

  /**
   * 匹配字符串
   *
   * @param str 字符串
   * @return 若匹配上，则为转移后index的负值；否则，则返回已匹配上的字符数
   */
  public int hasMatched(String str) {
    return hasMatched(0, str);
  }

  /**
   * 匹配字符串
   *
   * @param start DAT的开始index
   * @param str   字符串
   * @return 若匹配上，则为转移后index的负值；否则，则返回已匹配上的字符数
   */
  public int hasMatched(int start, String str) {
    int index = start;
    for (int i = 0; i < str.length(); i++) {
      index = transition(index, str.charAt(i));
      if (index == -1)
        return i;
    }
    return -index;
  }

}
