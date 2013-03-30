package net.dongliu.push.utils;

public class Constants {
	/** 服务器监听的端口 */
	public final static int SERVER_PORT_DEFAULT = 9888;

	/** Session中读buffer的初始大小 */
	public static final int SESSION_READ_BUFFER = 1024;
	/** bytebuffer扩展每次容量增加的系数 */
	public static final int EXPAND_FACTOR = 2;

	/** 服务器session超时检查间隔(毫秒) */
	public static final int SESSION_TIMEOUT_CHECK_INTERVAL = 60 * 1000;
	/** session超时时间(毫秒) */
	public static final int SESSION_TIMEOUT = 10 * 60 * 1000;

	/** 接收http发送消息的端口 */
	public static final int API_PORT_DEFAULT = 9889;
	/** 一次push请求最多制定的用户id数量 */
	public static final int MAX_USER_NUM = 1000;

	/** 客户端连接服务器超时时间 */
	public static final int CLIENT_CONNECT_TIMEOUT = 60 * 1000;
	/** 客户端重连间隔 */
	public static final long CLIENT_RECONNECT_TIME = 5 * 60 * 1000;
	/** 客户端心跳包发送时间间隔 */
	public static final long CLIENT_HEARTBEAT_TIME = 5 * 60 * 1000;

}
