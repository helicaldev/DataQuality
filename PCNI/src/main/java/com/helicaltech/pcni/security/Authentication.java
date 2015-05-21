package com.helicaltech.pcni.security;

import java.io.Serializable;
import java.util.Map;

import net.sf.json.JSONObject;

public interface Authentication extends Serializable {

	public boolean isUserLoggedIn(String userName);
	
	//public void registerUser(String userName,String password);
	
	public boolean destroyUser(String userName, String password);

	public void registerUser(UserDetails user, String userName);
	
	public Map<String, JSONObject> getUserMap();
	
	
}
