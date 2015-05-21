package com.helicaltech.pcni.datasource;

import com.helicaltech.pcni.exceptions.ConfigurationException;

import com.helicaltech.pcni.exceptions.JdbcConnectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Created by author on 29-Jan-15.
 *
 * @author Rajasekhar
 */
@Component
class JndiLookUpManager {
    private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread() + " : " + JndiLookUpManager.class);

    public DataSource getJndiDataSource(String lookUpName) {
    	if(lookUpName == null) {
    		throw new IllegalArgumentException("The parameter lookUpName is null");
    	}
        Context initContext;
        try {
            initContext = new InitialContext();
            DataSource dataSource = (DataSource) initContext.lookup("java:comp/env/" + lookUpName);
            if (dataSource == null) {
                throwException(lookUpName);
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Jndi lookUp for the name " + lookUpName + " is successful and " +
                            "the " +
                            "DataSource class is " + dataSource.getClass());
                }
            }
            return dataSource;
        } catch (NamingException exception) {
            throw new JdbcConnectionException("Could not find the JNDI resource " + lookUpName,
                    exception);
        }
    }

    private void throwException(String lookUpName) {
        throw new ConfigurationException(String.format("Could not find the JNDI resource %s. " +
                "Configure the JNDI" +
                " DataSource in your application server and query with proper lookUpName. " +
                "For example java:comp/env/jdbc/TestDB" + lookUpName));
    }
}