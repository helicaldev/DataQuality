package com.helicaltech.pcni.security;

import javax.servlet.http.HttpSession;

public class User implements UserDetails{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String username;
	private String password;
	private String email;
	private String session;
	private boolean isEnabled;
	
	public User(String userName, String password, String email, boolean isEnabled,String sessionId)
	{
		this.username=userName;
		this.password=password;
		this.email= email;
		this.isEnabled=isEnabled;
		this.session=sessionId;
	}
	
	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean getIsEnabled() {
		return this.isEnabled;
	}

	@Override
	public String getEmail() {
		return this.email;
	}

	@Override
	public String getSession() {
		return this.session;
	}
	
	

}
