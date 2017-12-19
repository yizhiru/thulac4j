package io.github.yizhiru.thulac4j.model;

import io.github.yizhiru.thulac4j.common.ModelPaths;
import io.github.yizhiru.thulac4j.dat.Dat;
import io.github.yizhiru.thulac4j.process.DATCementer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * THULAC 训练模型
 */
public final class ThulacModel {

    /**
     * label数量.
     */
    public final int labelSize;

    /**
     * 特征数量.
     */
    public final int featureSize;

    /**
     * label转移到label的权重.
     */
    public final int[] llWeights;

    /**
     * 特征对应某label的权重.
     */
    public final int[] flWeights;

    /**
     * Feature DAT size.
     */
    public final int featureDatSize;

    /**
     * 特征DAT, 对应于 cws_dat.bin  model_c_model.bin
     */
    public final int[] featureDat;

    /**
     * Label, 对应于cws_label.txt 或者 model_c_label.txt
     */
    public final String[] labelValues;

    /**
     * 训练模型文件中POC对应的标识.
     */
    public static final class PocMark {
        /**
         * 对应于 POC B 的char.
         */
        public static final Character POS_B_CHAR = '0';

        /**
         * 对应于 POC M 的char.
         */
        public static final Character POS_M_CHAR = '1';

        /**
         * 对应于 POC E 的char.
         */
        public static final Character POS_E_CHAR = '2';

        /**
         * 对应于 POC B 的char.
         */
        public static final Character POS_S_CHAR = '3';
    }

    /**
     * 加载THULAC训练模型.
     *
     * @param modelPath   label转移权重、特征label权重文件路径, cws_model.bin 或者 model_c_model.bin
     * @param featurePath 特征DAT文件路径, cws_dat.bin 或者 model_c_dat.bin
     */
    public ThulacModel(String modelPath, String featurePath) throws IOException {
        // Load weights model
        FileChannel channel = new FileInputStream(modelPath).getChannel();
        ByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        IntBuffer intBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        labelSize = intBuffer.get();
        featureSize = intBuffer.get();
        llWeights = new int[labelSize * labelSize];
        flWeights = new int[featureSize * labelSize];
        intBuffer.get(llWeights);
        intBuffer.get(flWeights);
        channel.close();

        String labelPath;
        if (labelSize == 4) {
            labelPath = ModelPaths.SEG_ONLY_LABEL_PATH;
        } else {
            labelPath = ModelPaths.SEG_POS_LABEL_PATH;
        }

        // Load feature DAT
        channel = new FileInputStream(featurePath).getChannel();
        // int类型占4个字节，有base与check值，所以除以8.
        featureDatSize = (int) channel.size() / 8;
        featureDat = new int[featureDatSize * 2];
        byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        intBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        intBuffer.get(featureDat);
        channel.close();

        // Load label file
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream(labelPath))
        );
        String line;
        List<String> labelList = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        labelValues = new String[labelList.size()];
        labelList.toArray(labelValues);
    }

    /**
     * 转换成CwsModel对象.
     */
    public CwsModel convert2CwsModel() throws IOException {
        int[][] llWeights = new int[labelSize][];
        for (int i = 0; i < labelSize; i++) {
            llWeights[i] = new int[labelSize];
        }
        for (int i = 0; i < this.llWeights.length; i++) {
            llWeights[i / labelSize][i % labelSize] = this.llWeights[i];
        }
        int[][] flWeights = new int[featureSize][];
        for (int i = 0; i < featureSize; i++) {
            flWeights[i] = new int[labelSize];
        }
        for (int i = 0; i < this.flWeights.length; i++) {
            flWeights[i / labelSize][i % labelSize] = this.flWeights[i];
        }

        // convert feature DAT
        List<Dat.Entry> entries = new ArrayList<>(featureDatSize);
        for (int i = 0; i < 2 * featureDatSize; i += 2) {
            entries.add(new Dat.Entry(featureDat[i], featureDat[i + 1]));
        }
        Dat featureDat = new Dat(entries);

        // 记录label 集合，能与allowTabular 映射起来
        int[][] posTags = getPosTags();

        // allowTabular 表示enum POC 对应的所有允许label，比如：
        // PUNCTUATION_POC 对应的允许label为 3 或 3w，
        // BEGIN_POC 对应的允许label为 0 或 0打头的label
        int[][] allowTabular = new int[12][];
        for (int i = 0; i < labelValues.length; i++) {
            // punctuation
            if ("3".equals(labelValues[i]) || "3w".equals(labelValues[i])) {
                allowTabular[0] = new int[]{i};
            }
            // single of numeral
            if ("3".equals(labelValues[i]) || "3m".equals(labelValues[i])) {
                allowTabular[4] = new int[]{i};
            }
            // begin of numeral
            else if ("0".equals(labelValues[i]) || "0m".equals(labelValues[i])) {
                allowTabular[1] = new int[]{i};
            }
            // middle of numeral
            else if ("1".equals(labelValues[i]) || "1m".equals(labelValues[i])) {
                allowTabular[2] = new int[]{i};
            }
            // end of numeral
            else if ("2".equals(labelValues[i]) || "2m".equals(labelValues[i])) {
                allowTabular[3] = new int[]{i};
            }
        }
        int[] indices = {1, 2, 4, 8, 9, 12, 15};
        for (int i = 0; i < indices.length; i++) {
            allowTabular[i + 5] = posTags[indices[i]];
        }

        return new CwsModel(
                labelSize,
                featureSize,
                llWeights,
                flWeights,
                featureDat,
                labelValues,
                allowTabular,
                new DATCementer(
                        this.getClass().getResourceAsStream(ModelPaths.NS_BIN_PATH),
                        "ns"),
                new DATCementer(
                        this.getClass().getResourceAsStream(ModelPaths.IDIOM_BIN_PATH),
                        "i")
        );
    }

    /**
     * 计算所有可能label 索引值集合，以二维数组表示
     *
     * @return 索引值二维数组
     */
    private int[][] getPosTags() {
        List<List<Integer>> posTagsList = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            posTagsList.add(new ArrayList<>());
        }
        for (int i = 0; i < labelValues.length; i++) {
            int segIndex = labelValues[i].charAt(0) - '0';
            for (int j = 0; j < 16; j++) {
                if (((1 << segIndex) & j) != 0) {
                    posTagsList.get(j).add(i);
                }
            }
        }
        // 将posTagsList 转成二维数组
        int[][] posTags = new int[posTagsList.size()][];
        for (int i = 0; i < posTagsList.size(); i++) {
            List<Integer> list = posTagsList.get(i);
            posTags[i] = new int[list.size()];
            for (int j = 0; j < list.size(); j++) {
                posTags[i][j] = list.get(j);
            }
        }
        return posTags;
    }
}
