package net.dongliu.push.server;

/**
 * 定时检查过期任务的task.
 *
 * @author dongliu
 *
 */
public class TimeOutSessionChecker extends java.util.TimerTask {

	@Override
	public void run() {
		RoutCenter routCenter = RoutCenter.getInstance();
		routCenter.closeTimeOutSession();
	}

}
