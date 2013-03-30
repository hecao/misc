package net.dongliu.push.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * IO工具类
 *
 * @author dongliu
 *
 */
public class IOUtils {

	/**
	 * 增加bytebuffer的空间
	 * 
	 * @param readBuffer
	 * @return
	 */
	public static ByteBuffer increaseBufferCapatity(ByteBuffer readBuffer) {
		if (readBuffer == null) {
			return null;
		}
		ByteBuffer newBuffer = ByteBuffer.allocateDirect(readBuffer.capacity() * Constants.EXPAND_FACTOR);
		newBuffer.put(readBuffer);
		return newBuffer;
	}

	/**
	 * bytebuffer中的可读内容转为String. 默认UTF-16 Big Endian
	 * 
	 * @param buffer
	 * @return
	 */
	public static String toString(ByteBuffer buffer, int length) {
		if (buffer == null) {
			return null;
		} else if (!buffer.hasRemaining()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		while (buffer.remaining() >= 2 && sb.length() < length / 2) {
			sb.append(buffer.getChar());
		}
		return sb.toString();
	}

	public static String toStringUTF8(ByteBuffer buffer, int length) {
		try {
			return new String(toByteArray(buffer, length), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Should never reach here.", e);
		}
	}

	public static byte[] toByteArray(ByteBuffer buffer, int length) {
		if (buffer.remaining() < length) {
			throw new BufferUnderflowException();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while (buffer.hasRemaining() && length-- > 0) {
			baos.write(buffer.get());
		}
		byte[] ba = baos.toByteArray();
		return ba;
	}

	/**
	 * 4byte组合成一个int.
	 * 
	 * @param b3
	 * @param b2
	 * @param b1
	 * @param b0
	 * @return
	 */
	public static int makeInt(byte b3, byte b2, byte b1, byte b0) {
		return (((b3) << 24) | ((b2 & 0xff) << 16) | ((b1 & 0xff) << 8) | ((b0 & 0xff)));
	}

	/**
	 * 2byte组合成一个short.
	 * 
	 * @param b1
	 * @param b0
	 * @return
	 */
	public static short makeShort(byte b1, byte b0) {
		return (short) (((b1 & 0xff) << 8) | ((b0 & 0xff)));
	}

	/**
	 * big endian bytes array to int.
	 * 
	 * @param ba
	 * @return
	 */
	public static int makeIntB(byte[] ba) {
		if (ba == null || ba.length < 4) {
			throw new IllegalArgumentException("Need at lease four bytes.");
		}
		return makeInt(ba[0], ba[1], ba[2], ba[3]);
	}

	/**
	 * big endian bytes array to int.
	 * 
	 * @param ba
	 * @return
	 */
	public static short makeShortB(byte[] ba) {
		if (ba == null || ba.length < 2) {
			throw new IllegalArgumentException("Need at lease four bytes.");
		}
		return makeShort(ba[0], ba[1]);
	}

	/**
	 * read short from big endian stream.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static int readShort(InputStream is) throws IOException {
		int b0 = is.read();
		if (b0 == -1) {
			return -1;
		}
		int b1 = is.read();
		if (b1 == -1) {
			return -1;
		}
		return makeShort((byte) b0, (byte) b1);
	}

	/**
	 * int to bit endian bytes.
	 * 
	 * @param value
	 * @param array
	 */
	public static void toBytesB(int value, byte[] ba, int offset) {
		ba[offset + 0] = (byte) (value >> 24);
		ba[offset + 1] = (byte) (value >> 16);
		ba[offset + 2] = (byte) (value >> 8);
		ba[offset + 3] = (byte) (value);
	}

	/**
	 * short int to bit endian bytes.
	 * 
	 * @param value
	 * @param array
	 */
	public static void toBytesB(short value, byte[] ba, int offset) {
		ba[offset + 0] = (byte) (value >> 8);
		ba[offset + 1] = (byte) (value);
	}

	/**
	 * 从Stream读出byte数组并关闭.
	 * 
	 * @param in
	 * @return
	 */
	public static byte[] toByteArray(InputStream in) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[256];
		int length;
		try {
			while ((length = in.read(buffer)) >= 0) {
				os.write(buffer, 0, length);
			}
			return os.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
	}
	
	public void closeQuietly (Closeable closeable){
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException ignore) {}
	}
}
