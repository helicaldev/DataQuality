package com.helicaltech.pcni.exceptions;

public class RuntimeIOException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	public RuntimeIOException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
