package io.github.yizhiru.thulac4j.dat;

import io.github.yizhiru.thulac4j.common.ModelPaths;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class DatMakerTest {

    @Test
    public void make() throws IOException {
        String[] paths = new String[]{
                ModelPaths.NS_DICT_PATH,
                ModelPaths.IDIOM_DICT_PATH,
                ModelPaths.STOP_WORDS_DICT_PATH
        };
        for (String path : paths) {
            List<String> lexicon = Files.lines(Paths.get(path))
                    .map(String::trim)
                    .collect(Collectors.toList());
            Dat dat = DatMaker.make(path);
            for (String word : lexicon) {
                if (word.length() > 1) {
                    assertTrue(dat.isPrefixMatched(word.substring(0, word.length() - 1)));
                }
                assertTrue(dat.isWordMatched(word));
            }
        }
    }
}
