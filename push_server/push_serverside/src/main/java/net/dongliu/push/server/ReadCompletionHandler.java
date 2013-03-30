package net.dongliu.push.server;

import java.nio.channels.CompletionHandler;

/**
 * 处理读数据事件的handler.
 *
 * @author dongliu
 *
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, Session> {

	public ReadCompletionHandler() {
	}

	public void cancelled(Session session) {
	}

	@Override
	public void completed(Integer result, Session session) {
		// 客户端关闭连接
		if (result < 0) {
			session.close();
			return;
		}
		try {
			if (result > 0) {
				session.getReadBuffer().flip();
				session.decode();
				session.getReadBuffer().compact();
			}
		} finally {
			try {
				session.pendingRead();
			} catch (Exception e) {
				session.onException(e);
				session.close();
			}
		}
	}

	@Override
	public void failed(Throwable exc, Session session) {
		session.onException(exc);
		session.close();
	}

}
