package io.github.yizhiru.thulac4j.util;

/**
 * 模型文件路径名
 */
public final class ModelPaths {

    /**
     * 核心字符类型词典
     */
    public static final String CORE_CHAR_PATH = "/dicts/core_char.dict";

    /**
     * 地名词典
     */
    public static final String NS_DICT_PATH = "dicts/ns.dict";

    /**
     * 成语、习语、谚语词典
     */
    public static final String IDIOM_DICT_PATH = "dicts/idiom.dict";

    /**
     * 停用词词典
     */
    public static final String STOP_WORDS_DICT_PATH = "dicts/stop_words.dict";

    public static final String NS_BIN_PATH = "/models/ns_dat.bin";
    public static final String IDIOM_BIN_PATH = "/models/idiom_dat.bin";
    public static final String STOP_WORDS_BIN_PATH = "/models/stop_dat.bin";

    /**
     * 繁体到简体字符映射
     */
    public static final String T2S_PATH = "/models/t2s.dat";

    /**
     * 分词模块权重
     */
    public static final String SEGMENTER_WEIGHT_PATH = "/models/cws_model.bin";

    /**
     * 分词模块特征
     */
    public static final String SEGMENTER_FEATURE_PATH = "/models/cws_dat.bin";

    /**
     * 分词模块label
     */
    public static final String SEGMENTER_LABEL_PATH = "/models/cws_label.txt";

    /**
     * 词性标注模块label
     */
    public static final String POS_TAGGING_LABEL_PATH = "/models/model_c_label.txt";
}
