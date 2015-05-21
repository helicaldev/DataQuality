package com.helicaltech.pcni.exceptions;


public class ConfigurationException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public ConfigurationException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public ConfigurationException(String message) {
		super(message);
	}

}
