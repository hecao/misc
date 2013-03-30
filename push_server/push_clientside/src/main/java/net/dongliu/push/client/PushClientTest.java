package net.dongliu.push.client;

import java.io.IOException;
import java.net.InetAddress;

import net.dongliu.push.client.data.Message;
import net.dongliu.push.client.exception.AuthFailedException;
import net.dongliu.push.client.utils.Constants;

/**
 * A push client demo.
 * 
 * @author dongliu
 * 
 */
public class PushClientTest {

	public static void main(String[] args) throws IOException {
		PushConnection client = new PushConnection(InetAddress.getLocalHost(), Constants.SERVER_PORT_DEFAULT);
		final Thread currentThread = Thread.currentThread();

		client.connect();
		try {
			client.login("test", "test");
		} catch (AuthFailedException e) {
			client.close();
			return;
		}

		client.regesterMessageHandler(new MessageHandler() {

			public void onMessageReceived(Message message) {
				System.out.println("Receive push: " + message.getMessage());
			}

			public void onServerClosed() {
				System.out.println("Server closed.");
				currentThread.interrupt();
			}

			public void onException(IOException e) {
				System.out.println("Client read/write exception.");
				currentThread.interrupt();
			}
		});

		while (!currentThread.isInterrupted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
		client.close();
	}
}
