package net.dongliu.push.client.exception;

public class CryptionFailedException extends Exception {

	private static final long serialVersionUID = -709428826297976463L;
	
	public CryptionFailedException(String msg){
		super(msg);
	}

}
