package com.helicaltech.pcni.resourceloader;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.helicaltech.pcni.datasource.ConnectionProvider;
import com.helicaltech.pcni.driver.JDBCDriver;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utils.ConfigurationFileReader;

/**
 * This class is responsible to get the datasource related details and execute
 * query and get result.
 *
 * @author Muqtar Ahmed
 * @author Avi
 * @since 1.0
 */

@Component
@Scope("prototype")
public class DataSetResource {

	private static final Logger logger = LoggerFactory.getLogger(DataSetResource.class);
	private final ApplicationProperties applicationProperties = ApplicationProperties.getInstance();

	@Autowired
	private ConnectionProvider connectionProvider;
	
	
	/**
	 * <p>
	 * This method is responsible to get the connection related details,
	 * instantiate the DataSources class and get the final result in JSONObject
	 * format. It reads the value of key efwd from setting.xml ,if same name
	 * extension is present in the directory then read that file from
	 * directory,If more than one file is present with that extension the it
	 * will throw exception. else read datamap id and corresponding connection
	 * details from efwd file. and provide connection details and query to
	 * another class(JDBCDriver) to execute query.
	 * </p>
	 *
	 * @return
	 */
	public JSONObject getResultSet(String data) {
		Map<String, String> confQueryMap=null;	
		confQueryMap=ConfigurationFileReader.getMapFromPropertiesFile(new File(applicationProperties.getSolutionDirectory()+File.separator+"System"+File.separator+"DQQuery.conf"));
		logger.debug("called from getResultSet with data "+data);
		JSONObject requestParameterJson = (JSONObject) JSONSerializer.toJSON(data);
		int count=1;
		Connection connections = connectionProvider.getConnection("jdbc/dqDatabase");
		
		logger.debug("The connection is null? " + (connections == null));

		JSONObject jsonData = null;
		
		logger.debug("Returning json from query configuration : "+confQueryMap);
		
		JSONObject parameterJsonObject = (JSONObject) JSONSerializer.toJSON(data);
		
		if (count == 1) {
			int mapId = requestParameterJson.getInt("map_id");
			
			String query=""; 
			Object query1= confQueryMap.get("query_"+mapId);
			query=query1.toString();
			if(query.contains("${"))
			{
				int parameterCount=StringUtils.countMatches(query, "$");
				for(int param=1; param<=parameterCount; param++)
				{
					String paramName=confQueryMap.get("param_"+mapId+"_"+param);
					String replace="\"${"+paramName+"}\"";
					String paramFromJson=parameterJsonObject.getString(paramName);
					if(paramFromJson.contains("["))
					{
						paramFromJson=paramFromJson.replaceAll("\\[", "").replaceAll("\\]","");
						paramFromJson=paramFromJson.replaceAll("\"", "'");
					}
					query=query.replace(replace,paramFromJson);
				}
				
			}

			JDBCDriver jdbcDriver= new JDBCDriver();
			logger.debug("Sending Query from getResultSet after getting parameterss from DQQUery.conf file : "+query);
			jsonData =jdbcDriver.getJSONData(connections, query, applicationProperties);
					
			
		}
		DbUtils.closeQuietly(connections);
		return jsonData;
	}
	
	
	/**
	 * <p>
	 * This method is responsible to get the connection related details,
	 * instantiate the DataSources class and get the final result in JSONObject
	 * format. It reads the value of key efwd from setting.xml ,if same name
	 * extension is present in the directory then read that file from
	 * directory,If more than one file is present with that extension the it
	 * will throw exception. else read datamap id and corresponding connection
	 * details from efwd file. and provide connection details and query to
	 * another class(JDBCDriver) to execute query.
	 * </p>
	 *
	 * @return
	 * @throws SQLException 
	 */
	public JSONObject getResultSetFromQuery(String dataQuery) {
		int count=1;
		JSONObject jsonData = null;
	
		try
		{
			logger.debug(Thread.currentThread()+" Reading query : "+dataQuery);
		
			if (count == 1) {
				String query=dataQuery; 
				logger.debug("The connection is null? " + (connectionProvider == null));
				Connection connections= connectionProvider.getConnection("jdbc/dqDatabase");
				
				JDBCDriver jdbcDriver= new JDBCDriver();
				logger.debug("The connection is null? " + (connections == null));
				jsonData =jdbcDriver.getJSONData(connections, query, applicationProperties);
				
				logger.debug("got jsonData " + jsonData);
			}
		
		}
		catch(Exception e)
		{
			logger.debug("SQL Exception Occured "+e);
		}
		finally
		{
		}
		
		return jsonData;
	}
}
