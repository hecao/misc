package net.dongliu.push.data;

import java.util.List;

public class PushResponse {
	public static int CODE_SUCCESS = 0;
	public static int CODE_CONTENT_NON = 1;
	public static int CODE_USER_NON = 2;
	public static int CODE_UNKNOW_TYPE = 3;
	public static int CODE_REQUEST_EMPTY = 4;
	/** 0:成功 >0:失败 */
	private int code;
	private List<String> useridList;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public List<String> getUseridList() {
		return useridList;
	}

	public void setUseridList(List<String> useridList) {
		this.useridList = useridList;
	}
}
