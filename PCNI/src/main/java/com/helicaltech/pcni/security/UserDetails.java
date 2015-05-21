package com.helicaltech.pcni.security;

import java.io.Serializable;

public interface UserDetails extends Serializable{

	
	  public abstract String getPassword();

	  public abstract String getUsername();

	  public abstract boolean getIsEnabled();
	  
	  public abstract String getEmail();
	  
	  public abstract String getSession();
	  
}
