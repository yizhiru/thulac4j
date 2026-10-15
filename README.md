# thulac4j

[THULAC](http://thulac.thunlp.org/) 的 Java 实现，支持中文分词、词性标注、自定义词典、繁体转简体和停用词过滤。运行环境为 Java 8 或更高版本。

[快速开始](#快速开始) · [配置与词典](#配置与词典) · [模型与源码构建](#模型与源码构建) · [测试与性能](#测试与性能) · [更新日志](CHANGELOG.md) · [升级指南](docs/MIGRATION-4.0.md)

> 本文对应 **4.0.0**。从 3.1.2 升级时，请先阅读[迁移指南](docs/MIGRATION-4.0.md)，重点检查词典替换与配置作用域。已发布至 Maven Central，验证结果见[发布记录](docs/RELEASE-4.0.0.md)。

## 快速开始

### 引入依赖

4.0.0 的 Maven 依赖坐标如下：

```xml
<dependency>
    <groupId>io.github.yizhiru</groupId>
    <artifactId>thulac4j</artifactId>
    <version>4.0.0</version>
</dependency>
```

4.0.0 已发布至 [Maven Central](https://central.sonatype.com/artifact/io.github.yizhiru/thulac4j/4.0.0)，可直接使用上述依赖。需要从源码构建时，参见[模型与源码构建](#模型与源码构建)。

### 中文分词

```java
import io.github.yizhiru.thulac4j.Segmenter;
import java.util.List;

public class SegmentExample {
    public static void main(String[] args) {
        List<String> words = Segmenter.segment("滔滔的流水，向着波士顿湾无声逝去");
        System.out.println(words);
        // [滔滔, 的, 流水, ，, 向着, 波士顿湾, 无声, 逝去]
    }
}
```

`Segmenter.segment` 返回词语列表，首次调用会加载 jar 中的分词模型，后续调用复用模型。

### 词性标注

词性标注需要单独提供两个 POS 模型文件，下载方式见下文。路径相对于应用启动目录，也可以传入绝对路径。

```java
import io.github.yizhiru.thulac4j.POSTagger;
import io.github.yizhiru.thulac4j.term.TokenItem;
import java.util.List;

public class TaggingExample {
    public static void main(String[] args) throws Exception {
        POSTagger tagger = new POSTagger(
                "models/model_c_model.bin", "models/model_c_dat.bin");
        List<TokenItem> tokens = tagger.tagging("滔滔的流水，向着波士顿湾无声逝去");
        System.out.println(tokens);
        // [滔滔/a, 的/u, 流水/n, ，/w, 向着/p, 波士顿湾/ns, 无声/v, 逝去/v]
    }
}
```

`TokenItem.word` 是词语，`TokenItem.pos` 是词性；无词性标签时 `pos` 为 `null`。服务中应复用已创建的 `POSTagger`，避免每次请求重新加载模型。

## 配置与词典

本节介绍 4.0.0 的配置接口。多业务或服务端使用独立实例，避免配置相互影响。

### 创建独立实例

```java
import io.github.yizhiru.thulac4j.Segmenter;
import io.github.yizhiru.thulac4j.SPChineseTokenizer;
import io.github.yizhiru.thulac4j.TokenizerConfig;
import io.github.yizhiru.thulac4j.term.TokenItem;
import java.util.List;

public class ConfigExample {
    public static void main(String[] args) {
        TokenizerConfig config = TokenizerConfig.DEFAULT
                .withTitleWord(true)
                .withFilterStopWords(true)
                .withConvertToSimplifiedChinese(true);
        SPChineseTokenizer tokenizer = Segmenter.newTokenizer(config);
        List<TokenItem> tokens = tokenizer.tokenize("繁體中文分詞");
        System.out.println(tokens);

        tokenizer.setFilterStopWords(false);
        tokenizer.setConfig(TokenizerConfig.DEFAULT); // 恢复所有默认开关
    }
}
```

`TokenizerConfig` 不可变，`with...` 方法返回新的配置对象。三个选项默认均为 `false`：

| 功能 | 创建配置时 | 更新实例时 |
| --- | --- | --- |
| 书名号内成词规则 | `withTitleWord(boolean)` | `setTitleWord(boolean)` |
| 过滤内置停用词 | `withFilterStopWords(boolean)` | `setFilterStopWords(boolean)` |
| 繁体转简体 | `withConvertToSimplifiedChinese(boolean)` | `setConvertToSimplifiedCHN(boolean)` |

`POSTagger` 可通过第三个构造参数接收同一配置，例如 `new POSTagger(weightPath, featurePath, config)`。开启繁转简后，输出词语也使用转换后的文字。

`Segmenter.newTokenizer()` 创建默认配置实例；这些实例共享分词模型和内置词典，配置与用户词典分别保存。`Segmenter.segment` 及静态配置方法则操作同一个默认实例。调用 `Segmenter.enableFilterStopWords()` 不会影响其他独立实例或 `POSTagger`。

### 添加、替换与清空词典

```java
import io.github.yizhiru.thulac4j.Segmenter;
import io.github.yizhiru.thulac4j.SPChineseTokenizer;
import java.util.Arrays;
import java.util.Collections;

public class DictionaryExample {
    public static void main(String[] args) {
        SPChineseTokenizer tokenizer = Segmenter.newTokenizer();
        tokenizer.addUserWords(Arrays.asList("自然语言处理", "用户词典"));
        tokenizer.addUserWords(Collections.singletonList("机器学习")); // 累加
        System.out.println(tokenizer.tokenize("机器学习用于自然语言处理"));

        tokenizer.setUserWords(Collections.singletonList("中文分词")); // 整体替换
        tokenizer.setUserWords(Collections.emptyList()); // 清空
    }
}
```

词条自动去除首尾空白、去重，传入列表不会被修改，支持不可修改列表。列表为 `null` 时抛出 `NullPointerException`，词条为 `null` 或去除首尾空白后为空时抛出 `IllegalArgumentException`。`addUserWords` 传入空列表不会清空已有词典。

用户词典在模型分词后进行词语合并，命中采用最长完整匹配；它不负责把模型已切出的词进一步拆分。需要替换旧词典时使用 `setUserWords`，升级前后的语义差异见[迁移指南](docs/MIGRATION-4.0.md)。

### 并发与输入约定

同一实例支持并发分词；每次调用分别读取一次配置和用户词典快照，词典构建完成后整体发布，并发追加不会丢失另一笔追加。配置更新和词典更新是两个独立操作，不保证两者作为一个事务同时切换。需要固定的配置与词典组合时，可预先创建并配置新实例，再由业务代码切换实例引用。

`null` 文本抛出 `NullPointerException`；空串和全空白文本返回空列表。共享自建模型时，请勿在使用期间修改模型的公开数组或分词器的受保护字段。

## 模型与源码构建

需要 Maven 3.6.3+、JDK 8+；自动下载模型另需 Python 3.8+ 和 curl。在仓库根目录执行：

```sh
python3 scripts/download_models.py
mvn -Pmodel-tests package
```

当前版本号对应的产物为 `target/thulac4j-4.0.0.jar`。源码构建前必须准备分词模型；仅运行不依赖大模型的单元测试时不需要下载。

| 用途 | 文件 | 加载方式 |
| --- | --- | --- |
| 中文分词 | `models/cws_model.bin`、`models/cws_dat.bin` | 构建时打入 jar，运行时从资源加载 |
| 词性标注 | `models/model_c_model.bin`、`models/model_c_dat.bin` | 由 `POSTagger` 构造参数指定外部文件 |
| 标签、内置词典、繁简映射 | 仓库中的其余模型与字典资源 | 构建时打入 jar |

下载脚本固定使用 THUNLP 的 thulac 0.2.2 发行包，先校验压缩包和模型文件的 SHA-256，再安装缺失文件。已有文件校验不符时会停止并保留原文件。下载约 53 MB，四个二进制模型解压后约 493 MB；这些大文件保持 Git 忽略。来源、尺寸与校验和见[模型清单](models/manifest.json)，更多模型资料见 [Wiki](https://github.com/yizhiru/thulac4j/wiki)。

已有下载包时可以离线安装：

```sh
python3 scripts/download_models.py --archive /path/to/thulac-0.2.2-py3-none-any.whl
```

源码更新模型文件后，需要重新构建 jar。使用自定义输入流或模型构造器时，资源所有权说明见[迁移指南](docs/MIGRATION-4.0.md#资源与错误处理)。

## 测试与性能

### 回归测试

```sh
mvn test                  # 小型确定性模型测试，无需大模型
mvn verify                # 默认测试与构建检查
mvn -Pmodel-tests test    # 加载真实模型，包含默认测试与已有语料断言
```

`model-tests` 模式缺模型会报错，不会静默跳过。CI 已配置 JDK 8、17、21、25 的默认测试；大模型回归需单独准备模型运行。普通构建不签名，源码包、文档包及 GPG 签名由 `release` 配置启用。

本次验证通过 **40 项测试**，117 条仓库回归文本分别进行分词、词性标注，与优化前共 **234 条输出完全一致**。这属于已有回归集覆盖，不是新语料上的准确率评测。

### 本次性能结果

Apple M2 / 16 GiB / JDK 19.0.1，固定模型和 2 GB 堆，单线程 JMH 测量：

| 117 句回归语料 | 优化前吞吐量 | 优化后吞吐量 | 每句内存分配减少 |
| --- | ---: | ---: | ---: |
| 分词 | 478,690 句/秒 | 503,486 句/秒（+5.2%） | 47.8% |
| 词性标注 | 24,051 句/秒 | 28,351 句/秒（+17.9%） | 77.1% |

分词吞吐量的误差区间重叠，不能据此确认稳定提速；本次词性标注语料测量的两版区间没有重叠。内存分配量不等于进程总内存。完整的八组场景、初始化耗时、峰值内存及测量边界见[性能报告](benchmarks/RESULTS.md)。

### 运行基准

```sh
mvn -Pbenchmarks -DskipTests package
# 无大模型时，运行合成解码基准
java -jar target/benchmarks/thulac4j-4.0.0-benchmarks.jar DecoderBenchmark -prof gc
# 已下载模型并重新构建后，运行完整分词与词性标注基准
java -jar target/benchmarks/thulac4j-4.0.0-benchmarks.jar TokenizerBenchmark -prof gc
java -jar target/benchmarks/thulac4j-4.0.0-benchmarks.jar CorpusBenchmark -prof gc
```

合成解码基准不包含真实特征查找，不能代表完整分词性能；完整基准不计模型初始化耗时。需要对比优化前后时，使用相同 JDK 顺序执行：

```sh
python3 benchmarks/compare.py --java /path/to/jdk-19/bin/java
python3 benchmarks/check_outputs.py
python3 benchmarks/measure_loading.py  # macOS：初始化耗时与进程峰值内存
python3 benchmarks/summarize.py
```

对比脚本默认基线为 `a9d5b8405e71ff900be08f36b876c225daec63c7`，使用独立构建目录，固定 2 GB 堆、G1、单线程、2 个独立 JVM，每个 JVM 预热 3 次、测量 5 次，每次 1 秒。结果写入 `target/performance/`。脚本辅助编译需 JDK 9+，本次实测使用 JDK 19.0.1；运行时避免其他高负载任务。对比脚本与 CI 从各自的 POM 读取版本号，不依赖写死的 jar 版本。

## 发布前检查

准备模型后生成主 jar、源码包和 Javadoc 包，并执行真实模型回归：

```sh
mvn -Prelease,model-tests -Dgpg.skip=true clean verify
python3 scripts/check_release.py
```

这里显式跳过 GPG，适用于本地检查，不能代替签名验证。正式签名、Central Portal 上传步骤及本次检查记录见[4.0.0 发布说明](docs/RELEASE-4.0.0.md)。

## 常见问题

| 问题 | 处理方式 |
| --- | --- |
| 出现 `Missing resource ... cws...`，或分词器初始化失败 | 检查两个分词模型是否齐全，下载后重新构建并替换 jar；查看异常原因链 |
| POS 模型报文件不存在 | 检查传入路径及应用的启动目录，或使用绝对路径 |
| 修改静态配置后，独立实例的行为没有变化 | 为目标实例设置配置；静态入口仅影响默认实例 |
| 追加词典后，旧词条仍然生效 | 需要整体替换时改用 `setUserWords` |
| 默认测试通过，但运行时找不到模型 | 默认测试使用小型模型；下载真实模型并执行 `-Pmodel-tests` |

## 许可与致谢

项目代码使用 [Apache License 2.0](LICENSE)。感谢 THUNLP 实验室提供 THULAC；模型来源另见[模型清单](models/manifest.json)。
