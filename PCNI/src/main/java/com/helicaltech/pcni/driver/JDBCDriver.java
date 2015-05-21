package com.helicaltech.pcni.driver;

//import com.helicaltech.pcni.resourceloader.EFWDQueryProcessor;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Queries the database and gives the response as a json data
 *
 * @author Rajasekhar
 * @since 1.0
 */
public class JDBCDriver {

	private static final Logger logger = LoggerFactory.getLogger(JDBCDriver.class);

	/**
	 * The response to be sent to the caller
	 */
	private final JSONObject response = new JSONObject();

	/**
	 * Return the json of the result of the database query
	 *
	 * @param requestParameterJson
	 *            The Http Request parameter
	 * @param connectionDetails
	 *            The connection details from the EFWD
	 * @param dataMapTagContent
	 *            The content of the data map tag from the corresponding file
	 * @param appProp
	 *            The singleton instance of ApplicationProperties
	 * @return The json of the result of the database query
	 */
	
	public JSONObject getJSONData(Connection connection, String query, ApplicationProperties appProp) {

		logger.debug(" Inside JDBC DRiver Object "+query);
		//EFWDQueryProcessor queryProcessor = new EFWDQueryProcessor();
		
			Statement statement = null;
			ResultSet resultSet = null;
			ResultSetMetaData metaData;
			JSONArray jsonArray;
//			JSONArray errorJsonArray = null;
			JSONObject jasonObject;
			try {
				statement = connection.createStatement();
				resultSet = statement.executeQuery(query);
				metaData = resultSet.getMetaData();

				jsonArray = new JSONArray();
				jasonObject = new JSONObject();
				for (int i = 1; i <= metaData.getColumnCount(); i++) {
					JSONObject object = new JSONObject();
					object.accumulate("name", metaData.getColumnLabel(i)); // md.getColumnName(i)
					object.accumulate("type", metaData.getColumnTypeName(i));
					jasonObject.put(i, object);
				}

				jsonArray.add(jasonObject);
				JSONArray array = new JSONArray();
				JSONObject object = new JSONObject();
				while (resultSet.next()) {
					for (int i = 1; i <= metaData.getColumnCount(); i++) {
						if (metaData.getColumnType(i) == Types.DATE) {
							object.put(metaData.getColumnName(i), resultSet.getObject(i).toString());
						} else if (metaData.getColumnType(i) == Types.TIMESTAMP) {
							object.put(metaData.getColumnName(i), resultSet.getObject(i).toString());
						} else if (metaData.getColumnType(i) == Types.TIME) {
							object.put(metaData.getColumnName(i), resultSet.getObject(i).toString());
						} else {
							object.put(metaData.getColumnLabel(i), resultSet.getObject(i) == null ? "Null" : resultSet.getObject(i));
						}
					}
					array.add(object);
				}

				response.accumulate("data", array);
				response.accumulate("metadata", jsonArray);
			} catch (SQLException e) {
				logger.error("SQL Exception has occurred", e);
//				errorJsonArray.add("No Data Available");
//				response.accumulate("data", errorJsonArray);
				e.printStackTrace();
			} finally {
				try {
					if (connection != null) {
						connection.close();
					}

					if (statement != null) {
						statement.close();
					}

					if (resultSet != null) {
						resultSet.close();
					}
				} catch (SQLException e) {
					logger.error("SQL Exception has occurred", e);
					e.printStackTrace();
				}
			}
	

		return response;
	}
	
	
}
