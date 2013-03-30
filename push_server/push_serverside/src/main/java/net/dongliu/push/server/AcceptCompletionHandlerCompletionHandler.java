package net.dongliu.push.server;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 服务器链接处理.
 * 
 * @author dongliu
 * 
 */
public class AcceptCompletionHandlerCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
	private AsynchronousServerSocketChannel server;

	public AcceptCompletionHandlerCompletionHandler(AsynchronousServerSocketChannel server) {
		this.server = server;
	}

	public void completed(AsynchronousSocketChannel socketChannel, Object attachment) {
		try {
			/* socketChannel.setOption(StandardSocketOptions.SO_LINGER, -1); */
			Session session = new Session(socketChannel);
			session.start();
		} finally {
			server.accept(null, this);
		}

	}

	@Override
	public void failed(Throwable exc, Object attachment) {
		exc.printStackTrace();
		server.accept(null, this);
	}
}
