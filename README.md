# thulac4j

thulac4j是[THULAC](http://thulac.thunlp.org/)的Java 8实现，相比于[THULAC-Java](https://github.com/thunlp/THULAC-Java)，我们做了如下工作：

1. 规范化分词词典，并去掉一些无用词；
2. 重写DAT（双数组Trie树）的构造算法，生成的DAT size减少了8%左右，从而节省了内存；
3. 优化分词算法，提高了分词速率。


## 使用示例

在项目中使用thulac4j，添加依赖：

```xml
<dependency>
  <groupId>io.github.yizhiru</groupId>
  <artifactId>thulac4j</artifactId>
  <version>1.0.1</version>
</dependency>
```

thulac4j支持两种分词模式：

1. SegOnly模式，只分词没有词性标注；
2. SegPos模式，分词兼有词性标注。


```java
// SegOnly mode
String sentence = "滔滔的流水，向着波士顿湾无声逝去";
SegOnly seg = new SegOnly("seg_only.bin");
System.out.println(seg.segment(sentence));
// [滔滔, 的, 流水, ，, 向着, 波士顿湾, 无声, 逝去]

// SegPos mode
SegPos pos = new SegPos("seg_pos.bin");
System.out.println(pos.segment(sentence));
// [滔滔/a, 的/u, 流水/n, ，/w, 向着/p, 波士顿湾/ns, 无声/v, 逝去/v]
```

模型数据较大，没有放在jar包与源码。更多使用说明及特性请参看[Getting Started](https://github.com/yizhiru/thulac4j/wiki).


## ToDo

1. 优化分词词典；
2. 增加自定义分词规则；
3. 停用词过滤。

最后感谢THUNLP实验室！没有你们的努力，便没有这么好用的THULAC，也就没有thulac4j。


