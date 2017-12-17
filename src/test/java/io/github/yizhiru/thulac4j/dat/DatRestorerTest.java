package io.github.yizhiru.thulac4j.dat;

import io.github.yizhiru.thulac4j.common.ModelPath;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class DatRestorerTest {

    @Test
    public void restore() throws IOException {
        String[] binPaths = new String[]{
                "." + ModelPath.NS_BIN_PATH,
                "." + ModelPath.IDIOM_BIN_PATH,
                "." + ModelPath.STOP_WORDS_BIN_PATH
        };
        String[] dictPaths = new String[]{
                ModelPath.NS_DICT_PATH,
                ModelPath.IDIOM_DICT_PATH,
                ModelPath.STOP_WORDS_DICT_PATH
        };

        for (int i = 0; i < binPaths.length; i++) {
            Dat stop = Dat.loadDat(binPaths[i]);
            Set<String> dict = Files.lines(Paths.get(dictPaths[i]))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            List<String> words = DatRestorer.restore(stop);
            for (String word : words) {
                assertTrue(dict.contains(word));
            }
            assertTrue(dict.size() == words.size());
        }
    }

    public static void main(String[] args) throws IOException {
        Dat dat = loadDat("train/time.dat");
        System.out.println(DatRestorer.restore(dat));
    }

    private static Dat loadDat(String path) throws IOException {
        FileChannel channel = new FileInputStream(path).getChannel();
        int datSize = (int) channel.size() / 8;
        int[] arr = new int[2 * datSize];
        ByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        IntBuffer intBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        intBuffer.get(arr);
        channel.close();

        List<Dat.Entry> entries = new ArrayList<>(datSize);
        for (int i = 0; i < 2 * datSize; i += 2) {
            entries.add(new Dat.Entry(arr[i], arr[i + 1]));
        }
        return new Dat(entries);
    }
}
