package io.github.yizhiru.thulac4j;

import io.github.yizhiru.thulac4j.model.SegItem;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import static io.github.yizhiru.thulac4j.BaseSegmenterTest.SEG_POS_FEATURES_PATH;
import static io.github.yizhiru.thulac4j.BaseSegmenterTest.SEG_POS_WEIGHTS_PATH;
import static io.github.yizhiru.thulac4j.SegOnlyTest.SENTENCES;
import static org.junit.Assert.assertEquals;

public class SegPosTest {

    @Test
    public void segment() throws IOException {
        String[] expectedResults = new String[]{
                "因/p",
                "",
                "",
                "UTF-8/x",
                "iphone5/x",
                "鲜芋仙/nz 3/m",
                "枪杆子/n 中/f 出/v 政权/n",
                "两/m 块/q 五/m 一/m 套/q ，/w 三/m 块/q 八/m 一/m 斤/q ，/w 四/m 块/q 七/m 一/m 本/q ，/w 五/m 块/q 六/m 一/m 条/q",
                "RT/x @/w laoshipukong/x :/w 27日/t ，/w",
                "AT&T/nz 是/v 一/m 件/q 不错/a 的/u 公司/n ，/w 给/p 你/r 发/v offer/x 了/u 吗/u ？/w",
                "4/m 个/q 月/n 赚/v 了/u 20％/m 多/m",
                "仅/d 1/m 只/q ，/w 为/v 0.9923/m 元/q",
                "Just/n one/nz space/x ,/w or/ns all/nz such/x spaces/x ?/w",
                "倒模/v ，/w 替身/v 算/v 什么/r ？/w 钟汉良/np 、/w ab/np 《/w 孤芳不自赏/id 》/w 抠图/n 来/v 充数/v",
                "奥迪/nz CEO/x 违规/v 遭批/v 大众/n 表示/v 不/d 会/v 解雇/v",
                "找/v 小姐/n",
                "找/v 小妹/n",
                "学生/n 妹/n",
                "职业/n 狐狸精/n",
                "男/a 公关/n",
                "上门/v",
                "抽獎/v",
                "好/a 声音/n",
                "好/a 聲音/n",
                "夢/n 之/u 声/g",
                "夢之聲/id",
                "訂票/n",
                "改簽/v",
                "熱线/n",
                "熱線/n",
                "热線/a",
                "電话/n",
                "電話/n",
                "醫院/n",
                "代刷/v",
                "撲剋牌/nz",
                "137-1234-1234/m",
                "这/r 是/v 一个/m 伸手不见五指/i 的/u 黑夜/n 。/w 我/r 叫/v 孙悟空/np ，/w 我/r 爱/v 北京/ns ，/w 我/r 爱/v Python/x 和/c C/x +/w" +
                        " +/w 。/w",
                "我/r 不/d 喜欢/v 日本/ns 和服/n 。/w",
                "雷猴/v 回归/v 人间/n 。/w",
                "工信处/n 女/a 干事/n 每月/r 经过/p 下属/v 科室/n 都/d 要/v 亲口/d 交代/v 24/m 口/q 交换机/n 等/u 技术性/n 器件/n 的/u 安装/v 工作/v",
                "我/r 需要/v 廉/g 租/v 房/n",
                "永和/nz 服装/n 饰品/n 有限公司/n",
                "我/r 爱/v 北京/ns 天安门/ns",
                "abc/n",
                "隐马尔可夫/np",
                "雷猴/v 是/v 个/q 好/a 网站/n",
                "“/w ,/w ”/w 和/c “/w SOFTware/x （/w 软件/n ）/w ”/w 两/m 部分/n 组成/v",
                "草泥马/n 和/c 欺/g 实马/n 是/v 今年/t 的/u 流行/v 词汇/n",
                "伊藤/nz 洋华堂/n 总府店/n",
                "中国/ns 科学院/n 计算/v 技术/n 研究所/n",
                "罗密欧/ns 与/c 朱丽叶/np",
                "我/r 购买/v 了/u 道具/n 和/c 服装/n",
                "PS/x :/w 我/r 觉得/v 开源/v 有/v 一个/m 好处/n ，/w 就/d 是/v 能够/v 敦促/v 自己/r 不断/d 改进/v ，/w 避免/v 敞帚自珍/id",
                "湖北省/ns 石首市/ns",
                "湖北省/ns 十堰市/ns",
                "总经理/n 完成/v 了/u 这/r 件/q 事情/n",
                "电脑/n 修好/v 了/u",
                "做好/v 了/u 这/r 件/q 事情/n 就/d 一了百了/i 了/u",
                "人们/n 审美/v 的/u 观点/n 是/v 不同/a 的/u",
                "我们/r 买/v 了/u 一个/m 美/a 的/u 空调/n",
                "线程/n 初始化/v 时/g 我们/r 要/v 注意/v",
                "一个/m 分子/n 是/v 由/p 好多/m 原子组/n 织成/v 的/u",
                "祝/v 你/r 马到功成/i",
                "他/r 掉/v 进/v 了/u 无/v 底洞/n 里/f",
                "中国/ns 的/u 首都/n 是/v 北京/ns",
                "孙君意/np",
                "外交部/ni 发言人/n 马朝旭/np",
                "领导人/n 会议/n 和/c 第四/m 届/q 东亚/ns 峰会/n",
                "在/p 过去/t 的/u 这/r 五/m 年/q",
                "还/d 需要/v 很/d 长/a 的/u 路/n 要/v 走/v",
                "60/m 周年/q 首都/n 阅兵/n",
                "你好/id 人们/n 审美/v 的/u 观点/n 是/v 不同/a 的/u",
                "买/v 水果/n 然后/c 来/v 世博园/j",
                "买/v 水果/n 然后/c 去/v 世博园/j",
                "但是/c 后来/t 我/r 才/d 知道/v 你/r 是/v 对/a 的/u",
                "存在/v 即/c 合理/a",
                "的/u 的/u 的/u 的/u 的/u 在/p 的/u 的/u 的/u 的/u 就/d 以/p 和和/nz 和/c",
                "I/v love/x 你/r ，/w 不以为耻/i ，/w 反/d 以为/v rong/x",
                "hello/x 你好/id 人们/n 审美/v 的/u 观点/n 是/v 不同/a 的/u",
                "很/d 好/a 但/c 主要/d 是/v 基于/p 网页/n 形式/n",
                "为什么/r 我/r 不/d 能/v 拥有/v 想/v 要/v 的/u 生活/v",
                "后来/t 我/r 才/d",
                "此次/r 来/v 中国/ns 是/v 为了/p",
                "使用/v 了/u 它/r 就/d 可以/v 解决/v 一些/m 问题/n",
                ",/w 使用/v 了/u 它/r 就/d 可以/v 解决/v 一些/m 问题/n",
                "其实/d 使用/v 了/u 它/r 就/d 可以/v 解决/v 一些/m 问题/n",
                "好人/n 使用/v 了/u 它/r 就/d 可以/v 解决/v 一些/m 问题/n",
                "是/v 因为/p 和/p 国家/n",
                "老年/t 搜索/v 还/d 支持/v",
                "干脆/d 就/d 把/p 那/r 部/q 蒙/v 人/n 的/u 闲法/n 给/p 废/v 了/u 拉倒/v ！/w RT/x @/w laoshipukong/x :/w 27日/t ，/w " +
                        "全国/n 人大/j 常委会/j 第三/m 次/q 审议/v 侵权/v 责任法/n 草案/n ，/w 删除/v 了/u 有关/v 医疗/n 损害/v 责任/n “/w 举证/v 倒置/v" +
                        " ”/w 的/u 规定/n 。/w 在/p 医患/n 纠纷/n 中/f 本/d 已/d 处于/v 弱势/n 地位/n 的/u 消费者/n 由此/d 将/d 陷入/v 万劫不复/i " +
                        "的/u 境地/n 。/w",
                "他/r 说/v 的/u 确实/a 在理/a",
                "长春/ns 市长/n 春节/t 讲话/n",
                "结婚/v 的/u 和/c 尚未/d 结婚/v 的/u",
                "结合/v 成分子/n 时/g",
                "旅游/v 和/c 服务/v 是/v 最/d 好/a 的/u",
                "这/r 件/q 事情/n 的确/d 是/v 我/r 的/u 错/n",
                "供/v 大家/r 参考/v 指正/v",
                "哈尔滨/ns 政府/n 公布/v 塌/v 桥/n 原因/n",
                "我/r 在/p 机场/n 入口处/n",
                "邢永臣/np 摄影/v 报道/v",
                "BP/x 神经/n 网络/n 如何/r 训练/v 才/d 能/v 在/p 分类/v 时/g 增加/v 区/n 分度/n ？/w",
                "南京市/ns 长江/ns 大桥/n",
                "应/v 一些/m 使用者/n 的/u 建议/n ，/w 也/d 为了/p 便于/v 利用/v NiuTrans/x 用于/v SMT/x 研究/v",
                "长春市/ns 长春/ns 药店/n",
                "邓颖超/np 生前/t 最/d 喜欢/v 的/u 衣服/n",
                "胡锦涛/np 是/v 热爱/v 世界/n 和平/n 的/u 政治局/n 常委/n",
                "程序员/n 祝海林/np 和/c 朱会震/np 是/v 在/p 孙健/np 的/u 左面/f 和/c 右面/f ,/w 范凯/np 在/p 最/d 右面/f ./w 再/d 往/p 左/f 是/v " +
                        "李松洪/np",
                "一次性/d 交/v 多少/r 钱/n",
                "两/m 块/q 五/m 一/m 套/q ，/w 三/m 块/q 八/m 一/m 斤/q ，/w 四/m 块/q 七/m 一/m 本/q ，/w 五/m 块/q 六/m 一/m 条/q",
                "小/a 和/c 尚/d 留/v 了/u 一个/m 像/p 大/a 和尚/n 一样/a 的/u 和尚/n 头/n",
                "我/r 是/v 中华人民共和国/ns 公民/n ;/w 我/r 爸爸/n 是/v 共和党/n 党员/n ;/w 地铁/n 和平/n 门站/n",
                "张晓梅/np 去/v 人民/n 医院/n 做/v 了/u 个/q B/x 超然/a 后/f 去/v 买/v 了/u 件/q T/m 恤/q",
                "C/x +/w +/w 和/c c/g #/w 是/v 什么/r 关系/n ？/w 11/m +/w 122/m =/w 133/m ，/w 是/v 吗/u ？/w PI/x =/w 3.14159/m",
                "你/r 认识/v 那个/r 和/c 主席/n 握手/v 的/u 的/u 哥/j 吗/u ？/w 他/r 开/v 一/m 辆/q 黑色/n 的士/n 。/w",
                "2017-10-13/m 给/p 你/r 发/v offer/x 了/u 吗/u ？/w 27日/t 发/v iphone5/x 了/u 吗/u",
        };

        SegPos segmenter = new SegPos(SEG_POS_WEIGHTS_PATH, SEG_POS_FEATURES_PATH);

        for (int i = 0; i < SENTENCES.length; i++) {
            String actual = segmenter.segment(SENTENCES[i])
                    .stream()
                    .map(SegItem::toString)
                    .collect(Collectors.joining(" "));
            assertEquals(expectedResults[i], actual);
        }

        long length = 0L;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; ++i) {
            for (String sentence : SENTENCES) {
                segmenter.segment(sentence);
                length += sentence.getBytes(StandardCharsets.UTF_8).length;
            }
        }
        long elapsed = (System.currentTimeMillis() - start);
        System.out.println(String.format("time elapsed: %d ms, rate: %f kb/s.",
                elapsed, (length * 1.0) / 1024.0f / (elapsed * 1.0 / 1000.0f)));
    }
}
