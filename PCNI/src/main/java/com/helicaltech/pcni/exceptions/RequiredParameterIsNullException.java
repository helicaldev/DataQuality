package com.helicaltech.pcni.exceptions;

public class RequiredParameterIsNullException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public RequiredParameterIsNullException(String message) {
		super(message);
	}

}
