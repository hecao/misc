package net.dongliu.push.server;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import net.dongliu.push.utils.Constants;

/**
 * 用户注册与消息路由...
 * 
 * @author dongliu
 * 
 */
public class RoutCenter {

	private ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<String, Session>();

	private static final RoutCenter instance = new RoutCenter();

	public static RoutCenter getInstance() {
		return instance;
	}

	/**
	 * 注册一个用户session.
	 * @param session
	 */
	public void registerSession(Session session) {
		if (session == null) {
			return;
		}
		String uid = session.getUid();
		if (uid == null || uid.isEmpty()) {
			// do something
			return;
		} else {
			// TODO: 如果有相同的uid，不通的session,则close上一个session
			Session oldSession = sessionMap.get(uid);
			if (oldSession != null) {
				oldSession.close();
			}
			sessionMap.put(uid, session);
		}
	}

	/**
	 * 注销一个用户session.
	 * @param session
	 */
	public void unRegisterSession(Session session) {
		if (session == null) {
			return;
		}
		String uid = session.getUid();
		if (uid != null) {
			Session mSession = sessionMap.get(uid);
			if (mSession != null && mSession.equals(session)) {
				sessionMap.remove(uid);
			}
		}
	}

	/**
	 * 按用户id找到所属的session.
	 * @param uid
	 * @return
	 */
	public Session getSession(String uid) {
		return sessionMap.get(uid);
	}

	/**
	 * 清楚过期的session.
	 */
	public void closeTimeOutSession() {
		// TODO: we need a better way.
		Iterator<Session> it = sessionMap.values().iterator();
		while (it.hasNext()) {
			Session session = it.next();
			if (System.currentTimeMillis() - session.getTimeStamp() > Constants.SESSION_TIMEOUT) {
				session.close();
			}
		}
	}
}
