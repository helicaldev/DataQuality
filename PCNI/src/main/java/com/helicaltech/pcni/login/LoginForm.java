package com.helicaltech.pcni.login;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginForm {
	
	private static final Logger logger = LoggerFactory.getLogger(LoginForm.class);
	private static LoginForm loginForm = null;
	private String jUserName = "";
	private String jPassword= "";
	private List<String> role;
	private String enabled= "";
	private String session= "";
	
//	private static List<String> userDetails = null;
	private  List<String> userDetails = new ArrayList<String>();
	
	private LoginForm()
	{
		
		logger.debug("Getting User details");
//		if(jUserName != null || jPassword != null || role != null)
//		{
			logger.debug(getjUserName()+" Fetching  User details "+jUserName);
			userDetails.add(jUserName);
			userDetails.add(jPassword);
			setjUserName(jUserName);
			setjPassword(jPassword);
			// userDetails.addAll(role);
//		}
		
	}
	public synchronized static LoginForm getInstance() {
		if (loginForm == null) {
			loginForm = new LoginForm();
		}
		return loginForm;
	}
	
	public String getjUserName() {
		return jUserName;
	}
	public void setjUserName(String jUserName) {
		logger.debug("setting User details "+jUserName);
		this.jUserName = jUserName;
	}
	public String getjPassword() {
		return jPassword;
	}
	public void setjPassword(String jPassword) {
		this.jPassword = jPassword;
	}
	public List<String> getRole() {
		return role;
	}
	public void setRole(List<String> role) {
		this.role = role;
	}
	public String getEnabled() {
		return enabled;
	}
	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}
	
	public String getSession() {
		return session;
	}
	public void setSession(String session) {
		this.session = session;
	}

}
