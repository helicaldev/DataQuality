package com.helicaltech.pcni.exceptions;


public class JdbcConnectionException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public JdbcConnectionException(String message, Throwable exception) {
		super(message, exception);
	}

}
