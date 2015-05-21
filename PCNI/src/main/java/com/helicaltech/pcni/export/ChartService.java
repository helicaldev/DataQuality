package com.helicaltech.pcni.export;

import com.helicaltech.pcni.driver.JDBCDriver;
import com.helicaltech.pcni.exceptions.ConfigurationException;
import com.helicaltech.pcni.resourceloader.DataSetResource;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utils.ApplicationContextAccessor;
import com.helicaltech.pcni.utils.ConfigurationFileReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * This class is responsible to create JSONobject to plot chart.
 *
 * @author Muqtar Ahmed
 * @since 1.0
 */
public class ChartService {

	private static final Logger logger = LoggerFactory.getLogger(ChartService.class);

	private final ApplicationProperties applicationProperties;

	/**
	 * @param data
	 *            a <code>String</code> in JSON format which contains directory
	 *            name efwvf file name Vf ID.
	 * @param applicationProperties
	 * @see EFWController
	 */
	public ChartService(String data, ApplicationProperties applicationProperties) {
		// this.data = data;
		// this.settingPath = null;
		this.applicationProperties = applicationProperties;
	}

	/**
	 * This method is responsible to read efwvf File and create JSONObject which
	 * contains chart id and Script.Before that it validate efwvf file.
	 *
	 * @return
	 * @throws SQLException
	 * @throws ApplicationException
	 */
	public String getData(String data, Connection connections) throws ConfigurationException {
		JSONObject parameterJsonObject = (JSONObject) JSONSerializer.toJSON(data);
		String mapId = parameterJsonObject.getString("map");

		try {

			JSONArray jsonArray;
			Map<String, String> confQueryMap = null;
			confQueryMap = ConfigurationFileReader.getMapFromPropertiesFile(new File(applicationProperties
					.getSolutionDirectory() + File.separator + "System" + File.separator + "DQQuery.conf"));
			logger.debug("The connection is null? " + (connections == null));

			JSONObject jsonData = null;

			logger.debug("Returning json from query configuration : " + confQueryMap);

			String query = "";
			Object query1 = confQueryMap.get("query_" + mapId);
			query = query1.toString();
			if (query.contains("${")) {
				int parameterCount = StringUtils.countMatches(query, "$");
				for (int param = 1; param <= parameterCount; param++) {
					String paramName = confQueryMap.get("param_" + mapId + "_" + param);
					String replace = "${" + paramName + "}";
					logger.debug("Parameter Name: " + paramName);
					String paramFromJson = parameterJsonObject.getString(paramName);
					if (paramFromJson.contains("[")) {
						paramFromJson = paramFromJson.replaceAll("\\[", "").replaceAll("\\]", "");
						paramFromJson = paramFromJson.replaceAll("\"", "'");
					}

					query = query.replace(replace, paramFromJson);
				}
			}
			JDBCDriver jdbcDriver = new JDBCDriver();

			jsonData = jdbcDriver.getJSONData(connections, query, applicationProperties);

			if (jsonData != null) {
				logger.debug("Returning the jsonObject from the controller.");
				jsonArray = jsonData.getJSONArray("data");

				return jsonArray + "";
			} else {
				logger.error("The jsonObject is null. Returning null!");
			}
		} catch (ConfigurationException e) {
			logger.error("ApplicationException occurred. " + e);
			e.printStackTrace();
		}
		return null;
		// }
	}

	public String getDataWC(String data) throws ConfigurationException {
		JSONObject parameterJsonObject = (JSONObject) JSONSerializer.toJSON(data);
		try {

			JSONArray jsonArray;
			String mapId = parameterJsonObject.getString("map");
			logger.debug("Getting map in chartservice: " + mapId);
			Map<String, String> confQueryMap = null;
			confQueryMap = ConfigurationFileReader.getMapFromPropertiesFile(new File(applicationProperties
					.getSolutionDirectory() + File.separator + "System" + File.separator + "DQQuery.conf"));

			JSONObject jsonData = null;

			logger.debug("Returning json from query configuration : " + confQueryMap);

			String query = "";
			Object query1 = confQueryMap.get("query_" + mapId);
			query = query1.toString();

			logger.debug("Reading query : " + confQueryMap);
			if (query.contains("${")) {
				int parameterCount = StringUtils.countMatches(query, "$");
				for (int param = 1; param <= parameterCount; param++) {
					String paramName = confQueryMap.get("param_" + mapId + "_" + param);
					String replace = "${" + paramName + "}";
					logger.debug("Parameter Name: " + paramName);
					String paramFromJson = parameterJsonObject.getString(paramName);
					if (paramFromJson.contains("[")) {
						paramFromJson = paramFromJson.replaceAll("\\[", "").replaceAll("\\]", "");
						paramFromJson = paramFromJson.replaceAll("\"", "'");
					}

					query = query.replace(replace, paramFromJson);
				}
			}

			DataSetResource dataSource = ApplicationContextAccessor.getBean(DataSetResource.class);

			jsonData = dataSource.getResultSetFromQuery(query);

			if (jsonData != null) {
				logger.debug("Returning the jsonObject from the controller.");
				jsonArray = jsonData.getJSONArray("data");

				return jsonArray + "";
			} else {
				logger.error("The jsonObject is null. Returning null!");
			}
		} catch (ConfigurationException e) {
			logger.error("ApplicationException occurred. " + e);
			e.printStackTrace();
		}
		return null;
		// }
	}

	/**
	 * This method is responsible to read efwvf File and create JSONObject which
	 * contains chart id and Script.Before that it validate efwvf file.
	 *
	 * @return
	 * @throws ApplicationException
	 */
	/*
	 * public JSONObject getData() throws ConfigurationException {
	 * 
	 * JSONObject resultData = null; JSONObject parameterJsonObject =
	 * (JSONObject) JSONSerializer.toJSON(data); String rootPath =
	 * parameterJsonObject.getString("dir"); String absolutePath =
	 * applicationProperties.getSolutionDirectory(); JSONProcessor jsonProcessor
	 * = new JSONProcessor(); String vfFile =
	 * parameterJsonObject.getString("vf_file");
	 * 
	 * int vfId = Integer.parseInt(parameterJsonObject.getString("vf_id"));
	 * 
	 * JSONObject vfJsonObject;
	 * 
	 * int dataSourceId; JSONObject chartData;
	 * 
	 * String completeVfFile = absolutePath + File.separator + rootPath +
	 * File.separator + vfFile; vfJsonObject =
	 * jsonProcessor.getJSON(completeVfFile, true);
	 * 
	 * ResourceValidator resourceValidator = new
	 * ResourceValidator(vfJsonObject); boolean exists =
	 * resourceValidator.validateVf();
	 * 
	 * if (!exists) { logger.error("Duplicate chart id in vf file"); throw new
	 * ApplicationException("Duplicate Chart ID in VF FIle"); } else { JSONArray
	 * charts = vfJsonObject.getJSONArray("Charts"); for (int chart = 0; chart <
	 * charts.size(); chart++) { int id =
	 * charts.getJSONObject(chart).getInt("@id"); if (vfId == id) { chartData =
	 * charts.getJSONObject(chart).getJSONObject("prop"); Object dataSource =
	 * chartData.get("DataSource"); if (dataSource instanceof JSONArray) {
	 * resultData = new JSONObject(); resultData.put("data",
	 * "No Data is available"); break; } else { dataSourceId =
	 * Integer.parseInt(chartData.getString("DataSource")); JSONObject
	 * mapIdParameter = parameterJsonObject.accumulate("map_id", dataSourceId);
	 * String dataMapId = mapIdParameter.toString(); DataSetResource
	 * dataSetResource = new DataSetResource(dataMapId, applicationProperties);
	 * resultData = dataSetResource.getResultSet(); } } } return resultData; } }
	 */
}