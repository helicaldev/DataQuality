package com.helicaltech.pcni.export;

import com.helicaltech.pcni.exceptions.ConfigurationException;
//import com.helicaltech.pcni.resourceloader.ChartService;
import com.helicaltech.pcni.singleton.ApplicationProperties;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * this class is responsible for converting chart data in to json array and
 * iterate the JSONArray and return the string for download CSV file of chart
 * data
 *
 * @author Muqtar Ahmed
 * @version 1.1
 * @since 1.0
 */

public class CSVUtility {

	private static final Logger logger = LoggerFactory.getLogger(CSVUtility.class);

	/**
	 * The singleton instance which holds the application settings. Only one
	 * instance per application
	 */
	private final ApplicationProperties applicationProperties = ApplicationProperties.getInstance();

	/**
	 * this method is used to conver chart data to jsonarray, iterate the
	 * jsonarray and return the data
	 *
	 * @param data
	 *            chart data
	 * @return String
	 * @throws SQLException 
	 */
	public String getCSVData(String data, Connection connection) throws SQLException {
		try {

			ChartService chartService = new ChartService(data, this.applicationProperties);
			//jsonObject = chartService.getData(data,map);
			//String jsonString = jsonObject.get("data").toString();
			String jsonString = chartService.getData(data,connection);
			
			//String jsonString = chartService.getDataWC(data);
			JSONObject jsonObj = new JSONObject();
			JSONArray dataNJsonArray = JSONArray.fromObject(jsonString);

			for (Object object : dataNJsonArray) {
				jsonObj = (JSONObject) JSONSerializer.toJSON(object);
			}

			Iterator<?> keys = jsonObj.keys();
			List<String> listOfKeys = new ArrayList<String>();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				logger.debug("Key == " + key);
				listOfKeys.add(key);
			}

			logger.debug("Keys == " + listOfKeys);
			String header = "";
			String stringOfHeader = "";
			for (int i = 0; i < listOfKeys.size(); i++) {
				String hValus = listOfKeys.get(i);
				stringOfHeader = '"' + hValus + '"';
				header = header + stringOfHeader + ",";
			}

			String result = "";
			result = header.substring(0, header.length() - 1).trim() + "\n";
			for (int j = 0; j < dataNJsonArray.size(); j++) {
				String stringOfValues = "";
				logger.debug("Size of Json Array" + dataNJsonArray.size());
				for (int k = 0; k < listOfKeys.size(); k++) {
					String value = dataNJsonArray.getJSONObject(j).getString(listOfKeys.get(k));
					String valuesNquote = '"' + value + '"';
					stringOfValues = stringOfValues + valuesNquote + ",";
					logger.debug("Values == " + stringOfValues);
				}
				result = result + stringOfValues.substring(0, stringOfValues.length() - 1).trim() + "\n";
			}
			return result;
		} catch (JSONException e) {
			logger.error("JSONException ", e);
			e.printStackTrace();
		} catch (ConfigurationException e) {
			logger.error("ApplicationException occurred. " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	public String getCSVData(String data) throws SQLException {
		
		try {

			ChartService chartService = new ChartService(data, this.applicationProperties);
			//jsonObject = chartService.getData(data,map);
			//String jsonString = jsonObject.get("data").toString();
			//String jsonString = chartService.getData(data,map,connection);
			
//			String jsonString = chartService.getDataWC(data,map);
			String jsonString = chartService.getDataWC(data);
			JSONObject jsonObj = new JSONObject();
			JSONArray dataNJsonArray = JSONArray.fromObject(jsonString);

			for (Object object : dataNJsonArray) {
				jsonObj = (JSONObject) JSONSerializer.toJSON(object);
			}
			if(jsonObj != null){
			Iterator<?> keys = jsonObj.keys();
			List<String> listOfKeys = new ArrayList<String>();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				logger.debug("Key == " + key);
				listOfKeys.add(key);
			}

			logger.debug("Keys == " + listOfKeys);
			String header = "";
			String stringOfHeader = "";
			for (int i = 0; i < listOfKeys.size(); i++) {
				String hValus = listOfKeys.get(i);
				stringOfHeader = '"' + hValus + '"';
				header = header + stringOfHeader + ",";
			}

			String result = "";
			result = header.substring(0, header.length() - 1).trim() + "\n";
			for (int j = 0; j < dataNJsonArray.size(); j++) {
				String stringOfValues = "";
				logger.debug("Size of Json Array" + dataNJsonArray.size());
				for (int k = 0; k < listOfKeys.size(); k++) {
					String value = dataNJsonArray.getJSONObject(j).getString(listOfKeys.get(k));
					String valuesNquote = '"' + value + '"';
					stringOfValues = stringOfValues + valuesNquote + ",";
					logger.debug("Values == " + stringOfValues);
				}
				result = result + stringOfValues.substring(0, stringOfValues.length() - 1).trim() + "\n";
			}
			
			return result;
			}
		} catch (JSONException e) {
			logger.error("JSONException ", e);
			e.printStackTrace();
		} catch (ConfigurationException e) {
			logger.error("ApplicationException occurred. " + e);
			e.printStackTrace();
		}
		return null;
	}
}
