package io.github.yizhiru.thulac4j.process;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import io.github.yizhiru.thulac4j.base.Util;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.zip.InflaterInputStream;

/**
 * @author jyzheng
 */
public class Simplifier {

  private HashMap<Character, Character> t2sMap;

  public Simplifier() throws FileNotFoundException {
    Kryo kryo = new Kryo();
    Input input = new Input(new InflaterInputStream(
            this.getClass().getResourceAsStream(Util.t2s)));
    t2sMap = kryo.readObject(input, HashMap.class);
    input.close();
  }

  // 将繁体汉字转为简体汉字
  public String t2s(String raw) {
    StringBuilder builder = new StringBuilder(raw.length());
    for (Character ch : raw.toCharArray()) {
      builder.append(t2sMap.getOrDefault(ch, ch));
    }
    return builder.toString();
  }


}
