package com.helicaltech.pcni.login;

import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {

	
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	public Boolean isAlreadyAuthenticated = false;
	public String user_role = null;
	public String session = null;

	public String performLogin(Connection connection, String username, String password) throws SQLException {
		String message = "Failure";
		Statement stmt = null;
		
		String fetchUser = "SELECT user.username, r.role_name FROM h_users user, role r, user_role urole WHERE user.username='"
				+ username + "' AND user.password='" + password + "' AND urole.user_id=user.id AND r.id=urole.role_id";
		try
		{
			stmt = connection.createStatement();
			ResultSet resultSet = stmt.executeQuery(fetchUser);
			while (resultSet.next()) {
				System.out.println("Setting user role");
				user_role = resultSet.getString("role_name");
				LoginForm.getInstance().setRole(user_role);
				message = "Success";
				isAlreadyAuthenticated=true;
			}
		
		if(message.equals("Success"))
		{
			//LoginForm.getInstance().setjPassword(password);
			
			LoginForm.getInstance().setjUserName(username);
			LoginForm.getInstance().setjPassword(password);
			
		}
		resultSet.close();
		stmt.close();
		}
		catch(Exception e)
		{
			logger.error("Exception Occurred",e);
			message="";
		}
		return message;
	}
}
