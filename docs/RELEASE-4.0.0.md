# 4.0.0 发布记录

发布日期：2026-10-15。**4.0.0 已发布至 Maven Central，服务端状态为 `PUBLISHED`。**

- Maven 坐标：`io.github.yizhiru:thulac4j:4.0.0`。
- [Central 版本页面](https://central.sonatype.com/artifact/io.github.yizhiru/thulac4j/4.0.0) / [公共仓库目录](https://repo.maven.apache.org/maven2/io/github/yizhiru/thulac4j/4.0.0/)。
- 部署 ID：`6a6dd314-e123-49f4-a275-24a15198b2c0`，见[服务端发布回执](releases/4.0.0/central-deployment.json)。
- 2026-10-15 10:18（Asia/Shanghai）读取公共仓库 POM，以及主包、源码包、Javadoc 包的 SHA-256 文件，全部返回 HTTP 200，校验值与最终本地产物一致，见[公共仓库验证记录](releases/4.0.0/public-repository.json)。三个远程 jar 通过仓库提供的 SHA-256 文件核对，未重新下载完整 jar。

Git 发布使用 `v4.0.0` 标签；GitHub Release 说明取自[本次更新日志](../CHANGELOG.md)。Maven Central 的发布回执与 GitHub Release 分别记录。

## 检查结果

| 项目 | 结果 | 说明 |
| --- | --- | --- |
| 发布构建 | 通过 | Maven 3.9.14 / Homebrew JDK 25.0.2；前期干净构建及最终 `release,model-tests` 签名构建均通过 |
| JDK 25 真实模型与单元测试 | 40/40 通过 | 0 失败、0 错误、0 跳过 |
| JDK 19.0.1 真实模型与单元测试 | 40/40 通过 | 在第二个本地 JDK 上复核 |
| 最终主 jar | 通过 | 26 个类均为 Java 8 字节码；内嵌 POM 版本为 4.0.0 |
| 模型与依赖隔离 | 通过 | 分词模型/标签校验和一致，内置词典齐全；无外置 POS 权重/特征、JMH 或 JUnit 依赖混入 |
| 源码包与 Javadoc 包 | 通过 | 均包含新增 TokenizerConfig；源码包未重复打入模型 |
| README 示例 | 4/4 通过 | 只依赖最终主 jar 编译并运行，已核对文档中的固定输出 |
| 最终 jar 的语料回归 | 234/234 一致 | 分词、词性标注各 117 条，与保存的优化版输出比较 |
| 基准包及对比脚本 | 通过 | 从 POM 读取版本；`--build-only` 成功构建基线与当前版两个 4.0.0 基准包 |
| JMH 功能检查 | 通过 | 解码 2 组、完整分词/标注 6 组、语料 2 组；仅检查可运行性 |
| GPG 签名 | 通过 | 主 jar、源码包、Javadoc 包和 POM 的四份签名均验证成功 |
| Central 发布 | 已完成 | 发布插件 0.12.0 上传成功，独立查询确认 `PUBLISHED`，公共仓库校验值一致 |
| JDK 8/17/21 运行测试 | 本机未执行 | 本机使用 JDK 19 和 25；仓库 CI 配置覆盖 8/17/21/25，不代表远程流水线已经运行 |

JMH 功能检查使用 1 个独立 JVM、0 次预热、1 次 100 ms 测量，不能作为新的性能结论。正式性能对比保留在[原报告](../benchmarks/RESULTS.md)，没有改写历史数据或基线版本。

构建日志、功能检查日志和校验和保存在 [releases/4.0.0](releases/4.0.0/)。[最终产物与发布汇总](releases/4.0.0/artifacts.json)记录 `signatures_verified: true` 与 `remote_upload_performed: true`；本地检查脚本本身不上传。前期未签名产物信息单独保留在 `artifacts-unsigned.json`，对应旧的 `release-build.log`；最终签名构建与产物检查日志为 `signed-build.log`、`signed-check.log`。

## 已发布产物

以下是本次构建的 SHA-256；再次构建时因归档时间戳变化，校验和可能不同，以新生成的检查结果为准。

| 文件（位于 target/） | 字节数 | SHA-256 |
| --- | ---: | --- |
| `thulac4j-4.0.0.jar` | 30,002,247 | `9b54edebf79ad2d240d9c2c5e3bc361cf3659c147bdbd300893afaa42fd7c34f` |
| `thulac4j-4.0.0-sources.jar` | 32,375 | `ba51ca9e2f951aad39499d71916618d19c17ce1076de4578bf391b1ee21e961f` |
| `thulac4j-4.0.0-javadoc.jar` | 4,313,419 | `df91a14b4e7b8fe242d4e6d977efbbf790c34aacc7f2ae6f35841546d112879e` |

POM 的 SHA-256：`adb79edde6fa91e736a723b970201cb1401f0f40202a81fe6dd59b8003de5fae`。

## 重复执行发布前检查

准备 JDK 9+（检查脚本编译示例使用 `--release`；建议与本次一致使用 JDK 19 或 25）、Maven 3.6.3+、Python 3.8+ 和 curl，在仓库根目录顺序执行：

```sh
python3 scripts/download_models.py
mvn -B -ntp -Prelease,model-tests -Dgpg.skip=true clean verify
python3 scripts/check_release.py
python3 benchmarks/compare.py --build-only --output target/release-benchmark-check
```

`check_release.py` 只检查本地产物，不上传文件；可用 `--java /path/to/jdk/bin/java` 指定运行环境。输出位于 `target/release-checks/`。若产物或示例不一致，脚本以非零状态退出，不保留旧的成功汇总。

CI 提供手动运行的 `release-check` 任务（`workflow_dispatch`），下载模型、生成未签名发布包，并执行同一个检查脚本；常规 push/PR 仍运行不依赖大模型的测试与合成基准功能检查。

## 签名与发布环境

本机已安装 GnuPG 2.5.24 与 pinentry-mac，并生成带密码保护的 RSA 3072 签名密钥：

- 公开身份：`Zheng Jiangyu <j.y.zheng@qq.com>`。
- 完整指纹：`9FEC6C1950155646FF09615ED74CC1FE5271BB25`。
- 到期日：2028-10-14。
- 公钥已上传 `hkps://keyserver.ubuntu.com`，并独立下载核对指纹；仓库保存[公钥](releases/4.0.0/signing-public.asc)和[元数据](releases/4.0.0/signing.json)。

私钥与撤销证书保存在本机 `~/.gnupg`，应单独安全备份；项目仓库仅含公钥。Central 令牌保存在 `~/.m2/settings.xml`，权限为 `0600`，未写入项目或发布日志。

### 后续版本的发布操作

**4.0.0 已发布，不能覆盖或重复发布。以下上传命令仅用于完成版本号更新和验证后的后续版本。**

原 OSSRH 服务已经停用，POM 已删除旧地址，使用独立的 `central` 配置接入 Central Portal，参见 [Sonatype 的迁移公告](https://central.sonatype.org/news/20250326_ossrh_sunset/)和[官方 Maven 发布说明](https://central.sonatype.org/publish/publish-portal-maven/)。`release` 配置负责源码、Javadoc 与 GPG 签名，`central` 配置负责上传，默认 `autoPublish=false`。

发布环境需具备：可用的 GPG 及发布者签名密钥（公钥按 Central 要求可获取）、有 `io.github.yizhiru` 命名空间权限的 Central Portal 账号和用户令牌。令牌放在本地 Maven settings.xml 的 `central` server 下，不写入项目文件或日志。实际上传应使用支持发布插件的 JDK；本次插件配置已在 JDK 25 上验证。

本机录入令牌时，可在项目目录运行：

```sh
python3 scripts/configure_maven_central.py
python3 scripts/configure_maven_central.py --check
```

该工具使用不回显的交互输入，将 token username 和 token password 保存到用户级 `~/.m2/settings.xml` 的 `central` server，文件权限为 `0600`。它保留其他配置，已有 `central` 条目默认不覆盖；主动轮换令牌时使用 `--replace`。`--check` 仅报告字段是否存在，不验证账号权限，也不显示令牌。令牌可从 [Central User Token 页面](https://central.sonatype.com/usertoken)生成，操作说明见[官方文档](https://central.sonatype.org/publish/generate-portal-token/)。

新建签名密钥时需要发布者的公开姓名和邮箱；私钥密码通过本机 GPG 密码窗口输入。公钥发布到 Central 支持的公钥服务器，私钥保留在本机，不放入项目仓库。

配置完成后，先执行签名构建和签名检查：

```sh
mvn -ntp -Prelease,model-tests clean verify
python3 scripts/check_release.py --signed
```

本机使用图形密码窗口签名时，不加 Maven 的 `-B` 参数；批处理会禁止 GPG 弹窗。CI 的无人值守签名需另行配置安全的密码来源。多个签名密钥共存时，通过 `-Dgpg.keyname=完整公钥指纹` 明确指定发布密钥。

`--signed` 会验证主 jar、源码包、Javadoc 包和 `target/thulac4j-4.0.0.pom` 的四份签名，并核对这份 POM 副本与项目文件一致。签名检查通过后，上传到 Central Portal 的命令为：

```sh
mvn -ntp -Prelease,model-tests,central deploy
```

已确定直接正式发布时，可通过显式参数让 Central 校验后自动发布，并等待发布状态：

```sh
mvn -ntp -Prelease,model-tests,central -Dcentral.autoPublish=true -Dcentral.waitUntil=PUBLISHED deploy
```

不要在正式发布命令上添加 `-Dgpg.skip=true`。默认上传命令停在验证阶段，需在 Portal 正式发布；显式开启 `central.autoPublish=true` 时由服务端自动发布。

## 本次上传与状态确认

首次认证失败的原因是令牌密码带有复制自页面的 XML 包装；本地已修正，配置脚本也支持只包含一个字段的 XML 包装输入。之后上传成功，服务端启动自动发布。

Maven 在上传后的状态轮询阶段遇到 `SSLHandshakeException / PKIX path building failed` 并以非零状态退出，因此不能把该命令描述为完整成功。随后使用系统信任链、保持 TLS 证书验证的独立查询，确认同一个部署已进入 `PUBLISHED`；公共仓库 POM 与三个 jar 的 SHA-256 校验值也全部匹配。没有关闭证书验证、修改信任库或重复上传。后续 JDK 19 和 25 的独立 TLS 连通检查均通过。

需要重新查询本次发布时，使用只读状态脚本：

```sh
python3 scripts/central_status.py 6a6dd314-e123-49f4-a275-24a15198b2c0
```

脚本从本地 Maven 配置读取令牌，只请求固定的 Central HTTPS 状态接口，拒绝重定向，不上传文件；输出部署 ID、状态及脱敏错误信息。
