package com.helicaltech.pcni.login;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {

	private static final Logger logger = LoggerFactory
			.getLogger(LoginController.class);

	public Boolean isAlreadyAuthenticated = false;
	public List<String> user_role = new ArrayList<String>();
	public String session = null;

	public String performLogin(HttpSession session, Connection connection, String username,String password) throws SQLException {
		String message = "Failure";
		Statement stmt = null;

		String fetchUser = "select u_info.USER_ID as username,u_info.PASSWORD_ENC as password,c_perm.DESCRIPTION as 'role_name' from USER_INFO u_info inner join USER_APPS u_apps on u_apps.USER_ID = u_info.USER_KEY inner join CODE_APP_INFO c_app_info on c_app_info.CODE_KEY = u_apps.APP_ID left join USER_PERMISSION u_perm on u_perm.USER_KEY = u_info.USER_KEY left join CODE_PERMISSION c_perm on c_perm.CODE_KEY = u_perm.PERMISSION_KEY where c_app_info.CODE_KEY = 1 and u_info.USER_ID = '"
				+ username + "' and u_info.PASSWORD_ENC = '" + password + "'";
		try {
			stmt = connection.createStatement();
			ResultSet resultSet = stmt.executeQuery(fetchUser);
			while (resultSet.next()) {
				System.out.println("Setting user role.. "
						+ resultSet.getString("role_name"));
					user_role.add(resultSet.getString("role_name"));
				message = "Success";
				isAlreadyAuthenticated = true;
			}
			
			logger.debug("user_role :" + user_role);

			if (message.equals("Success")) {
				LoginForm.getInstance().setjUserName(username);
				LoginForm.getInstance().setjPassword(password);
				// mapping more than 1 role to user below
				if(user_role.isEmpty())
				{
					user_role.add("NORMAL");
				}
				LoginForm.getInstance().setRole(user_role);
				session.setAttribute("Roles", user_role.toString());
				logger.debug("LoginForm updated !!");
			}
			resultSet.close();
			stmt.close();
		} catch (Exception e) {
			logger.error("Exception Occurred", e);
			message = "";
		}
		return message;
	}
}
