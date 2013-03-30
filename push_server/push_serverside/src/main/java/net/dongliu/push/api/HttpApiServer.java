package net.dongliu.push.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import net.dongliu.push.utils.Constants;

/**
 * 通过http接口，接收发送push消息的请求.
 * @author dongliu
 *
 */
public class HttpApiServer {
	
	private ServerSocket serverSocket;
	private ExecutorService executorService;
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	private volatile boolean stop;
	
	public void start() throws IOException{
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);
		serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress(Constants.API_PORT_DEFAULT));
		stop = false;
        while(!stop){
            Socket socket=null;
            try{
	            socket = serverSocket.accept();
	            executorService.execute(new HttpRequestHandler(socket));
            }catch (Exception e) {
            	logger.warn("Server accept error.", e);
			}
        }
	}
	
	
	
	public void stop() throws IOException{
		stop = true;
		executorService.shutdown();
		if(serverSocket != null){
			serverSocket.close();
		}
	}
	
}
