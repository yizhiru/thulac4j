package io.github.yizhiru.thulac4j.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
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
		try (FileInputStream input = new FileInputStream(inputPath);
		     FileChannel channel = input.getChannel()) {
			return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
		}
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
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
			List<String> list = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				list.add(line);
			}
			return list;
		}
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
		if (bytes.length % Integer.BYTES != 0) {
			throw new IOException("Truncated little-endian integer array");
		}
		IntBuffer intBuffer = ByteBuffer.wrap(bytes)
				.order(ByteOrder.LITTLE_ENDIAN)
				.asIntBuffer();
		int[] array = new int[intBuffer.remaining()];
		intBuffer.get(array);
		return array;
	}

	/** Opens a bundled resource with an actionable error instead of a null stream. */
	public static InputStream openResource(String path) throws IOException {
		InputStream input = IOUtils.class.getResourceAsStream(path);
		if (input == null) {
			throw new IOException("Missing resource " + path + "; install the model files described in README.md");
		}
		return input;
	}

	/** Reads directly into the final array using a bounded byte buffer. Does not close input. */
	public static int[] readLittleEndianInts(DataInputStream input, int count) throws IOException {
		if (count < 0 || count > Integer.MAX_VALUE - 8) {
			throw new IOException("Invalid integer array length: " + count);
		}
		byte[] buffer = new byte[Math.min(count, 4096) * Integer.BYTES];
		// Read the first block before allocating, so short/corrupt headers fail promptly.
		input.readFully(buffer);
		int[] values = new int[count];
		IntBuffer integers = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
		for (int offset = 0; offset < count; ) {
			int block = Math.min(count - offset, buffer.length / Integer.BYTES);
			if (offset != 0) input.readFully(buffer, 0, block * Integer.BYTES);
			integers.position(0);
			integers.get(values, offset, block);
			offset += block;
		}
		return values;
	}

	public static int readLittleEndianInt(DataInputStream input) throws IOException {
		return Integer.reverseBytes(input.readInt());
	}

	public static int checkedArrayLength(int rows, int columns) throws IOException {
		long length = (long) rows * columns;
		if (rows < 0 || columns < 0 || length > Integer.MAX_VALUE - 8) {
			throw new IOException("Invalid model dimensions: " + rows + " x " + columns);
		}
		return (int) length;
	}
}
