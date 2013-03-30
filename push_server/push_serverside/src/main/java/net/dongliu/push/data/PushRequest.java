package net.dongliu.push.data;

import java.util.List;

/**
 * 表示向pushserver发送的请求.
 * 
 * @author dongliu
 * 
 */
public class PushRequest {

	public static final int TYPE_QUERY = 2;
	public static final int TYPE_PUSH = 0;
	public static final int TYPE_BROADCAST = 1;

	/** 2:查询指定用户是否在线 0:发送给指定的用户 1:发送给所有用户 */
	private int type;
	/** push消息内容 */
	private String content;
	/** 用户id列表 */
	private List<String> userids;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<String> getUserids() {
		return userids;
	}

	public void setUserids(List<String> userids) {
		this.userids = userids;
	}
}
