package net.dongliu.push.client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.TimerTask;

import net.dongliu.push.client.data.Message;
import net.dongliu.push.client.utils.Constants;

/**
 * 发送心跳包的线程.
 * 
 * @author dongliu
 * 
 */
public class HeartBeatSender extends TimerTask {

	private PushConnection client;

	public HeartBeatSender(PushConnection client) {
		this.client = client;
	}

	@Override
	public void run() {
		// 检测上次接收到响应包的时间
		long last = this.client.getLastAck();
		if (System.currentTimeMillis() - last > Constants.SESSION_TIMEOUT) {
			// 超时.
			// 断开连接
			try {
				this.client.close();
			} catch (IOException e) {
			}
			// 重连..
			this.client.reConnect();
		}
		try {
			OutputStream os = this.client.getSocket().getOutputStream();
			Message message = new Message(Message.TYPE_HEART_BEAT, "heart beat.");
			message.writeToStream(os);
			os.flush();
		} catch (IOException e) {
			// TODO: do something.
		}
	}

}
