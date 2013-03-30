package net.dongliu.push.client.exception;

import java.io.IOException;

public class ServerClosedException extends IOException {
	private static final long serialVersionUID = 3400337421190211039L;
	public ServerClosedException(String msg){
		super(msg);
	}
}
