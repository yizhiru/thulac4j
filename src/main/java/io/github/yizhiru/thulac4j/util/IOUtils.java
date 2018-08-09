package io.github.yizhiru.thulac4j.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * IO Utils, 参考 commons-io 包中类 IOUtils 实现.
 */
public final class IOUtils {

    /**
     * Represents the end-of-file (or stream).
     */
    private static final int EOF = -1;

    /**
     * The default buffer size  to use for copy.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Maps a region of this channel's file directly into memory.
     *
     * @param inputPath the file path to read from, not null.
     * @return The mapped byte buffer
     * @throws IOException If some other I/O error occurs
     */
    public static MappedByteBuffer mapToByteBuffer(final String inputPath) throws IOException {
        FileChannel channel = new FileInputStream(inputPath).getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
    }

    /**
     * Gets the contents of an <code>InputStream</code> as a list of Strings,
     * one entry per line.
     *
     * @param input the <code>InputStream</code> to read from, not null
     * @return the list of Strings, never null
     * @throws NullPointerException if the input is null
     * @throws IOException          if an I/O error occurs
     */
    public static List<String> readLines(final InputStream input) throws IOException {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(input));
        final List<String> list = new ArrayList<>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        reader.close();
        return list;
    }

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
        byte[] bytes = toByteArray(input);
        IntBuffer intBuffer = ByteBuffer.wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asIntBuffer();
        int[] array = new int[intBuffer.remaining()];
        intBuffer.get(array);
        return array;
    }
}
