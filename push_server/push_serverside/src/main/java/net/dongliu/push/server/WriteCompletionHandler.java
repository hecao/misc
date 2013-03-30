package net.dongliu.push.server;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.util.Queue;

import net.dongliu.push.data.Message;

/**
 * 写完成时间处理.
 * 写通过队列进行，如果队列不为空，写下一条消息.
 *
 * @author dongliu
 *
 */
public class WriteCompletionHandler implements CompletionHandler<Integer, Session> {
	@Override
	public void completed(Integer result, Session session) {
		Queue<Message> writeQueue = session.getWriteQueue();
		Message writeMessage;
		ByteBuffer byteBuffer;
		synchronized (writeQueue) {
			do {
				writeQueue.remove();
				writeMessage = writeQueue.peek();
				// 队列为空
				if (writeMessage == null) {
					return;
				}
				byteBuffer = writeMessage.toByteBuffer();
			} while (!byteBuffer.hasRemaining());
		}
		session.writeMessage(writeMessage);

	}

	@Override
	public void failed(Throwable exc, Session session) {
		session.onException(exc);
		session.close();
	}

}
