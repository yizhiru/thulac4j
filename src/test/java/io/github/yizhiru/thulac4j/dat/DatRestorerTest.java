package io.github.yizhiru.thulac4j.dat;

import io.github.yizhiru.thulac4j.common.ModelPaths;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class DatRestorerTest {

    @Test
    public void restore() throws IOException {
        String[] binPaths = new String[]{
                "." + ModelPaths.NS_BIN_PATH,
                "." + ModelPaths.IDIOM_BIN_PATH,
                "." + ModelPaths.STOP_WORDS_BIN_PATH
        };
        String[] dictPaths = new String[]{
                ModelPaths.NS_DICT_PATH,
                ModelPaths.IDIOM_DICT_PATH,
                ModelPaths.STOP_WORDS_DICT_PATH
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
}
