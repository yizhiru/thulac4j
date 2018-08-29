package io.github.yizhiru.thulac4j.util;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IOUtilsTest {

    @Test
    public void toIntArray() throws IOException {
        int[] array = IOUtils.toIntArray(
                this.getClass().getResourceAsStream(ModelPaths.T2S_PATH));
        assertEquals(5600, array.length);
        assertEquals(33836, array[0]);
        assertEquals(40800, array[2789]);
        assertEquals(40863, array[5599]);
    }
}