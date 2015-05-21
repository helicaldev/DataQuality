package com.helicaltech.pcni.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.helicaltech.pcni.exceptions.JdbcConnectionException;

/**
 * @author Rajasekhar
 */
@Component
public class ConnectionProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread() + " : " + ConnectionProvider.class);
	
	@Autowired
	private JndiLookUpManager jndiLookUpManager;
	
	public Connection getConnection(String jndiLookUpName) {
		DataSource dataSource = jndiLookUpManager.getJndiDataSource(jndiLookUpName);
		Connection connection;
		long currentTime = System.currentTimeMillis();
		try {
			connection = dataSource.getConnection();
			long now = System.currentTimeMillis();
			if(logger.isDebugEnabled()) {
				logger.debug(String.format("A connection is obtained in %s milli seconds.", (now - currentTime)));
			}
		} catch (SQLException e) {
			throw new JdbcConnectionException("", e);
		}
		return connection;
	}
}
