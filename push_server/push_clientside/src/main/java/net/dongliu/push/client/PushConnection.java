package net.dongliu.push.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.Random;
import java.util.Timer;

import javax.crypto.SecretKey;

import net.dongliu.push.client.data.Message;
import net.dongliu.push.client.exception.AuthFailedException;
import net.dongliu.push.client.exception.CryptionFailedException;
import net.dongliu.push.client.exception.ServerClosedException;
import net.dongliu.push.client.utils.Constants;
import net.dongliu.push.client.utils.EncryptUtils;

/**
 * 监听服务器端的push消息。
 * 
 * @author dongliu
 * 
 */
public class PushConnection {

	private InetAddress host;
	private int port;
	private MessageHandler messageHandler;

	private long lastAck;
	private Socket socket;
	private Timer timer;
	private Message loginMessage;

	public PushConnection(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * 建立到服务器的链接.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws AuthFailedException
	 */
	public void connect() throws UnknownHostException, IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(this.host, this.port), Constants.CLIENT_CONNECT_TIMEOUT);
	}

	/**
	 * 向服务器发送加密密钥.
	 * 
	 * @param rsaPubKey
	 * @throws IOException
	 * @throws ServerClosedException
	 * @throws CryptionFailedException
	 */
	public void startEncryptiion(String rsaPubKey) throws IOException, CryptionFailedException {
		PublicKey pubKey = EncryptUtils.getRSAPublicKey(rsaPubKey);
		String seed = String.valueOf(System.currentTimeMillis()) + new Random().nextInt(1000);
		// 生成des密钥
		SecretKey key = EncryptUtils.generatorDeskey(seed);
		byte[] data = key.getEncoded();
		// 使用RSA加密des密钥
		data = EncryptUtils.encryptRSA(data, pubKey);
		Message keyMessage = new Message(Message.TYPE_KEY_MESSAGE, Message.MARK_RSA, data);

		OutputStream os = socket.getOutputStream();
		keyMessage.writeToStream(os);
		os.flush();

		// 接收服务器响应
		Message respMessage = readMessage();
		if (respMessage == null) {
			throw new ServerClosedException("Server close during send pub key.");
		}
		String msg = respMessage.getMessage();
		if (!msg.equals("0")) {
			throw new CryptionFailedException(msg);
		}

		// 服务器接收密钥.
	}

	/**
	 * 超时后重新连接. 现在只有心跳超时，进行重连.
	 */
	protected void reConnect() {
		while (true) {
			try {
				Thread.sleep(Constants.CLIENT_RECONNECT_TIME);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			try {
				connect();
				login();

			} catch (UnknownHostException e) {
			} catch (IOException e) {
				// TODO:重连异常
			} catch (AuthFailedException e) {
				// 验证失败
				break;
			}
		}
	}

	/**
	 * 发送登录消息.
	 * 
	 * @return
	 * @throws IOException
	 * @throws AuthFailedException
	 */
	public void login(String usrid, String passwd) throws IOException, AuthFailedException {
		loginMessage = new Message(Message.TYPE_LOGIN, usrid);
		login();
	}

	private void login() throws IOException, AuthFailedException {
		if (loginMessage == null) {
			throw new NullPointerException("No login message.");
		}
		OutputStream os = socket.getOutputStream();
		loginMessage.writeToStream(os);
		os.flush();

		Message rm = readMessage();
		if (rm == null) {
			// 验证失败时，服务器直接关闭连接。
			throw new AuthFailedException("Login failed.");
		}

		// 启动接收线程
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					PushConnection.this.receiveMessage();
				} catch (IOException e) {
					messageHandler.onException(e);
					try {
						close();
					} catch (IOException e1) {
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();

		// 心跳检测线程
		timer = new Timer(true);
		timer.schedule(new HeartBeatSender(this), Constants.CLIENT_HEARTBEAT_TIME, Constants.CLIENT_HEARTBEAT_TIME);
	}

	public void regesterMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	private void receiveMessage() throws IOException {
		Message message;
		while ((message = readMessage()) != null) {
			switch (message.getType()) {
			case Message.TYPE_PUSH_MESSAGE:
				handelMessage(message);
				break;
			case Message.TYPE_ECHO:
				onEchoMessage(message);
				break;
			default:
			}
		}
		// 服务器端关闭连接.
		onServerClose();
	}

	private void onServerClose() {
		try {
			close();
		} catch (IOException e) {
		}

		messageHandler.onServerClosed();
	}

	/**
	 * 读取一条消息.
	 * 
	 * @return
	 * @throws IOException
	 */
	private Message readMessage() {
		try {
			InputStream is = socket.getInputStream();
			return Message.fromInputStream(is);
		} catch (SocketException e) {
			// server close may result in reset exception.
			messageHandler.onException(e);
			return null;
		} catch (IOException e) {
			messageHandler.onException(e);
			return null;
		}
	}

	/**
	 * 收到服务器的心跳响应包.
	 * 
	 * @param message
	 */
	private void onEchoMessage(Message message) {
		lastAck = System.currentTimeMillis();
	}

	/**
	 * 读取或者写入发生异常.
	 * 
	 * @param e
	 */
	protected void onException(Exception e) {
		e.printStackTrace();
	}

	/**
	 * 处理服务器发送的push消息.
	 * 
	 * @param message
	 */
	private void handelMessage(Message message) {
		if (messageHandler != null) {
			try {
				messageHandler.onMessageReceived(message);
			} catch (Exception ignore) {
				// ignore exception.
			}
		}
	}

	/**
	 * 关闭与服务器连接.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (socket != null) {
			socket.close();
		}
	}

	protected Socket getSocket() {
		return this.socket;
	}

	protected long getLastAck() {
		return this.lastAck;
	}

}
