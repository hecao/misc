package net.dongliu.push.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.LinkedList;
import java.util.Queue;

import net.dongliu.push.data.Message;
import net.dongliu.push.utils.Constants;
import net.dongliu.push.utils.IOUtils;

import org.apache.log4j.Logger;

/**
 * 一个用户会话。
 * 
 * @author dongliu
 * 
 */
public class Session {

	private String uid;
	private long timeStamp;
	private AsynchronousSocketChannel asynchronousSocketChannel;
	private ByteBuffer readBuffer;
	private boolean closed;
	private Queue<Message> writeQueue;
	private short readMessageLenth = -1;

	private ReadCompletionHandler readCompletionHandler;
	private WriteCompletionHandler writeCompletionHandler;

	private static Logger logger = Logger.getLogger(Session.class);

	public Session(AsynchronousSocketChannel asynchronousSocketChannel) {
		this.asynchronousSocketChannel = asynchronousSocketChannel;
		this.readBuffer = ByteBuffer.allocateDirect(Constants.SESSION_READ_BUFFER);
		this.writeQueue = new LinkedList<Message>();

		this.readCompletionHandler = new ReadCompletionHandler();
		this.writeCompletionHandler = new WriteCompletionHandler();
	}

	protected void start() {
		pendingRead();
	}

	protected final void pendingRead() {
		if (!isClosed() && this.asynchronousSocketChannel.isOpen()) {
			if (!this.readBuffer.hasRemaining()) {
				this.readBuffer = IOUtils.increaseBufferCapatity(this.readBuffer);
			}
			this.asynchronousSocketChannel.read(this.readBuffer, this, this.readCompletionHandler);

		} else {
			close();
			throw new IllegalStateException("Session Or Channel has been closed");
		}
	}

	/**
	 * 一次read请求成功完成后，执行
	 */
	public void decode() {
		// do something.
		while (readBuffer.hasRemaining()) {
			if (readMessageLenth == -1) {
				if (readBuffer.remaining() >= 2) {
					readMessageLenth = readBuffer.getShort();
				} else {
					break;
				}
			} else if (readBuffer.remaining() >= readMessageLenth) {
				timeStamp = System.currentTimeMillis();
				Message message = Message.fromByteBuffer(readBuffer, readMessageLenth);
				switch (message.getType()) {
				case Message.TYPE_LOGIN:
					doLogin(message);
					break;
				case Message.TYPE_HEART_BEAT:
					doHeartBeat(message);
					break;
				default:
				}
				readMessageLenth = -1;
			} else {
				// 未读完一条消息
				break;
			}
		}

	}

	private void doLogin(Message message) {
		if (uid == null) {
			uid = message.getMessage();
			RoutCenter routCenter = RoutCenter.getInstance();
			routCenter.registerSession(this);
			logger.info("new user login, uid:" + uid);
			pendingWrite(new Message(Message.TYPE_ECHO, "Login success."));
		}
	}

	private void doHeartBeat(Message message) {
		logger.debug(message.getMessage());
		if (uid == null) {
			close();
		} else {
			pendingWrite(Message.echoMessage());
		}
	}

	/**
	 * 发送一条消息.
	 * 
	 * @param message
	 */
	public void pendingWrite(Message message) {
		boolean directRwrite = false;
		synchronized (this.writeQueue) {
			directRwrite = this.writeQueue.isEmpty();
			this.writeQueue.offer(message);
		}
		// 如果队列为空，需要直接调用一次
		// 其他情况下在completionHandler里调用
		if (directRwrite) {
			writeMessage(message);
		}
		if (message.getType() == Message.TYPE_PUSH_MESSAGE) {
			logger.debug("Send push, uid:" + this.uid);
		}
	}

	/**
	 * 仅供write方法实现使用，在其他地方不能直接调用
	 * 
	 * @param writeMessage
	 */
	protected void writeMessage(Message writeMessage) {

		if (!isClosed() && this.asynchronousSocketChannel.isOpen()) {
			this.asynchronousSocketChannel.write(writeMessage.toByteBuffer(), this, this.writeCompletionHandler);
		} else {
			// session 已经close了
			logger.warn("Session Or Channel has been closed");
		}
	}

	/**
	 * 关闭session.
	 */
	public synchronized void close() {
		if (this.closed) {
			return;
		}
		try {
			this.asynchronousSocketChannel.shutdownOutput();
		} catch (IOException e) {
			logger.warn("Shut down socket output error.", e);
		}
		try {
			this.asynchronousSocketChannel.close();
		} catch (IOException e) {
			logger.warn("Close socket error.", e);
		}
		RoutCenter routCenter = RoutCenter.getInstance();
		routCenter.unRegisterSession(this);
		this.closed = true;
	}

	public void onException(Throwable e) {
		if (e instanceof AsynchronousCloseException) {
			// 服务器关闭了连接
			logger.debug("Session socket channel closed. uid:" + uid);
		} else {
			logger.warn("Read from/Write to client error." + e.getMessage());
		}
	}

	public boolean isClosed() {
		return closed;
	}

	public String getUid() {
		return uid;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public ByteBuffer getReadBuffer() {
		return this.readBuffer;
	}

	public Queue<Message> getWriteQueue() {
		return this.writeQueue;
	}

}
