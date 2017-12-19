package io.github.yizhiru.thulac4j.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * IO Util, 参考 commons-io 包中类 IOUtils 实现.
 */
public final class IOUtils {

    /**
     * Represents the end-of-file (or stream).
     */
    public static final int EOF = -1;

    /**
     * The default buffer size  to use for copy.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Gets the contents of an <code>InputStream</code> as a <code>byte[]</code>.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input the <code>InputStream</code> to read from
     * @return the requested byte array
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     */
    public static byte[] toByteArray(final InputStream input) throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int n;
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            while (EOF != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            return output.toByteArray();
        }
    }

    /**
     * Gets the contents of an <code>InputStream</code> as a int array.
     * <p>
     * This method buffers the input internally, so there is no need to use a
     * <code>BufferedInputStream</code>.
     *
     * @param input the <code>InputStream</code> to read from
     * @return the requested int array
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     */
    public static int[] toIntArray(final InputStream input) throws IOException {
        byte[] bytes = IOUtils.toByteArray(input);
        IntBuffer intBuffer = ByteBuffer.wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        int[] array = new int[intBuffer.remaining()];
        intBuffer.get(array);
        return array;
    }
}
