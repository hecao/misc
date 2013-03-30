package net.dongliu.push.client.utils;

public class Constants {

	/** 客户端连接服务器超时时间 */
	public static final int CLIENT_CONNECT_TIMEOUT = 60 * 1000;
	/** 客户端重连间隔 */
	public static final long CLIENT_RECONNECT_TIME = 5 * 60 * 1000;
	/** 客户端心跳包发送时间间隔 */
	public static final long CLIENT_HEARTBEAT_TIME = 5 * 60 * 1000;

	/** session超时时间(毫秒) */
	public static final int SESSION_TIMEOUT = 10 * 60 * 1000;
	/** 服务器监听的端口 */
	public final static int SERVER_PORT_DEFAULT = 9888;
}
