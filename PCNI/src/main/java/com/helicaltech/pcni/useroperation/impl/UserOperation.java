package com.helicaltech.pcni.useroperation.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.helicaltech.pcni.datasource.ConnectionProvider;
import com.helicaltech.pcni.datasource.IJdbcDao;
import com.helicaltech.pcni.utils.ApplicationContextAccessor;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

@Component
@Scope("prototype")
public class UserOperation {
	
	
	private static final Logger logger = LoggerFactory.getLogger(UserOperation.class);
	private static UserOperation userOperation = null;
	
	@Autowired
	private ConnectionProvider connectionProvider;

	
	private UserOperation()
	{
		
		logger.debug("Creating instance of UsrOperation");
		
	}
	
	public synchronized static UserOperation getInstance() {
		if (userOperation == null) {
			userOperation = new UserOperation();
		}
		return userOperation;
	}
	
	
	public String saveUser(JSONObject formData,Connection connection)
	{
		String createUserQuery="insert into h_users(userName,password,emailAddress,enabled) values('"+formData.getString("username")+"', '"+
		formData.getString("password")+"', '"+formData.getString("email")+"', '"+formData.getString("status")+"');";
		
		IJdbcDao jdbc= ApplicationContextAccessor.getBean(IJdbcDao.class);
		String result="";
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		result= jdbc.updateQuery(connection, createUserQuery);
		
		if(result.equals("success"))
		{
			String idJSON = jdbc.query(connection, "SELECT id FROM h_users WHERE username='"+formData.getString("username")+"' AND password='"+formData.getString("password")+"' AND emailAddress='"+formData.getString("email").trim()+"';");
			String lastInsertedId = null;
			if(idJSON != null)
			{
				try{
					jsonObject = (JSONObject) JSONSerializer.toJSON(idJSON);
					jsonArray = new JSONArray();
					jsonArray= jsonObject.getJSONArray("data");
					for(int idIndex=0; idIndex< jsonArray.size(); idIndex++)
					{
						lastInsertedId= jsonArray.getJSONObject(idIndex).getString("id");
						
					}
				}
				catch(JSONException e)
				{
					logger.error("JSONException occured: ",e);
				}
			}
			if(lastInsertedId != null && !lastInsertedId.equals(""))
			{
				String roleIdJSON = jdbc.query(connection, "SELECT id FROM role WHERE role_name='"+formData.getString("role")+"';");
				String roleId=null;
				if(roleIdJSON != null )
				{
					jsonObject = (JSONObject) JSONSerializer.toJSON(roleIdJSON);
					jsonArray = new JSONArray();
					jsonArray= jsonObject.getJSONArray("data");
					
					for(int idIndex=0; idIndex< jsonArray.size(); idIndex++)
					{
						roleId= jsonArray.getJSONObject(idIndex).getString("id");
						
					}
				}
				String createRoleMapping = "INSERT INTO user_role(user_id,role_id) VALUES("+lastInsertedId+", "+roleId+");";
				result= jdbc.updateQuery(connection, createRoleMapping);
			}
		}
		if(result.equals("success"))
		{
			result="Request completed Successfully.";
		//	DbUtils.closeQuietly(connection);		
		}
		return result;
	}
	
	public String editUser(StringBuilder updateQuery, Connection connection,int id, String role)
	{
		String message = "Failed!";
		if(updateQuery.length() >= 1)
		{
			
			String updataTableQuery = "UPDATE h_users SET "+updateQuery+" WHERE id="+id+";";
			
			try
			{
				IJdbcDao jdbc= ApplicationContextAccessor.getBean(IJdbcDao.class);
				message = jdbc.updateQuery(connection, updataTableQuery);
				String roleIdJSON = jdbc.query(connection, "SELECT id FROM role WHERE role_name='"+role+"';");
				String roleId=null;
				JSONObject jsonObject = new JSONObject();
				JSONArray jsonArray = new JSONArray();
				if(roleIdJSON != null )
				{
					jsonObject = (JSONObject) JSONSerializer.toJSON(roleIdJSON);
					jsonArray = new JSONArray();
					jsonArray= jsonObject.getJSONArray("data");
					
					for(int idIndex=0; idIndex< jsonArray.size(); idIndex++)
					{
						roleId= jsonArray.getJSONObject(idIndex).getString("id");
						
					}
				}
				
				updataTableQuery = "UPDATE user_role SET role_id="+roleId+" where user_id="+id;
				message = jdbc.updateQuery(connection, updataTableQuery);
			}
			catch(Exception e)
			{
				message="Error occured!!";
				logger.error("Update Query failed",e);
			}
			
		}
		if(message.equalsIgnoreCase("success"))
		{
			message="Request Completed Successfully";
		}
	
		return message;
	}
	
	public String deleteUser(Connection conection, int userId)
	{
		String message = "Error occured!";
		try
		{
			IJdbcDao jdbc= ApplicationContextAccessor.getBean(IJdbcDao.class);
			message=jdbc.deleteQuery(conection, userId);
		}
		catch(Exception e)
		{
			message="Error occured!!";
			logger.error("Update Query failed",e);
		}
		if(message.equalsIgnoreCase("Success"))
		{
			message="Request Completed Successfully";
		}
		return message;
	}
	
	public List<String> getUser(Connection connection, int id)
	{
		List<String> userList= new ArrayList<String>();
		try
		{
		
			IJdbcDao jdbc= ApplicationContextAccessor.getBean(IJdbcDao.class);
			String getUserList= jdbc.query(connection, "select username from h_users where enabled='Y'");
			JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(getUserList);
			JSONArray jsonArray = new JSONArray();
			jsonArray= jsonObject.getJSONArray("data");
			for(int idIndex=0; idIndex < jsonArray.size(); idIndex++)
			{
				String userNam = jsonArray.getJSONObject(idIndex).getString("username");
				if(userNam != null)
				{
					userList.add(jsonArray.getJSONObject(idIndex).getString("username"));
				}
				
			}
		}
		catch(JSONException e)
		{
			userList.add("Error");
			logger.error("Error occured while searching user",e);
		}
		return userList;
	}
	
	
	public Map<String,String> searchUser(Connection connection, int id)
	{
		Map<String,String> userList= new HashMap<String, String>();
		try
		{
			IJdbcDao jdbc= ApplicationContextAccessor.getBean(IJdbcDao.class);

			//	String searchUserQuery="select id,username,password,emailAddress from h_users where id="+id;
			String searchUserQuery="SELECT u.id,u.username,u.password, u.enabled, u.emailAddress, r.role_name FROM h_users u, role r, user_role ur WHERE ur.user_id=u.id"+
			" and ur.role_id=r.id and u.id="+id+";";
			String getUserList= jdbc.query(connection,searchUserQuery);
			JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(getUserList);
			JSONArray jsonArray = new JSONArray();
			jsonArray= jsonObject.getJSONArray("data");
			for(int idIndex=0; idIndex < jsonArray.size(); idIndex++)
			{
				String userNam = jsonArray.getJSONObject(idIndex).getString("username");
				if(userNam != null)
				{
					logger.debug("Fetched userName: "+jsonArray.getJSONObject(idIndex).getString("username"));
					userList.put("id",jsonArray.getJSONObject(idIndex).getString("id"));
					userList.put("username",jsonArray.getJSONObject(idIndex).getString("username"));
//					userList.put("password",jsonArray.getJSONObject(idIndex).getString("password"));
					userList.put("email",jsonArray.getJSONObject(idIndex).getString("emailAddress"));
					userList.put("role",jsonArray.getJSONObject(idIndex).getString("role_name"));
					userList.put("status",jsonArray.getJSONObject(idIndex).getString("enabled"));
				}
				
			}
		}
		catch(Exception e)
		{
			userList.put("Error","");
			logger.error("Error occured while searching user",e);
		}
		return userList;
	}

}
