# 从 3.1.2 迁移到 4.0.0

本文说明 4.0.0 的兼容性变化，主要是词典追加和配置作用域。4.0.0 已发布至 Maven Central；构建检查与发布回执见[发布记录](RELEASE-4.0.0.md)。

## 用户词典

旧版 `addUserWords` 每次覆盖已有用户词典；新版同名方法累加去重。检查重复加载、定时刷新词典的调用点：

```java
// 旧代码：每次刷新时希望覆盖已有词典
// tokenizer.addUserWords(latestWords);

// 新代码：显式替换
 tokenizer.setUserWords(latestWords);

// 只增加新词
 tokenizer.addUserWords(additionalWords);

// 清空用户词典
 tokenizer.setUserWords(java.util.Collections.emptyList());
```

上述方法也有对应的 `Segmenter` 静态入口。`addUserWords(emptyList())` 不会清空词典。调用方的列表不再被排序或修改；词条会去除首尾空白并去重，null 或空白词条会被拒绝。请在装载自定义文本词典时处理注释和空行。

## 配置作用域

旧版配置由所有分词器共享；新版配置属于具体实例。旧代码如果依赖以下调用同时改变已有 `POSTagger` 的行为，需要分别设置：

```java
// 仅修改 Segmenter 的默认实例
Segmenter.enableFilterStopWords();

// 对已创建的 POSTagger 显式开启
 tagger.setFilterStopWords(true);
```

新的独立实例使用默认配置或构造时指定的配置，不继承静态入口的当前设置：

```java
TokenizerConfig config = TokenizerConfig.DEFAULT.withFilterStopWords(true);
SPChineseTokenizer tokenizer = Segmenter.newTokenizer(config);
POSTagger tagger = new POSTagger(weightPath, featurePath, config);
```

所需类型均位于 `io.github.yizhiru.thulac4j` 包。`TokenizerConfig` 不可变，调用 `with...` 后需要使用返回值；实例可用 `setConfig` 整体替换，或用接受 boolean 的方法单独开关。原有 `enable...` 方法仍然可用。

分词过程中分别读取配置与用户词典快照。两次更新不是一个原子事务；业务需要同时切换配置与词典时，先完成新实例配置，再切换业务持有的实例引用。不要在使用期间直接修改共享模型数组或分词器受保护字段。

## 分词结果与输入

以下修复可能影响特定文本的输出，应使用业务语料验证：

| 变化 | 影响 |
| --- | --- |
| 停用词在所有词语合并之后过滤 | 防止删掉标点后误合并原本不相邻的词；用户词与停用词组合结果可能变化 |
| 最长完整词典命中回退 | 更长前缀失败时保留已匹配的短词 |
| 解码累计分数使用 `long` | 原来发生整数累计溢出的极端输入可能获得不同路径 |

null 文本现在明确抛出 `NullPointerException`；空串及全空白输入返回空列表。用户词典为 null 时抛出 `NullPointerException`；null/空白词条抛出 `IllegalArgumentException`。

本次已有语料的分词与词性标注共 234 条输出一致；这不意味着新边界行为或所有业务文本都与旧版完全相同。

## 资源与错误处理

| 接口 | 输入流所有权 |
| --- | --- |
| `StructuredPerceptronModel` 的三个输入流；`SPChineseTokenizer` 对应构造器 | 接管全部流，成功或失败均关闭 |
| `DoubleArrayTrie.loadDat(InputStream)`、`make(InputStream)` | 接管并关闭输入流 |
| `IOUtils.readLines(InputStream)` | 读取 UTF-8，并关闭输入流 |
| `IOUtils.toByteArray`、`toIntArray`、`readLittleEndianInt(s)` | 不关闭输入流，由调用方管理 |

不要在传入接管型接口后继续复用同一个输入流。原先依赖系统默认编码的文本词典需要转换为 UTF-8。损坏尺寸、截断数据、不完整标签等会更早报错；缺失资源不再通过 null 流继续处理。捕获异常时应保留原因链，模型读取在部分无受检异常的入口会包装为 `UncheckedIOException`。

`StructuredPerceptronClassifier.getLabelValues()` 返回数组副本；修改该返回值不会修改模型标签。需要自建模型时，在发布给分词器之前完成模型初始化。

## 验证升级

在仓库根目录下载并校验模型后，运行 `mvn -Pmodel-tests test`。对业务侧重点检查词典刷新、不同实例的配置隔离、停用词与用户词的组合行为，以及自定义模型输入流的使用方式。

完整使用示例见 [README](../README.md)，性能测量和原始数据见[性能报告](../benchmarks/RESULTS.md)。
