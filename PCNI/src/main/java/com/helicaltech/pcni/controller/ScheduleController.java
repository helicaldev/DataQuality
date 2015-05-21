package com.helicaltech.pcni.controller;

import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.rules.BusinessRulesUtils;
import com.helicaltech.pcni.scheduling.ConvertIntoCronExpression;
import com.helicaltech.pcni.scheduling.ScheduleProcess;
import com.helicaltech.pcni.scheduling.XmlOperation;
import com.helicaltech.pcni.scheduling.XmlOperationWithParser;
import com.helicaltech.pcni.utils.ConfigurationFileReader;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@Component
public class ScheduleController {
	private final static Logger logger = LoggerFactory.getLogger(ScheduleController.class);

	/**
	 * <p>
	 * get ID from request and send JSONObject as response of that id
	 * </p>
	 *
	 * @param id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/getScheduleData", method = RequestMethod.POST)
	public @ResponseBody String getScheduleData(@RequestParam("id") String id, HttpServletRequest request, HttpServletResponse response) {
		String idd = request.getParameter("id");
		logger.debug("idd:  " + idd);
		Map<String, String> getpropertiesFileValue = new HashMap<String, String>();
		ConfigurationFileReader propertiesFileReader = new ConfigurationFileReader();
		getpropertiesFileValue = propertiesFileReader.read("project.properties");
		String SchedulerPath = getpropertiesFileValue.get("schedularPath");

		boolean idExist = false;
		JSONObject jsonObject = new JSONObject();
		JSONObject message = new JSONObject();
		JSONProcessor jsonProcessor = new JSONProcessor();
		jsonObject = jsonProcessor.getJSON(SchedulerPath, true);
		XmlOperation xmlOperation = new XmlOperation();
		idExist = xmlOperation.searchId(jsonObject, idd);
		logger.debug("idExist: " + idExist);
		String responseData = "";
		if (idExist) {
			jsonObject = xmlOperation.getParticularObject(SchedulerPath, idd);
			responseData = jsonObject.toString();
			logger.debug("jsonObject sending to frontEnd for update" + jsonObject);
		} else {
			responseData = message.accumulate("message", "Id not found in schedule.xml").toString();
		}
		return responseData;
	}

	/**
	 * <p>
	 * get update schedule related information from front end in JSON format
	 * update scheduling.xml and update trigger.
	 * </p>
	 *
	 * @param request
	 * @param response
	 * @author Prashansa
	 */
	@RequestMapping(value = "/updateScheduleData", method = RequestMethod.POST)
	public @ResponseBody String updateScheduleData(HttpServletRequest request, HttpServletResponse response) {
		logger.debug("Inside updateScheduleData");
		XmlOperationWithParser xmlOperationWithParser = new XmlOperationWithParser();
		String data = request.getParameter("data");
		JSONObject existingScheduleOption = new JSONObject();
		JSONObject updatedScheduleOption = new JSONObject();
		ConvertIntoCronExpression convertIntoCronExpression = new ConvertIntoCronExpression();
		String existionCronexpression = "";
		String updatedCronExpression = "";
		ScheduleProcess scheduleProcess = new ScheduleProcess();
		logger.debug("data:  " + data);
		boolean isValidUSer = false;
		String id = "";
		String path = "";
		JSONObject jsonObject = new JSONObject();
		JSONObject jsonData = new JSONObject();
		jsonObject.accumulate("data", data);
		jsonData = jsonObject.getJSONObject("data");
		logger.debug("Soption==" + jsonData.getString("SchedulingJob"));
		logger.debug("jsonData:  " + jsonData);
		id = jsonData.getString("@id");
		jsonData.remove("@id");
		jsonData.remove("@type");
		String reportParameters = jsonData.getJSONObject("SchedulingJob").getString("reportParameters");
		String EmailSettings = jsonData.getJSONObject("SchedulingJob").getString("EmailSettings");
		String reportDirectory = jsonData.getJSONObject("SchedulingJob").getString("reportDirectory");
		String reportFile = jsonData.getJSONObject("SchedulingJob").getString("reportFile");
		jsonData.remove("SchedulingJob");
		logger.debug("data afer remove SchedulingJob" + jsonData);
		logger.debug("data after remove:" + jsonData);
		jsonData.accumulate("reportParameters", reportParameters);
		jsonData.accumulate("EmailSettings", EmailSettings);
		jsonData.accumulate("reportDirectory", reportDirectory);
		jsonData.accumulate("reportFile", reportFile);
		logger.debug("Final jsonData:  " + jsonData);
		updatedScheduleOption = jsonData.getJSONObject("ScheduleOptions");
		String updatedStartDate = jsonData.getJSONObject("ScheduleOptions").getString("StartDate");
		logger.debug("updatedScheduleOption:  " + updatedScheduleOption);
		/*
		 * getting Schedule path from project.properties file
		 */
		Map<String, String> getpropertiesFileValue = new HashMap<String, String>();
		ConfigurationFileReader propertiesFileReader = new ConfigurationFileReader();
		getpropertiesFileValue = propertiesFileReader.read("project.properties");
		path = getpropertiesFileValue.get("schedularPath");
		String message = "";
		isValidUSer = isValidUSer(id, path);
		logger.debug("isValidUSer:" + isValidUSer);
		if (isValidUSer) {
			XmlOperation xmlOperation = new XmlOperation();
			JSONProcessor jsonProcessor = new JSONProcessor();
			jsonObject = jsonProcessor.getJSON(path, true);
			boolean idExist = xmlOperation.searchId(jsonObject, id);
			logger.debug("idExist: " + idExist);
			if (idExist) {
				jsonObject = xmlOperation.getParticularObject(path, id);
				existingScheduleOption = jsonObject.getJSONObject("ScheduleOptions");
				logger.debug("jsonObject sending to frontEnd for update" + jsonObject);
				logger.debug("existingScheduleOption:  " + existingScheduleOption);
				existionCronexpression = convertIntoCronExpression.convertDateIntoCronExpression(existingScheduleOption);
				updatedCronExpression = convertIntoCronExpression.convertDateIntoCronExpression(updatedScheduleOption);
				DateFormat formatter = null;
				Date sDate = null;
				Date eDate = null;
				try {
					formatter = new SimpleDateFormat("yyyy-MM-dd");
					sDate = (Date) formatter.parse(updatedStartDate);
					logger.debug("sDate:  " + sDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				String endDate = "";
				if (jsonData.getJSONObject("ScheduleOptions").getString("endsRadio").equalsIgnoreCase("on")) {
					endDate = jsonData.getJSONObject("ScheduleOptions").getString("EndDate");
					try {
						formatter = new SimpleDateFormat("yyyy-MM-dd");
						eDate = (Date) formatter.parse(endDate);
						logger.debug("eDate:  " + eDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}

				}
				scheduleProcess.updateTriger(existionCronexpression, updatedCronExpression, id, sDate, eDate);

				xmlOperationWithParser.removeElementFromXml(path, id);
				xmlOperationWithParser.updateJobInExistingXML(jsonData, path, Integer.parseInt(id));
				message = "data updated successfully";
			}
		} else {
			message = "nota a valid user";
		}
		return message;
	}

	/**
	 * this method is responsible to validate user
	 *
	 * @param idd
	 *            a <code>String</code> which specify schedule id
	 * @param SchedulerPath
	 *            a <code>String</code> which specify scheduling.xml path
	 * @return true if valid user else return false
	 */
	public boolean isValidUSer(String idd, String SchedulerPath) {
		XmlOperation xmlOperation = new XmlOperation();
		JSONObject jsonObject = new JSONObject();
		boolean isvalidUser = false;
		jsonObject = xmlOperation.getParticularObject(SchedulerPath, idd);
		logger.debug("jsonObject for " + idd + ": " + jsonObject);
		String userName = jsonObject.getJSONObject("Security").getString("CreatedBy");
		logger.debug("userName: " + userName);
		String organization = jsonObject.getJSONObject("Security").getString("Organization");
		logger.debug("organization: " + organization);

		String loginUserName = BusinessRulesUtils.getUserDetails().get(0);
		String loginUserOrg = BusinessRulesUtils.getUserDetails().get(1);
		logger.debug("loginUserName:  " + loginUserName);
		logger.debug("loginUserOrg: " + loginUserOrg);
		if (userName.equals(loginUserName)) {
			if (null == organization || "[]".equals(organization) && loginUserOrg == null) {
				isvalidUser = true;
			} else if (organization.equals(loginUserOrg)) {
				isvalidUser = true;
			} else {
				isvalidUser = false;
			}
		}

		return isvalidUser;
	}
}
