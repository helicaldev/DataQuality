package com.helicaltech.pcni.security;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationImpl implements Authentication{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread() + " : "+ AuthenticationImpl.class);

	/*
	 * Maintain User Details 
	 */
	private Map<String,JSONObject> userMap= new HashMap<String,JSONObject>();
	
	//private UserDetails userDetails;
	
	@Override
	public boolean isUserLoggedIn(String userName) {
		boolean isAlreadyLoggedIn= false;
		if(userMap != null)
		{
			if(userMap.get(userName) != null)
			{
				isAlreadyLoggedIn= true;
			}
		}
		return isAlreadyLoggedIn;
	}

	@Override
	public void registerUser(UserDetails user,String userName) {
		JSONObject json= new JSONObject();
		try
		{
		
				json.accumulate("username",user.getUsername());
				json.accumulate("password",user.getPassword());
				//json.accumulate("email",user.getEmail());
				json.accumulate("session",user.getSession());
				this.userMap.put(userName,json);
				
		}
		catch(JSONException e)
		{
			logger.error("JSON Exception occured. ",e);
		}
		//userMap.put("password",password);
	}

	@Override
	public boolean destroyUser(String userName, String password) {
		boolean destroyedStatus= false;
		if(userMap != null)
		{
			if(userMap.get(userName) != null)
			{
				this.userMap.remove(userName);
			}
		}
		return destroyedStatus;
	}
	
	@Override
	public Map<String, JSONObject> getUserMap()
	{
		return this.userMap;
	}
	
}
