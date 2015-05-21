package com.helicaltech.pcni.controller;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.helicaltech.pcni.datasource.ConnectionProvider;
import com.helicaltech.pcni.login.LoginForm;
import com.helicaltech.pcni.useroperation.impl.UserOperation;

@Controller
public class UserOperationController {
	
	
	private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread() + " : "+ UserOperationController.class);
	
	
	
	@Autowired
	private ConnectionProvider connectionProvider;

	@SuppressWarnings("null")
	@RequestMapping(value = "/frmCreatUsers", method = { RequestMethod.POST })
	public ModelAndView executeUserOperation(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		
				String userName=request.getParameter("username");
				String password=request.getParameter("password");
				String email=request.getParameter("email");
				String role=request.getParameter("role");
				String status=request.getParameter("enable_status");
				
				String action=request.getParameter("action");
				String message="Error ocuured";
				ModelAndView serviceLoad= new ModelAndView();
				Connection connection = null;
				if(LoginForm.getInstance().getjUserName() != null && LoginForm.getInstance().getjUserName().trim().length() > 1)
				{
					serviceLoad.setViewName("pages/sections/welcome");
					try
					{
						connection= connectionProvider.getConnection("jdbc/pcni");
						if(action.equalsIgnoreCase("Add"))
						{
							if(role == null || role.length() <=1)
							{
								role="ROLE_USER";
							}
							
							if(userName != null && password != null)
							{
								JSONObject json= new JSONObject();
								json.accumulate("username",userName);
								json.accumulate("password",password);
								json.accumulate("email",email);
								json.accumulate("role",role);
								if(status != null && status.trim().length() >=0)
								{
									if(status.equalsIgnoreCase("on"))
									{
										status="Y";
									}
								}
								else
								{
									status="N";
								}
								
								json.accumulate("status",status);
								
									logger.debug("The connection is null? " + (connectionProvider == null));
									
									if(logger.isDebugEnabled()) {
										logger.debug("The connection is null? " + (connection == null));
									}
									try
									{
										message=UserOperation.getInstance().saveUser(json,connection);
										serviceLoad.addObject("message", message);
										serviceLoad.setViewName("message");
									//	return serviceLoad;
									}
									catch(Exception e)
									{
										logger.error("Exception occured, while adding user. ",e);
										serviceLoad.addObject("error", "Error Occured!!");
										serviceLoad.setViewName("message");
										//return serviceLoad;
									}
							}
						}
						else if(action.equalsIgnoreCase("Edit"))
						{
							StringBuilder updateUserQuery = new StringBuilder();
							String editUserId=request.getParameter("hiddenUsrId");
							
							if(editUserId != null && editUserId.trim().length() >0)
							{
								if(password != null && password.trim().length() >=1 )
								{
									updateUserQuery.append(" password='"+password+"',");
								}
								if(userName != null && userName.trim().length() >=1 )
								{
									updateUserQuery.append(" username='"+userName+"',");
								}
								if(email != null && email.trim().length() >=1 )
								{
									updateUserQuery.append(" emailAddress='"+email+"',");
								}
								if(role != null && role.length() >=1)
								{
									//updateUserQuery.append(" role='"+role+"',");
								}
								if(status != null && status.trim().length() >=0)
								{
									if(status.equalsIgnoreCase("on"))
									{
										status="Y";
									}
								}
								else
								{
									status="N";
								}
								updateUserQuery.append(" enabled='"+status+"'");
								try
								{
									message=UserOperation.getInstance().editUser(updateUserQuery,connection,Integer.parseInt(editUserId),role);
									serviceLoad.addObject("message", message);
									serviceLoad.setViewName("message");
								}
								catch(Exception e)
								{
									serviceLoad.addObject("error", "Error Occured!!");
									serviceLoad.setViewName("message");
									logger.error("Error while updating user",e);
								}
							}
						}
						else if(action.equalsIgnoreCase("Delete"))
						{
							String searchId=request.getParameter("id");
							if(searchId != null && searchId.trim().length() >=1)
							{
								try
								{
									message=UserOperation.getInstance().deleteUser(connection,Integer.parseInt(searchId));
									serviceLoad.addObject("message", message);
									serviceLoad.setViewName("message");
								}
								catch(Exception e)
								{
									serviceLoad.addObject("error", "Error Occured!!");
									serviceLoad.setViewName("message");
									logger.error("Error while updating user",e);
								}

							}
							else
							{
								serviceLoad.addObject("error", "User Id can not be null!!");
								serviceLoad.setViewName("message");
							}
						}
						else if(action.equalsIgnoreCase("Search"))
						{
							String searchId=request.getParameter("id");
							if(searchId != null && searchId.trim().length() >=1)
							{
								try{
									List<String> userList = UserOperation.getInstance().getUser(connection, Integer.parseInt(searchId));
									Map<String, String> searchUserValue = UserOperation.getInstance().searchUser(connection, Integer.parseInt(searchId));
									serviceLoad.addObject("searchUserValue",searchUserValue);
									serviceLoad.addObject("userList", userList);
									serviceLoad.setViewName("message");
								}catch(Exception e)
								{
										logger.error("Exception occured, while searching user. ",e);
										serviceLoad.addObject("error", "Error Occured!!");
										serviceLoad.setViewName("message");
									}
							}
							else
							{
								serviceLoad.addObject("error", "User Id can not be null!!");
								serviceLoad.setViewName("message");
							}
						}
						else
						{
							logger.error("Application Exception occured. Command not Found");
						}
					}catch(Exception e)
					{
						
					}
					finally
					{
						if(connection != null)
						{
							logger.debug("Closing SQL Connection");
							DbUtils.closeQuietly(connection);
						}
					}
						
				}
				else
				{
					String getUser= LoginForm.getInstance().getjUserName();
					if(getUser == null && getUser.trim().length() > 1)
					{
					//	request.getRequestDispatcher("/").forward(request, response);
					}
					serviceLoad.setViewName("login");
				}
			return serviceLoad;
	
		}
	
	
		
}
