package net.dongliu.push.api;

import java.util.ArrayList;
import java.util.List;

import net.dongliu.push.data.Message;
import net.dongliu.push.data.PushRequest;
import net.dongliu.push.data.PushResponse;
import net.dongliu.push.server.RoutCenter;
import net.dongliu.push.server.Session;
import net.dongliu.push.utils.Constants;

/**
 * 处理通过API发送的push相关的请求.
 * 
 * @author dongliu
 * 
 */
public class PushRequestHandler {

	/**
	 * 查询用户.
	 * 
	 * @param pushRequest
	 * @return
	 */
	public PushResponse onQuery(PushRequest pushRequest) {
		List<String> userids = pushRequest.getUserids();
		PushResponse response = new PushResponse();
		if (userids == null || userids.isEmpty()) {
			response.setCode(PushResponse.CODE_USER_NON);
			return response;
		}

		int count = 0;
		RoutCenter routCenter = RoutCenter.getInstance();
		List<String> successList = new ArrayList<String>();
		for (String userid : userids) {
			Session session = routCenter.getSession(userid);
			if (session != null) {
				successList.add(userid);
			}
			if (count++ > Constants.MAX_USER_NUM) {
				break;
			}
		}
		response.setCode(PushResponse.CODE_SUCCESS);
		response.setUseridList(successList);
		return response;
	}

	/**
	 * TODO:广播.
	 * 
	 * @param pushRequest
	 * @return
	 */
	public PushResponse onBroadCast(PushRequest pushRequest) {
		return null;
	}

	/**
	 * 发送push给指定用户.
	 * 
	 * @param pushRequest
	 * @return
	 */
	public PushResponse onPush(PushRequest pushRequest) {
		List<String> userids = pushRequest.getUserids();
		PushResponse response = new PushResponse();
		if (userids == null || userids.isEmpty()) {
			response.setCode(PushResponse.CODE_USER_NON);
			return response;
		}
		String content = pushRequest.getContent();
		if (content == null) {
			response.setCode(PushResponse.CODE_CONTENT_NON);
			return response;
		}
		int count = 0;
		RoutCenter routCenter = RoutCenter.getInstance();
		Message message = new Message(Message.TYPE_PUSH_MESSAGE, content);
		List<String> successList = new ArrayList<String>();
		for (String userid : userids) {
			Session session = routCenter.getSession(userid);
			if (session != null) {
				session.pendingWrite(message);
				successList.add(userid);
			}
			if (count++ > Constants.MAX_USER_NUM) {
				break;
			}
		}
		response.setCode(PushResponse.CODE_SUCCESS);
		response.setUseridList(successList);
		return response;
	}

}
