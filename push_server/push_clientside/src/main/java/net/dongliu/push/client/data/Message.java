package net.dongliu.push.client.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import net.dongliu.push.client.utils.IOUtils;

/**
 * Push消息封装.
 * 
 * @author dongliu
 * 
 */
public class Message {

	public static final byte TYPE_LOGIN = 0;
	public static final byte TYPE_HEART_BEAT = 1;
	public static final byte TYPE_ECHO = 2;
	public static final byte TYPE_PUSH_MESSAGE = 3;
	public static final byte TYPE_KEY_MESSAGE = 3;

	public static final byte MARK_PLAIN = 0;
	/** DES加密 */
	public static final byte MARK_DES = 1;
	/** RSA加密 */
	public static final byte MARK_RSA = 2;

	/** 消息类型 */
	private byte type;
	/** 消息数据的一些特征;目前用于标记数据加密方式 */
	private byte mark;
	/** 数据内容 */
	private byte[] data;

	private static class MessageHoler {
		public static Message echoMessage = new Message(Message.TYPE_ECHO, "");
		public static Message heartBeatMessage = new Message(Message.TYPE_HEART_BEAT, "");
	}

	public Message(byte type, String str) {
		this(type, MARK_PLAIN, str);
	}

	public Message(byte type, byte mark, String msg) {
		this.mark = mark;
		this.type = type;
		setMessage(msg);
	}

	public Message(byte type, byte[] data) {
		this(type, MARK_PLAIN, data);
	}

	public Message(byte type, byte mark, byte[] data) {
		this.mark = mark;
		this.type = type;
		setData(data);
	}

	private Message() {
	}

	private void setMessage(String msg) {
		if (msg == null) {
			throw new NullPointerException("Payload String cannot be null.");
		}
		byte[] data;
		try {
			data = msg.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("should not came here", e);
		}
		setData(data);
	}

	private void setData(byte[] data) {
		if (data == null) {
			throw new NullPointerException("Payload data cannot be null.");
		}
		this.data = data;
	}

	public byte[] getData() {
		return this.data;
	}

	public String getMessage() {
		if (mark != 0) {
			throw new RuntimeException("");
		}
		try {
			return new String(this.data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Wrong encoding.", e);
		}
	}

	/**
	 * 组合成bytes.
	 * 
	 * @return
	 */
	public byte[] toBytes() {
		byte[] bas = new byte[data.length + 4];
		IOUtils.toBytesB((short) (data.length + 2), bas, 0);
		bas[2] = type;
		bas[3] = mark;
		System.arraycopy(data, 0, bas, 4, data.length);
		return bas;
	}

	/**
	 * 组合成bytebuffer.
	 * 
	 * @return
	 */
	public ByteBuffer toByteBuffer() {
		return ByteBuffer.wrap(toBytes());
	}

	/**
	 * 将内容写到输出流中.
	 * 
	 * @param os
	 * @throws IOException
	 */
	public void writeToStream(OutputStream os) throws IOException {
		os.write((byte) ((data.length + 2) >> 8));
		os.write((byte) (data.length + 2));
		os.write((byte) this.type);
		os.write((byte) this.mark);
		os.write(data);
	}

	/**
	 * 从bytebuffer中构造一个message.
	 * 
	 * @param buffer
	 * @param length
	 * @return
	 */
	public static Message fromByteBuffer(ByteBuffer buffer, short length) {
		if (buffer.remaining() < length) {
			throw new BufferUnderflowException();
		}
		byte type = buffer.get();
		switch (type) {
		case Message.TYPE_ECHO:
			buffer.position(buffer.position() + length - 1);
			return Message.echoMessage();
		case Message.TYPE_HEART_BEAT:
			buffer.position(buffer.position() + length - 1);
			return Message.heartBeatMessage();
		default:
			Message message = new Message();
			message.type = type;
			message.mark = buffer.get();
			message.data = new byte[length - 2];
			buffer.get(message.data, 0, length - 2);
			return message;
		}

	}

	/**
	 * 从bytes中构造一个message.
	 * 
	 * @param buffer
	 * @param length
	 * @return
	 */
	public static Message fromBytes(byte[] buffer, short length) {
		if (buffer.length < length) {
			throw new BufferUnderflowException();
		}
		Message message = new Message();
		message.type = buffer[0];
		message.mark = buffer[1];
		message.data = new byte[length - 2];
		System.arraycopy(buffer, 2, message.data, 0, length - 2);
		return message;
	}

	/**
	 * 从输入流中构造一个Message. 返回null表示读取到流末尾.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static Message fromInputStream(InputStream is) throws IOException {
		int length = IOUtils.readShort(is);
		if (length == -1) {
			return null;
		}
		if (length < 2) {
			throw new BufferUnderflowException();
		}
		length = length - 2;
		Message message = new Message();

		int type = is.read();
		if (type == -1) {
			return null;
		}
		message.type = (byte) type;

		int mark = is.read();
		if (mark == -1) {
			return null;
		}
		message.mark = (byte) mark;

		message.data = new byte[length];
		int offset = 0;
		int count;
		while ((count = is.read(message.data, offset, length - offset)) >= 0) {
			offset += count;
			if (offset == length) {
				break;
			}
		}
		if (count < 0) {
			return null;
		}
		return message;
	}

	/**
	 * 返回echo Message.
	 * 
	 * @return
	 */
	public static Message echoMessage() {
		return MessageHoler.echoMessage;
	}

	/**
	 * 返回heart beat Message.
	 * 
	 * @return
	 */
	public static Message heartBeatMessage() {
		return MessageHoler.heartBeatMessage;
	}

	public short getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public byte getMark() {
		return mark;
	}

	public void setMark(byte mark) {
		this.mark = mark;
	}

}
