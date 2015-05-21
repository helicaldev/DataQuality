package com.helicaltech.pcni.scheduling;


import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.rules.BusinessRulesUtils;
import com.helicaltech.pcni.utils.ConfigurationFileReader;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for doing scheduling related opration
 *
 * @author Prashansa
 * @version 1.1
 */
public class ScheduleProcessCall {
	private static final Logger logger = Logger.getLogger(ScheduleProcessCall.class);

	/**
	 * <p>
	 * scheduleOperation() is responsible for reading the scheduling.xml from
	 * given path and schedule all the job given in XML
	 * </p>
	 *
	 * @param path
	 *            a <code>String</code> specify path of scheduling.xml
	 * @param baseUrl
	 *            a <Code>String</code> specify base URL like:
	 *            http://localhost:9090/Example/test.html
	 * @param uPassword
	 *            a <code>String</code> specify password.
	 * @return
	 * @see EFWController
	 */
	public void scheduleOperation(String path, String baseUrl, String uPassword) {
		logger.debug("Inside Schedule process call");
		JSONProcessor jsonProcessor = new JSONProcessor();
		JSONObject obj = new JSONObject();
		JSONArray jsonArray1 = new JSONArray();
		obj = jsonProcessor.getJSON(path, true);
		logger.debug("jsonObject   " + obj);
		String cronexpression = "";

		XmlOperation xmlOperation = new XmlOperation();

		jsonArray1 = xmlOperation.convertXmlStringIntoJSONArray(path);
		logger.debug("IT is JSON array" + jsonArray1);
		ISchedule scheduleClass;

		/*
		 * this className is static value after proper integration this value
		 * will come from some other place
		 */
		String className = "com.helicaltech.pcni.scheduling.ScheduleJob";
		String jobName = "";
		ScheduleProcess schedulerProcess = new ScheduleProcess();
		ConvertIntoCronExpression convertIntoCronExpression = new ConvertIntoCronExpression();

		for (int count = 0; count < jsonArray1.size(); count++) {
			String id = jsonArray1.getJSONObject(count).getString("@id");
			if (id.equals("0")) {
				logger.debug("discarding 0 id");
				jsonArray1.getJSONObject(count).discard("@id");
				jsonArray1.getJSONObject(count).discard("isActive");
				logger.debug("jsonArray1===:  " + jsonArray1.getJSONObject(count).isEmpty());
			}
			if (jsonArray1.getJSONObject(count).isEmpty() == false && jsonArray1.getJSONObject(count).getString("isActive").equals("true")) {
				JSONObject newJSoJsonObject = new JSONObject();
				newJSoJsonObject = jsonArray1.getJSONObject(count);
				logger.debug("newJSoJsonObject:  " + newJSoJsonObject);
				jobName = String.valueOf(id);
				net.sf.json.JSONObject scheduleOption = jsonArray1.getJSONObject(count).getJSONObject("ScheduleOptions");
				logger.debug("scheduleOption:  " + scheduleOption.getString("Frequency"));
				logger.debug("jobName :" + jobName);
				cronexpression = convertIntoCronExpression.convertDateIntoCronExpression(scheduleOption);
				logger.debug("cronexpression: " + cronexpression);
				try {

					scheduleClass = (ISchedule) Class.forName(className).newInstance();
					newJSoJsonObject.accumulate("password", uPassword);
					schedulerProcess.scheduleJob(cronexpression, scheduleClass, jobName, "DEFAULT", path, newJSoJsonObject, baseUrl);
				} catch (InstantiationException e1) {
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * this method is responsible to schedule specific job on the basis of id.
	 *
	 * @param path
	 *            a <code>String</code> specify path of scheduling.xml
	 * @param idd
	 *            a <code>String</code> specify id.
	 * @param baseUrl
	 *            a <Code>String</code> specify base URL like:
	 *            http://localhost:9090/Example/test.html
	 * @see SaveReportController
	 */
	public String scheduleSpecificJob(String path, String idd, String baseUrl) {

		ISchedule scheduleClass;
		String cronexpression = "";
//		String className = "com.helical.efw.scheduling.ScheduleJob";
		String jobName = "";

		XmlOperation xmlOperation = new XmlOperation();
		net.sf.json.JSONObject jsonObject = new JSONObject();

		net.sf.json.JSONObject jsonObjectScheduleOption = new JSONObject();
		String password = BusinessRulesUtils.getUserDetails().get(2);
		jsonObject = xmlOperation.getParticularObject(path, idd);
		jsonObject.accumulate("password", password);
		ScheduleProcess schedulerProcess = new ScheduleProcess();
		ConvertIntoCronExpression convertIntoCronExpression = new ConvertIntoCronExpression();
		jsonObjectScheduleOption = jsonObject.getJSONObject("ScheduleOptions");
		xmlOperation.getParticularObject(path, idd);
		String jobType = "DEFAULT";
		cronexpression = convertIntoCronExpression.convertDateIntoCronExpression(jsonObjectScheduleOption);
		logger.debug("cronexpression " + cronexpression);

		jobName = jsonObject.getString("@id");
		logger.debug("jobName :" + jobName);
		JSONObject reportParameter = new JSONObject();
		reportParameter = jsonObject.getJSONObject("SchedulingJob").getJSONObject("reportParameters");
		logger.debug("reportParameter:  " + reportParameter);
		logger.debug("jsonObject before sending to schedule: " + jsonObject);
		try {

			scheduleClass = (ISchedule) Class.forName("com.helicaltech.pcni.scheduling.ScheduleJob").newInstance();
			schedulerProcess.scheduleJob(cronexpression, scheduleClass, jobName, jobType, path, jsonObject, baseUrl);
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		return "scheduled";
	}

	// public String schedulerOperationForJsonObject(
	// net.sf.json.JSONObject jsonobject, String path, String baseUrlPath) {
	// logger.debug("Inside schedulerOperationForJsonObject");
	// ISchedule scheduleClass;
	// String className = "com.helical.efw.scheduling.ScheduleJob";
	// String jobName = "";
	// String cronexpression = "";
	// ScheduleProcess schedulerProcess = new ScheduleProcess();
	// ConvertIntoCronExpression convertIntoCronExpression = new
	// ConvertIntoCronExpression();
	// /*
	// * Temperory hard coded values has passed after some time these values
	// * will be taken from frontEnd
	// */
	//
	// String jobType = "DEFAULT";
	// net.sf.json.JSONObject cornExpressionJSonObject = new
	// net.sf.json.JSONObject();
	// cornExpressionJSonObject = jsonobject.getJSONObject("ScheduleOptions");
	// cronexpression = convertIntoCronExpression
	// .convertDateIntoCronExpression(cornExpressionJSonObject);
	// logger.debug("cronexpression " + cronexpression);
	// if (jsonobject.getBoolean("isActive") == true) {
	// // String id = "1";
	// jobName = "1";
	// JSONProcessor jsonProcessor = new JSONProcessor();
	//
	// net.sf.json.JSONObject jsonObject = new JSONObject();
	// jsonObject = jsonProcessor.getJSON(path, true);
	// logger.debug("jsonObject:  " + jsonObject);
	// JSONObject jsonObjectNew = new JSONObject();
	// jsonObjectNew = jsonObject.getJSONObject("Schedules")
	// .getJSONObject("Schedule");
	// logger.debug("jobName=============" + jsonObjectNew);
	// try {
	//
	// scheduleClass = (ISchedule) Class.forName(className)
	// .newInstance();
	// schedulerProcess.scheduleJob(cronexpression, scheduleClass,
	// jobName, jobType, path, jsonObjectNew, baseUrlPath);
	// } catch (InstantiationException e1) {
	// e1.printStackTrace();
	// } catch (IllegalAccessException e1) {
	// e1.printStackTrace();
	// } catch (ClassNotFoundException e1) {
	// e1.printStackTrace();
	// }
	// }
	// return "ok";
	// }
	//
	// public void stopJobAfterSomeCount() {
	//
	// }

	/**
	 * <p>
	 * creteReportParameter() is responsible for create report parameter
	 * like:delemeter=abc&Cuntry=india on the basis of JSONObject
	 * </p>
	 *
	 * @param reportParameter
	 *            a <code>JSONObject</code> specify Report parameter
	 * @return <code>String</code> create report parameter
	 * @see ScheduleJob
	 */
	public String creteReportParameter(JSONObject reportParameter) {
		logger.debug("creteReportParameter: ");
		// JSONObject jsonObject = new JSONObject();
		XmlOperation xmlOperation = new XmlOperation();
		List<String> key = new ArrayList<String>();
		key = xmlOperation.findKey(reportParameter.discard("csvdata"));
		// key = xmlOperation.findKey(reportParameter.);
		String parameter = "";
		logger.debug("key.size():  " + key.size());
		for (int keyCount = 0; keyCount < key.size(); keyCount++) {
			logger.debug("test..");
			String keyName = key.get(keyCount);
			logger.debug("keyName:  " + keyName);
			String KeyValue = reportParameter.getString(keyName);

			logger.debug("KeyValue:  " + KeyValue);
			if (KeyValue.contains("[") || KeyValue.contains("]")) {
				String modifyKeyValue = KeyValue.substring(1, KeyValue.length() - 1).replace("\"", "");
				logger.debug("modifyKeyValue:  " + modifyKeyValue);
				String[] modifyKeyValueArray = modifyKeyValue.split(",");
				logger.debug("modifyKeyValueArray:  " + modifyKeyValueArray);
				for (int stringCount = 0; stringCount < modifyKeyValueArray.length; stringCount++) {
					String parameterValue = modifyKeyValueArray[stringCount];
					parameter = parameter + keyName + "=" + parameterValue + "&";
					logger.debug("parameterValue:  " + parameterValue);
				}
			} else {
				parameter = parameter + keyName + "=" + KeyValue + "&";
			}
		}
		logger.debug("parameter:  " + parameter);
		return parameter;
	}

	/**
	 * gettingBaseUrl() responsible to get base url from setting.xml
	 *
	 * @return base url.
	 * @see EFWController
	 */
	public String gettingBaseUrl() {
	//	ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
		//String settingPath = applicationProperties.getSettingPath();
		String settingPath = "";
		logger.debug("settingPath: " + settingPath);
		JSONObject jsonObject = new JSONObject();
		JSONProcessor jsonProcessor = new JSONProcessor();
		jsonObject = jsonProcessor.getJSON(settingPath, true);
		logger.debug("jsonObject: " + jsonObject);
		//String baseUrl = jsonObject.getJSONObject("efwProject").getString("BaseUrl");
		String baseUrl = "http://localhost:8080/PCNI/hdi.html";
		return baseUrl;
	}

	/**
	 * getSchedulePath() is responsible to read scheduleing.xml path from
	 * project.properties file
	 *
	 * @return scheduling.xml file path
	 * @see EFWController
	 */
	public String getSchedulePath() {
		@SuppressWarnings("unchecked")
		Map<String, String> getpropertiesFileValue = new HashedMap();
		ConfigurationFileReader propertiesFileReader = new ConfigurationFileReader();
		getpropertiesFileValue = propertiesFileReader.read("project.properties");
		String SchedulerPath = getpropertiesFileValue.get("schedularPath");

		return SchedulerPath;
	}
}
