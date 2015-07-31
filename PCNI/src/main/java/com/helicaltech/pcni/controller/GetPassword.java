package com.helicaltech.pcni.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.helicaltech.pcni.datasource.ConnectionProvider;
import com.helicaltech.pcni.scheduling.ScheduleProcessCall;
import com.mysql.jdbc.Statement;

import org.apache.log4j.Logger;

/* This class is responsible for fetching password from USER_INFO table for all the "createdBy" present in the scheduling.xml*/
public class GetPassword {
	

	String password = "";
	Statement stmt = null;
	private static final Logger logger = Logger.getLogger(ScheduleProcessCall.class);
	public String getPassword(Connection connection ,String createdBy){
		
		String created_pwd = "select PASSWORD_ENC as password from USER_INFO u_info where USER_ID = '"
				+ createdBy + "'";
		try {
				stmt = (Statement) connection.createStatement();
				ResultSet resultSet = stmt.executeQuery(created_pwd);
				while (resultSet.next()) {
					password = resultSet.getString("password");
				}
				resultSet.close();
				stmt.close();
		}catch (Exception e) {
			logger.error("Exception Occurred", e);
		}
		return password;
		
	}
}