package net.dongliu.push;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import net.dongliu.push.api.HttpApiServer;
import net.dongliu.push.server.AcceptCompletionHandlerCompletionHandler;
import net.dongliu.push.server.TimeOutSessionChecker;
import net.dongliu.push.utils.Constants;

/**
 * server 启动程序
 * @author dongliu
 *
 */
public class ServerMain {
	
	/**
	 * 服务端口
	 */
	private AsynchronousServerSocketChannel server;
	private Timer timer;
	private HttpApiServer apiServer;

	public ServerMain(){
	}
	
	public void init(){
	}

	public void start() throws InterruptedException, ExecutionException, TimeoutException, IOException {
		server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(Constants.SERVER_PORT_DEFAULT));
		// 注册事件和事件完成后的处理器  
		server.accept(null, new AcceptCompletionHandlerCompletionHandler(server));
		timer = new Timer(true);
		timer.schedule(new TimeOutSessionChecker(), Constants.SESSION_TIMEOUT_CHECK_INTERVAL, 
				Constants.SESSION_TIMEOUT_CHECK_INTERVAL);
		apiServer = new HttpApiServer();
		apiServer.start();
	}

	public void stop(){
		try {
			apiServer.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		timer.cancel();
	}

	public static void main(String args[]) throws Exception {
		new ServerMain().start();
	}

}
