package net.dongliu.push.client;

import java.io.IOException;

import net.dongliu.push.client.data.Message;

/**
 * 消息处理接口.
 * @author dongliu
 * 
 */
public interface MessageHandler {
	/**
	 * 处理接收到的服务器端消息.
	 * 
	 * @param message
	 */
	public void onMessageReceived(Message message);

	/**
	 * 服务器关闭连接时，调用.
	 * 
	 * @param message
	 */
	public void onServerClosed();

	/**
	 * 读取/发送数据发生异常
	 * 
	 * @param e
	 */
	public void onException(IOException e);
}
