package com.helicaltech.pcni.scheduling;

import com.helicaltech.pcni.datasource.ConnectionProvider;
import com.helicaltech.pcni.export.EmailUtility;
import com.helicaltech.pcni.export.ReportsUtility;
import com.helicaltech.pcni.export.SendMail;
import com.helicaltech.pcni.utils.ConfigurationFileReader;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Map;

/**
 * <p>
 * This class is responsible to execute the scheduled job.
 * </p>
 *
 * @author Prashansa
 * @version 1.1
 * @see ScheduleProcessCall
 */

public class ScheduleJob implements Job, ISchedule {

	private static final Logger logger = Logger.getLogger(ScheduleJob.class);
	int executeCount = 0;

	
	@Autowired
	ConnectionProvider connectionProvider;
	
	/**
	 * <p>
	 * This method is responsible to execute job.it is an overridden method from
	 * Job interface
	 * </p>
	 */
	@Override
	public void execute(JobExecutionContext context) {

		logger.debug("Inside Execute Method ");
		logger.debug("trigger:" + context.getFireTime());
		logger.debug("instance: " + context.getJobInstance());
		logger.debug("trigger: " + context.getTrigger());
		logger.debug("JobDetail: " + context.getJobDetail());

		JSONObject newData = new JSONObject();

		JobDataMap dataMap = context.getJobDetail().getJobDataMap();
		JSONObject parametersJSON = new JSONObject();

		int jobid = dataMap.getInt("jobinput");
		String path = dataMap.getString("path");

		ConfigurationFileReader propertiesFileReader = new ConfigurationFileReader();

		// Read the properties file in the EFW/System/Mail directory
		Map<String, String> propertiesMap = propertiesFileReader.read("Mail", "mailConfiguration.properties");

		logger.debug("properties file map " + propertiesMap);

		Assert.notNull(propertiesMap, "The mailConfiguration.properties map is null!!");

		String hostName = propertiesMap.get("hostName");
		String port = propertiesMap.get("port");
		String from = propertiesMap.get("from");
		String isAuthenticated = propertiesMap.get("isAuthenticated");
		String isSSLEnabled = propertiesMap.get("isSSLEnabled");
		String user = propertiesMap.get("user");
		String password = propertiesMap.get("password");

		net.sf.json.JSONObject json = (net.sf.json.JSONObject) dataMap.get("jsonobject");
		logger.debug("path:  " + path + "JobId:  " + jobid + "  json : " + json);
		String reportEfwFile = json.getJSONObject("SchedulingJob").getString("reportFile");
		String reportDirectory = json.getJSONObject("SchedulingJob").getString("reportDirectory");
		logger.debug("reportEfwFile:  " + reportEfwFile);
		logger.debug("reportDirectory: " + reportDirectory);
		parametersJSON = json.getJSONObject("SchedulingJob").getJSONObject("reportParameters");
		String reportCsvParameter = null;
		/*
		 * check reportParameter contains csvdata or not
		 */
		JSONObject jsonObjectCsvData = new JSONObject();
		XmlOperation xmlOperation = new XmlOperation();
		jsonObjectCsvData = xmlOperation.getParticularObject(path, String.valueOf(jobid));
		if (jsonObjectCsvData.getJSONObject("SchedulingJob").getJSONObject("reportParameters").containsKey("csvdata")) {
			reportCsvParameter = jsonObjectCsvData.getJSONObject("SchedulingJob").getJSONObject("reportParameters").getString("csvdata");
			logger.debug("reportCsvParameter:: " + reportCsvParameter);
		}

		String totalFormats = json.getJSONObject("SchedulingJob").getJSONObject("EmailSettings").getString("Formats");

		String Recipients = json.getJSONObject("SchedulingJob").getJSONObject("EmailSettings").getString("Recipients");

		String[] totalRecipients = Recipients.substring(1, Recipients.length() - 1).replace("\"", "").split(",");

		String Subject = json.getJSONObject("SchedulingJob").getJSONObject("EmailSettings").getString("Subject");
		String Body = json.getJSONObject("SchedulingJob").getJSONObject("EmailSettings").getString("Body");
		logger.debug("totalFormats:  " + totalFormats);
		String csvData = reportCsvParameter;
		logger.debug("Recipients:  " + totalRecipients);
		String baseUrl = dataMap.getString("baseUrl");
		logger.debug("baseUrl:  " + baseUrl);
		ScheduleProcessCall scheduleProcessCall = new ScheduleProcessCall();
		String parameters = scheduleProcessCall.creteReportParameter(parametersJSON);
		String username = json.getJSONObject("Security").getString("CreatedBy");
		logger.debug("username: " + username);
		String appPassword = json.getString("password");
		logger.debug("appPassword:  " + appPassword);
		parameters = parameters.substring(0, parameters.length() - 1);
		logger.debug("parameters:  " + parameters);

		String data = baseUrl + "?j_username=" + username + "&j_password=" + appPassword + "&dir=" + reportDirectory + "&file=" + reportEfwFile + "&"
				+ parameters;
		
		logger.debug("Inside Schedule Job: with url "+data+"  nad CSVDATA: "+csvData);
		
		logger.debug("data1:  " + data);
		String dataAfterEncode = "";
		try {
			dataAfterEncode = URLEncoder.encode(data, "UTF-8");
			logger.debug("Data After Encode:  " + dataAfterEncode);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String reportName = ReportsUtility.getReportName(json.getString("JobName".trim()));
		String reportSourceType = "url";
		String formats = json.getJSONObject("SchedulingJob").getJSONObject("EmailSettings").getString("Formats");
		String[] totalformats = formats.substring(1, formats.length() - 1).replace("\"", "").split(",");
		logger.debug("formats:  " + formats);
		
		//Added Connection For now, Remove it later
		
		//String map="6";
		//ConnectionProvider connections=new ConnectionProvider();
		//Connection connection=connectionProvider.getConnection("jdbc/dqDatabase");
		String[] attachments = null;
		try {
			attachments = EmailUtility.getAttachmentsArray(totalformats, dataAfterEncode, reportSourceType, reportName, csvData);
			logger.debug("Inside Schedule Job: with attachments:  "+attachments);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//DbUtils.closeQuietly(connection);
		logger.debug("attachments:  " + attachments);
		logger.debug("Subject:  " + Subject);
		logger.debug("totalRecipients:  " + totalRecipients);
		SendMail mailClient = new SendMail();
		// Send mail to all the recipients with all the attachments
		mailClient.sendMessage(hostName, port, totalRecipients, from, isAuthenticated, isSSLEnabled, user, password, Subject, Body, attachments);
		String id = String.valueOf(jobid);
		String nOFExecution = json.getString("NoOfExecutions");
		logger.debug("nOFExecution:  " + nOFExecution);
		if (json.getJSONObject("ScheduleOptions").getString("endsRadio").equalsIgnoreCase("After")) {
			logger.debug("json.getString(NoOfExecutions):  " + json.getString("NoOfExecutions"));
			JSONObject jsonObject1 = new JSONObject();
			jsonObject1 = xmlOperation.getParticularObject(path, id);
			logger.debug("jsonObject for Specific ID: " + jsonObject1);
			logger.debug("NUMBER OF EXECUTION:  " + jsonObject1.getString("NoOfExecutions"));
			newData.accumulate("NoOfExecutions", Integer.parseInt(jsonObject1.getString("NoOfExecutions")) + 1);
		}
		newData.accumulate("NextExecutionOn", context.getNextFireTime());
		newData.accumulate("LastExecutedOn", context.getFireTime());

		logger.debug("newData :  " + newData);
		logger.debug("context.getResult():  " + context.getResult());
		XmlOperationWithParser xmlOperationWithParser = new XmlOperationWithParser();

		xmlOperationWithParser.updateExistingXml(newData, path, id);

	}

	/*
	 * public void modifyJsonField(net.sf.json.JSONObject obj, String path,
	 * net.sf.json.JSONObject newData) { net.sf.json.JSONArray jsonArray = new
	 * net.sf.json.JSONArray(); XmlOperation xmlOperation = new XmlOperation();
	 * 
	 * if (obj.getJSONObject("Schedules").get("Schedule") instanceof
	 * net.sf.json.JSONArray) { jsonArray =
	 * obj.getJSONObject("Schedules").getJSONArray("Schedule"); for (int count =
	 * 0; count < jsonArray.size(); count++) { if
	 * (jsonArray.getJSONObject(count).getString("id") == newData
	 * .getString("id")) { int NoOfExecutions = obj.getJSONObject("Schedules")
	 * .getJSONArray("Schedule").getJSONObject(count) .getInt("NoOfExecutions");
	 * logger.debug("NoOfExecutions:" + NoOfExecutions);
	 * newData.accumulate("NoOfExecutions", NoOfExecutions + 1);
	 * 
	 * int endAfterExecution = obj.getJSONObject("Schedules")
	 * .getJSONArray("Schedule").getJSONObject(count)
	 * .getJSONObject("ScheduleOptions") .getInt("EndAfterExecutions");
	 * 
	 * if (endAfterExecution == NoOfExecutions) { newData.put("NoOfExecutions",
	 * NoOfExecutions); newData.accumulate("isActive", false);
	 * logger.debug("Stop This Job"); int jobKey = newData.getInt("id"); String
	 * jobId = String.valueOf(jobKey); ScheduleProcess schedulerProcess = new
	 * ScheduleProcess(); schedulerProcess.delete(jobId); } }
	 * 
	 * } } else { int NoOfExecutions = obj.getJSONObject("Schedules")
	 * .getJSONObject("Schedule").getInt("NoOfExecutions");
	 * logger.debug("NoOfExecutions:" + NoOfExecutions);
	 * newData.accumulate("NoOfExecutions", NoOfExecutions + 1);
	 * 
	 * int endAfterExecution = obj.getJSONObject("Schedules")
	 * .getJSONObject("Schedule").getJSONObject("ScheduleOptions")
	 * .getInt("EndAfterExecutions");
	 * 
	 * if (endAfterExecution == NoOfExecutions) { newData.put("NoOfExecutions",
	 * NoOfExecutions); newData.accumulate("isActive", false);
	 * logger.debug("Stop This Job"); int jobKey = newData.getInt("id"); String
	 * jobId = String.valueOf(jobKey); ScheduleProcess schedulerProcess = new
	 * ScheduleProcess(); schedulerProcess.delete(jobId); } obj =
	 * xmlOperation.modifyJSONObjectById(obj, newData); } String data =
	 * xmlOperation.convertJsonToXml(obj .getJSONArray("Schedules"));
	 * logger.debug("newData:" + newData);
	 * xmlOperation.writeStringIntoFile(data, path);
	 * 
	 * }
	 */
}
