package com.helicaltech.pcni.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.helicaltech.pcni.export.TempDirectoryCleaner;
import com.helicaltech.pcni.datasource.ConnectionProvider;
import com.helicaltech.pcni.datasource.IJdbcDao;
import com.helicaltech.pcni.driver.JDBCDriver;
//import com.helicaltech.pcni.service.UserService;
import com.helicaltech.pcni.exceptions.ConfigurationException;
import com.helicaltech.pcni.export.CSVUtility;
import com.helicaltech.pcni.export.EnableSaveResult;
import com.helicaltech.pcni.export.ReportsProcessor;
import com.helicaltech.pcni.export.ReportsUtility;
import com.helicaltech.pcni.login.LoginController;
import com.helicaltech.pcni.login.LoginForm;
import com.helicaltech.pcni.process.BaseLoader;
//import com.helical.efw.useractions.UserActionsUtility;
import com.helicaltech.pcni.resourceloader.DataSetResource;
import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.resourceloader.TemplateReader;
import com.helicaltech.pcni.rules.BusinessRulesUtils;
import com.helicaltech.pcni.scheduling.ScheduleProcess;
import com.helicaltech.pcni.scheduling.ScheduleProcessCall;
import com.helicaltech.pcni.scheduling.XmlOperation;
import com.helicaltech.pcni.scheduling.XmlOperationWithParser;
import com.helicaltech.pcni.security.AuthenticationImpl;
import com.helicaltech.pcni.security.User;
import com.helicaltech.pcni.singleton.ApplicationProperties;
//import com.helicaltech.pcni.export.TempDirectoryCleaner;
import com.helicaltech.pcni.useractions.UserActionsUtility;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import com.helicaltech.pcni.utils.ConfigurationFileReader;
import com.helicaltech.pcni.utils.ExecuteReport;
//import com.helicatech.pcni.security.Authentication;

@Controller
public class ApplicationController{

private final ApplicationProperties applicationProperties;
	
	@Autowired
	private IJdbcDao jdbcDao;

	@Autowired
	private ConnectionProvider connectionProvider;
	

	@Autowired
	private DataSetResource dataSource;
	
	
	AuthenticationImpl authentication= new AuthenticationImpl();
	
	private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread() + " : "	+ ApplicationController.class);
	
	@Autowired
	public ApplicationController(ServletContext servletContext) {
		
		this.applicationProperties = ApplicationProperties.getInstance();
	
	}
	
	
	public ApplicationController() 
	{
		this.applicationProperties = ApplicationProperties.getInstance();
	}

	/**
	 * Provides the default landing page view and model of the application.
	 * Mapped to hdi.html
	 *
	 * @param request
	 *            HttpServletRequest object
	 * @param response
	 *            HttpServletResponse object
	 * @return ModelAndView object
	 */

	@RequestMapping(value = "/pcni", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView hdi(HttpServletRequest request, HttpServletResponse response) {
		logger.info("Start of the EFW-Application. hdi controller is called.");
		ModelAndView error = new ModelAndView();
		String userName=request.getParameter("j_username");
		String password=request.getParameter("j_password");
		Map<String, JSONObject> userMap= authentication.getUserMap();
		
			if(userName != null && password != null)
			{
				if(userMap != null )
				{
					if(userMap.get(userName) != null)
					{
						request.setAttribute("service", "run");
						String dir = request.getParameter("dir");
						String file = request.getParameter("file");
						if(logger.isDebugEnabled())
						{
							logger.debug("dir = " + dir + ", file = " + file);
						}
						/*
						 * Attach the directory files and folders JSON to response body
						 */
						getSolutionResources();
						try{
							request.getRequestDispatcher("/getEFWSolution.html").forward(request, response);
							logger.debug("Returning null from /hdi mapping");
						}
						 catch (ServletException e) {
							logger.error("ServletException occurred", e);
							e.printStackTrace();
						} catch (IOException e) {
							logger.error("IOException occurred", e);
							e.printStackTrace();
						}
					}
					else
					{
						if(logger.isDebugEnabled())
							logger.debug("User is not authenticated");
						// If user is not authenticated
						try 
						{
							request.setAttribute("isThroughURL", "true");
							request.getRequestDispatcher("/login.html").forward(request, response);
						}
						catch(Exception e)
						{
							logger.error("ApplicationException occurred");
							error.addObject("message", "User is not authenticated");
							error.setViewName("errorPage");
							return error;
						}
					}
			}
			//JSONObject userDetail= userMap.get(userName);
		}
		else
		{
			request.setAttribute("service", "run");
			String dir = request.getParameter("dir");
			String file = request.getParameter("file");
			logger.debug("dir = " + dir + ", file = " + file);
			/*
			 * Attach the directory files and folders JSON to response body
			 */
			getSolutionResources();
			try {
				request.getRequestDispatcher("/getEFWSolution.html").forward(request, response);
				logger.debug("Returning null from /hdi mapping");
				return null;
			} catch (ServletException e) {
				logger.error("ServletException occurred", e);
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("IOException occurred", e);
				e.printStackTrace();
			}
		}
		
			@SuppressWarnings("unchecked")
			Enumeration<String> enumeration = request.getParameterNames();
			
			if (enumeration.hasMoreElements()) {
				if (request.getParameter("dir") != null && request.getParameter("file") != null) {
				
				} else {
					try {
						throw new ConfigurationException("Directory name and/or file name parameters are not available");
					} catch (ConfigurationException e) {
						logger.error("ApplicationException occurred", e);
						error.addObject("message", e.getMessage());
						error.setViewName("errorPage");
						return error;
					}
				}
			} else {
				logger.debug("No parameter supplied.");
				// return new ModelAndView();
			}
				return new ModelAndView();
	}

	
	/*
	 * Handle user Landing page. Redirects user according to their role
	 */
	@RequestMapping(value = "/welcome", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView welcomeUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ModelAndView serviceLoadView = new ModelAndView();
		if(LoginForm.getInstance().getRole().equalsIgnoreCase("ROLE_ADMIN"))
		{
			serviceLoadView.setViewName("pages/menu/admin_menu");
		}
		else if(LoginForm.getInstance().getRole().equalsIgnoreCase("ROLE_USER"))
		{
			logger.debug("Setting user template");
			serviceLoadView.setViewName("pcni");
		}
		else
		{
			serviceLoadView.setViewName("login");
		}
		return serviceLoadView;
	}

	/*
	 * Handle Admin Menu Landing page. Redirects user according to their role
	 */
	@RequestMapping(value = "/adminCreateUser", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ModelAndView adminCreateUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ModelAndView serviceLoadView = new ModelAndView();
		if(LoginForm.getInstance().getRole().equalsIgnoreCase("ROLE_ADMIN"))
		{
				serviceLoadView.setViewName("pages/menu/admin_menu");
				
				//request.getRequestDispatcher("/welcome.html").forward(request, response);
//			else if(menu.equalsIgnoreCase("HDI"))
//			{
//				LoginForm.getInstance().setRole("ROLE_USER");
//				serviceLoadView.setViewName("pcni");
//				//request.getRequestDispatcher("/user.html").forward(request, response);
//			}
		
		}
		else
		{
			serviceLoadView.setViewName("login");
		}
		return serviceLoadView;
	}


	/*
	 * Handle Admin Menu Landing page. Redirects user according to their role
	 */
	@RequestMapping(value = "/adminHDI", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody ModelAndView adminHDIMenu(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ModelAndView serviceLoadView = new ModelAndView();
		if(LoginForm.getInstance().getRole().equalsIgnoreCase("ROLE_ADMIN"))
		{
//				LoginForm.getInstance().setRole("ROLE_USER");
				serviceLoadView.setViewName("pcni");
				//request.getRequestDispatcher("/user.html").forward(request, response);
			
		}
		else
		{
			serviceLoadView.setViewName("login");
		}
		return serviceLoadView;
	}

	
	
	
	/*
	 * Handle user Landing page. Redirects user according to their role
	 */
	@RequestMapping(value = "/user", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView userMenu(@RequestParam("menu") String menu,HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ModelAndView serviceLoadView = new ModelAndView();
				serviceLoadView.setViewName("pcni");
		
		return serviceLoadView;
	}
	
	
	
	@RequestMapping(value = "/login", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException{
		
		Connection connection = connectionProvider.getConnection("jdbc/pcni");
		HttpSession session = request.getSession();
		if(logger.isDebugEnabled()) {
			logger.debug("The connection is null? " + (connection == null));
		}
		String userName=request.getParameter("j_username");
		String passWord=request.getParameter("j_password");
		
		logger.debug("Authentication user "+userName);
		
		LoginController loginController= new LoginController();
		
		
			String isAuthenticated=	loginController.performLogin(connection, userName, passWord);
			DbUtils.closeQuietly(connection);
			
			
			if(isAuthenticated.equals("Success"))
			{
				session.setAttribute("user", userName);
				session.setMaxInactiveInterval(30*60);
				
				User user= new User(userName, passWord, "", true, session.getId());
				authentication.registerUser(user,userName);
				
				if(loginController.user_role != null){
					session.setAttribute("Role", loginController.user_role);
				} else {
					session.setAttribute("Role", "user");
				}
				if(request.getAttribute("isThroughURL") == null)
				{
					request.getRequestDispatcher("/WEB-INF/jsp/pages/sections/welcome.jsp").forward(request, response);
				}
				else if(request.getAttribute("isThroughURL") != null && request.getAttribute("isThroughURL").equals("true"))
				{
					try {
						request.setAttribute("service", "run");
//						String dir = request.getParameter("dir");
//						String file = request.getParameter("file");
						/*
						 * Attach the directory files and folders JSON to response body
						 */
						getSolutionResources();
						request.getRequestDispatcher("/getEFWSolution.html").forward(request, response);
						logger.debug("Returning null from /hdi mapping");
					//	return null;
					} catch (ServletException e) {
						logger.error("ServletException occurred", e);
						e.printStackTrace();
					} catch (IOException e) {
						logger.error("IOException occurred", e);
						e.printStackTrace();
					}
				}
			}
			else
			{
				request.setAttribute("errorMessage", "Bad credentials!!");
				request.getRequestDispatcher("/").forward(request, response);
			}
		
	}
	
	/**
	 * Returns the data from the database by executing the query
	 *
	 * @param data
	 *            The request parameter data
	 * @return Returns the data by executing the query
	 */
	@RequestMapping(value = "/executeDatasource", method = RequestMethod.POST)
	public @ResponseBody String executeDatasource(@RequestParam("data") String data) {
		logger.info("Inside executeDatasource method, data");
		return dataSource.getResultSet(data).toString();
	}
	
	
	@RequestMapping(value = "/logout", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView performLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		ModelAndView serviceLoadView = new ModelAndView();
		if(session.getAttribute("user")!=null)
		{
			session.invalidate();
		}
		
		//request.getRequestDispatcher("/").forward(request, response);
		serviceLoadView.setViewName("login");
		return serviceLoadView;
	}
	
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getEFWSolution", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getEFWSolution(@RequestParam("dir") String dirPath, @RequestParam("file") String htmlFile, HttpServletResponse response,
			HttpServletRequest request) {

//		HttpSession session= request.getSession();
		logger.debug("Inside getEFWSolution  Session ");
		logger.debug("dir = " + dirPath);
		
		ModelAndView serviceLoadView = new ModelAndView();
		
		String absolutePath = applicationProperties.getSolutionDirectory();
		String templateData;
		String htmlTemplate = null;

		htmlTemplate=absolutePath + File.separator + dirPath + File.separator +htmlFile;
		
		
		String encoding = ApplicationUtilities.getEncoding();

		if (htmlTemplate.isEmpty() || htmlTemplate.length() == 0) {
			logger.error("EFW file has no template element. HTML file not found.");
		} else {
			String templateFile;
			if (htmlTemplate.contains("solution:")) {
				templateFile = htmlTemplate.replaceFirst("solution:", absolutePath + File.separator);
			} else {
				templateFile = absolutePath + File.separator + dirPath + File.separator + htmlFile;
//				templateFile = dirPath + File.separator + efwTemplate;
			}

				logger.debug("Tempalte: File: "+templateFile);
			TemplateReader templateReader = new TemplateReader(new File(templateFile));
			templateData = templateReader.readTemplate();
			Enumeration<String> enumeration = request.getParameterNames();

			Map<String, String> parameterValues = new HashMap<String, String>();
			while (enumeration.hasMoreElements()) {
				String name = enumeration.nextElement();
				logger.debug("param name:  " + name);
				String[] value = request.getParameterValues(name);
				if (value.length > 1) {
					String temp = "";
					logger.debug("Multiple value for " + name);
					for (int i = 0; i < value.length; i++) {
						temp = temp.trim() + (i == 0 ? "" : ",") + "'" + value[i] + "'";
					}
					parameterValues.put(name, temp);
				} else {
					parameterValues.put(name, "'" + value[0] + "'");
				}
			}

			for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
				logger.debug("templateData contains ${} " + templateData.contains("${" + entry.getKey() + "}"));
				if (templateData.contains("${" + entry.getKey() + "}")) {
					templateData = templateData.replace("${" + entry.getKey() + "}", entry.getValue());
				}
			}

			if (request.getAttribute("service") != null) {
				logger.debug("Inside service condition '" + request.getAttribute("service") + "'");
				serviceLoadView.addObject("dir", dirPath.replace("\\", "\\\\"));
				serviceLoadView.addObject("templateData", templateData);
				serviceLoadView.setViewName("serviceLoadView");
			} else {
				OutputStream outputStream;
				try {
					outputStream = response.getOutputStream();
					outputStream.write(templateData.getBytes(encoding));
					outputStream.flush();
					ApplicationUtilities.closeResource(outputStream);
					return null;
				} catch (IOException e) {
					logger.error("IOException occurred. " + e);
					e.printStackTrace();
				}
			}
		}
		
		return serviceLoadView;
	}

	
	/**
	 * Returns the visualization data from the ChartService
	 *
	 * @param chartData
	 *            Request parameter data
	 * @return Returns the visualization data from the ChartService
	 */
	@RequestMapping(value = "/visualizeData", method = {RequestMethod.POST,RequestMethod.GET})
	public @ResponseBody String visualizeData(@RequestParam("data") String chartData,HttpServletRequest request, HttpServletResponse response) {

		JSONObject parameterJsonObject = (JSONObject) JSONSerializer.toJSON(chartData);
		
//		String mapId=request.getParameter("map");
		String mapId=parameterJsonObject.getString("map");
		Connection connections = connectionProvider.getConnection("jdbc/dqDatabase");
		try {
			
			JSONArray jsonArray;
			Map<String, String> confQueryMap=null;	
			confQueryMap=ConfigurationFileReader.getMapFromPropertiesFile(new File(applicationProperties.getSolutionDirectory()+File.separator+"System"+File.separator+"DQQuery.conf"));
			
			
			logger.debug("The connection is null? " + (connections == null));

			JSONObject jsonData = null;
			
				String query=""; 
				Object query1= confQueryMap.get("query_"+mapId);
				query=query1.toString();
				if(query.contains("${"))
				{
					int parameterCount=StringUtils.countMatches(query, "$");
					for(int param=1; param<=parameterCount; param++)
					{
						String paramName=confQueryMap.get("param_"+mapId+"_"+param);
						String replace="${"+paramName+"}";
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
				
				jsonData=jdbcDriver.getJSONData(connections, query, applicationProperties);
				
				
			if (jsonData != null ) {
				logger.debug("Returning the jsonObject from the controller.");
				jsonArray=jsonData.getJSONArray("data");
				
				return jsonArray+"";
			} else {
				logger.error("The jsonObject is null. Returning null!");
			}
		} catch (ConfigurationException e) {
			logger.error("ApplicationException occurred. " + e);
			e.printStackTrace();
		}
			
		return null;
	}

	
	
	@RequestMapping("/getSolutionResources")
	public @ResponseBody String getSolutionResources() {
		
		BaseLoader baseLoader = new BaseLoader(applicationProperties);
		// returning the jsonObject of the solution directory.
		
		return baseLoader.loadResources();
	}
	
	
	
	@RequestMapping(value = "/saveReport", method = RequestMethod.POST)
	public @ResponseBody String saveReport(@RequestParam("reportDirectory") String reportDirectory,
			HttpServletRequest request, HttpServletResponse response) {
		logger.debug("Inside Save controller...");
		String operation = request.getParameter("operation");
		String reportName = request.getParameter("reportName");
		String reportFile = request.getParameter("reportFile");
		
		if (operation == null) {
			operation = "add";
		}

		if ("add".equalsIgnoreCase(operation)) {
			if ((reportName == null) || "".equals(reportName) || (reportName.trim().length() == 0)) {
				
					request.setAttribute("response", "Please provide reportName!");
					logger.error("reportName of the file to be saved is null!!");
					return "failure";
			}
			String location = request.getParameter("location");
			Assert.notNull(location, "location parameter is not provided!");
			String result = saveReport(reportFile, reportDirectory, location, reportName, request);
			if ("success".equals(result)) {

				request.setAttribute("response", "Successfully saved the report!");
			} else if ("couldn't save the file".equals(result)) {
				request.setAttribute("response", "Couldn't save the report!");
			}

			return result;
		} else if ("update".equalsIgnoreCase(operation)) {
			/*
			 * For future use
			 */
		} else if ("favourite".equalsIgnoreCase(operation)) {
		//	UserActionsUtility utility = new UserActionsUtility();
			
			String result="";
			String favouriteLocation = request.getParameter("favouriteLocation");
			if ("unmark".equalsIgnoreCase(request.getParameter("markAsFavourite"))) {
				// Delete the favourite file and remove mark the file as
				// favourite
				deleteFavouriteFile(favouriteLocation,reportFile, reportDirectory);
				result = markFavourite(reportFile, reportDirectory, false, null);
			} else {
				// Create a favourite file and save its name in RDF(mark as
				// favourite)
				String favFileName = createFavouriteFile(reportFile, reportDirectory, favouriteLocation);
				logger.debug("favFileName = " + favFileName);
				result =markFavourite(reportFile, reportDirectory, true, favFileName);
			}
			if ("success".equals(result)) {
				request.setAttribute("response", "Successfully marked as Favourite!");
			} else if ("alreadyFavourite".equals(result)) {
				request.setAttribute("response", "The report is already marked as Favourite!");
			} else if ("unmarked".equals(result)) {
				request.setAttribute("response", "The report is already unmarked as Favourite!");
			} else if ("wasNotAFavourite".equals(result)) {
				request.setAttribute("response", "The report was not favourite to unmark!");
			} else {
				request.setAttribute("response", "Couldn't mark as favourite due to insufficient privileges");
			}
			return result;
		}

		request.setAttribute("response", "Couldn't save the report");
		return null;
	}

	
	/**
	 * Saves a particular report in the dashboard as a saved report by writing
	 * an efwsr file with relevant information about the EFW file from which the
	 * report was developed.
	 * <p/>
	 * The saved file consists of schedulingReference, indicating the scheduling
	 * related information if any.
	 * <p/>
	 * The visibility of the saved report is set to be true by default.
	 * <p/>
	 * Note: reportParameters of the report is an optional parameter.
	 *
	 * @param reportFile
	 *            The file under concern
	 * @param reportDirectory
	 *            The directory of the report
	 * @param location
	 *            The location where the report has to be saved
	 * @param reportName
	 *            The name of the report to be saved
	 * @param request
	 *            The http request object
	 * @return Writes a string to the response body to avoid 404 indicating the
	 *         result of processing
	 */

	private String saveReport(String reportFile, String reportDirectory, String location, String reportName, HttpServletRequest request) {
		String reportParameters = null;
		int schedulingReference = 0;

		if (request.getParameter("ScheduleOptions") != null) {
			schedulingReference = schedule(request);
		}
		if (request.getParameter("reportParameters") != null) {
			reportParameters = request.getParameter("reportParameters");
		}
		logger.debug("reportParameters = " + reportParameters);

		// Obtain schedulingReference
		logger.debug("scheduleReference = " + schedulingReference);

		String visible = "true";
		if (request.getParameter("visible") != null) {
			visible = request.getParameter("visible");
			logger.debug("Visibility is set to be " + visible);
		}
		logger.debug("By default visibility is set to be " + visible);

		// Accumulate the parameters
		JSONObject jsonObject = new JSONObject();
		jsonObject.accumulate("reportName", reportName);

	
//			jsonObject.accumulate("reportFile", list.get(0));
//			jsonObject.accumulate("reportDirectory", list.get(1));
			jsonObject.accumulate("reportFile", reportFile);
			jsonObject.accumulate("reportDirectory", reportDirectory);
	
		if (!(reportParameters == null)) {
			jsonObject.accumulate("reportParameters", "<![CDATA[" + reportParameters + "]]>");
		}

		jsonObject.accumulate("visible", visible);
		if (schedulingReference != 0) {
			jsonObject.accumulate("schedulingReference", schedulingReference);
		}

		JSONObject security = new JSONObject();
		security.accumulate("createdBy",LoginForm.getInstance().getjUserName());
		security.accumulate("organization","");
		jsonObject.accumulate("security", security);
		jsonObject.accumulate("favourite", false);

		File fileLocation = new File(location);
		if (!fileLocation.exists()) {
			if (fileLocation.mkdir()) {
				logger.debug("Created the directory location");
			}
		}

		String extension = "rdf";
		ApplicationProperties properties = ApplicationProperties.getInstance();
		location = properties.getSolutionDirectory() + File.separator + location;
		File xmlFile = new File(location + File.separator + reportName + "_" + System.currentTimeMillis() + "." + extension);

		logger.debug("jsonObject = " + jsonObject);
		return ApplicationUtilities.writeReportXML(xmlFile, jsonObject, "SR") ? "success" : "couldn't save the file";
	}


	
	@RequestMapping(value = "/executeSavedReport", method = RequestMethod.POST)
	public ModelAndView executeSavedReport(@RequestParam("dir") String directoryName, @RequestParam("file") String fileName,
			HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("decorator", "empty");
		ApplicationProperties properties = ApplicationProperties.getInstance();
		logger.debug("Inside Execute Save report");
		ExecuteReport executeReport = new ExecuteReport();
		ModelAndView serviceLoadView = new ModelAndView();
		UserActionsUtility userUtility = new UserActionsUtility();
		String solutionFolder = properties.getSolutionDirectory();
		String strDirToChk = directoryName;
		File dirToChk = new File(strDirToChk);
		String path = null;

		if ((directoryName == null) || "".equalsIgnoreCase(directoryName) || (directoryName.trim().length()) < 0) {
			path = userUtility.search(solutionFolder, fileName) == null ? "" : userUtility.search(solutionFolder, fileName);
			try {
				if ("".equalsIgnoreCase(directoryName) || directoryName.trim().length() < 0) {
					throw new ConfigurationException("File does not exist in file system");
				}
			} catch (ConfigurationException e) {
				logger.error(fileName + "does not exist in file system");
				return null;
			}

		} else if (!dirToChk.isDirectory()) {
			path = userUtility.search(solutionFolder, fileName) == null ? "" : userUtility.search(solutionFolder, fileName);
			try {
				if ("".equalsIgnoreCase(path) || path.trim().length() < 0) {
					throw new ConfigurationException("File does not exist in file system!!!");
				}
			} catch (ConfigurationException e) {
				logger.error("File does not exist in file system!!!");
				return null;
			}
		} else {
			path = properties.getSolutionDirectory() + File.separator + directoryName + fileName;
		//	path = directoryName + File.separator + fileName;
			File pathFile = new File(path);
			if (!pathFile.exists()) {
				path = userUtility.search(solutionFolder, fileName) == null ? "" : userUtility.search(solutionFolder, fileName);
				try {
					if ("".equalsIgnoreCase(path) || path.length() < 0) {
						throw new ConfigurationException("path not found in system ");
					}

				} catch (ConfigurationException e) {
					logger.error("path not found in system ");
					return null;
				}
			}
		}

		logger.debug("Trying to get JSON for the resource " + path);

		JSONProcessor processor = new JSONProcessor();
		JSONObject jsonObject = processor.getJSON(path, false);
		/*
		 * Get the saved JSON from the rdf file. Convert string to JSONObject
		 */
		String efwDirectory = jsonObject.getString("reportDirectory");
		String efwFile = jsonObject.getString("reportFile");
		
		JSONObject parametersJSON = (JSONObject) JSONSerializer.toJSON(jsonObject.getString("reportParameters"));

		List<String> list = executeReport.execute(efwDirectory, efwFile, parametersJSON);
		//logger.debug("List obtained from ExecuteReport is  " + list);
		String templateData = list.get(0);
		String dirPath = list.get(1);
		String replaceQuotesinParameters=jsonObject.getString("reportParameters").replace("\"","\\\"");
		serviceLoadView.addObject("dir", dirPath.replace("\\", "\\\\"));
		serviceLoadView.addObject("templateData", templateData);
		serviceLoadView.addObject("chartData", replaceQuotesinParameters);
		serviceLoadView.setViewName("serviceLoadView");
		return serviceLoadView;
	}
	
	
	/**
	 * Creates favourite file in the specified location with extension from
	 * setting.xml
	 *
	 * @param reportFile
	 *            The efwsr file
	 * @param reportDirectory
	 *            The location of reportFile
	 * @param favouriteLocation
	 *            A <code>String</code> which specifies favourite file location
	 * @return The name of the favourite file
	 */
	public String createFavouriteFile(String reportFile, String reportDirectory, String favouriteLocation) {
		if (favouriteLocation == null) {
				logger.error("Provide request parameter favouriteLocation");
				return null;
		}
		
		JSONProcessor processor = new JSONProcessor();
		JSONObject reportFileName = processor.getJSON(applicationProperties.getSolutionDirectory() + File.separator +reportDirectory + File.separator + reportFile, false);
		JSONObject jsonObject = new JSONObject();
		jsonObject.accumulate("savedReportFileName",reportFile);
		jsonObject.accumulate("visible", "true");
		jsonObject.accumulate("reportName",reportFileName.getString("reportName"));

		//List<String> userDetails = BusinessRulesUtils.getUserDetails();
		//logger.debug("userDetails = " + userDetails);

		JSONObject security = new JSONObject();
		
		// Add user name
		security.accumulate("createdBy", LoginForm.getInstance().getjUserName());
		security.accumulate("organization", "");
		jsonObject.accumulate("security", security);

		String extension = "fav";
		String[] array = reportFile.split("\\.(?=[^\\.]+$)");
		String fileName = array[0] + "." + extension;
		String path = new File(applicationProperties.getSolutionDirectory() + File.separator + favouriteLocation + File.separator + fileName).toURI().getPath();
		File xmlFile = new File(path);
		ApplicationUtilities.writeReportXML(xmlFile, jsonObject, "efwfav");
		return fileName;
	}
	
	/**
	 * Marks file as favourite or not favourite based on isFavourite boolean.
	 * The xml content of the file will be modified to mark or un mark. While
	 * marking as favourite, the favFileName will be assigned to the tag
	 * favourite. While un marking false will be written to the tag.
	 *
	 * @param reportFile
	 *            The name of the file for which favourite is to be created
	 * @param reportDirectory
	 *            The location of reportFile
	 * @param isFavourite
	 *            Indicates true or false
	 * @param favFileName
	 *            Specifies favourite file name
	 * @return A string that represents the result of the operation
	 */
	public String markFavourite(String reportFile, String reportDirectory, boolean isFavourite, String favFileName) {

		File xmlFile = new File(this.applicationProperties.getSolutionDirectory() + File.separator + reportDirectory + File.separator + reportFile);
		logger.debug("xmlFile = " + xmlFile);
		JSONProcessor processor = new JSONProcessor();
		JSONObject jsonObject = processor.getJSON(xmlFile.toString(), false);

		
		try {
			if (jsonObject.getJSONObject("security").getString("createdBy").equalsIgnoreCase(LoginForm.getInstance().getjUserName())) {
				
					logger.debug("Matching user credentials!");
					if (isFavourite) {
						if ("false".equalsIgnoreCase(jsonObject.getString("favourite"))) {
							String reportParameters = jsonObject.getString("reportParameters");
							jsonObject.discard("favourite");
							jsonObject.discard("reportParameters");
							jsonObject.accumulate("favourite", favFileName);
							jsonObject.accumulate("reportParameters", "<![CDATA[" + reportParameters + "]]>");
							logger.debug("jsonObject = " + jsonObject);
							if (ApplicationUtilities.writeReportXML(xmlFile, jsonObject, "efwsr")) {
								return "success";
							}
						} else {
							return "alreadyFavourite";
						}
					} else {
						// Unmark as favourite
						if (jsonObject.getString("favourite") != null) {
							String reportParameters = jsonObject.getString("reportParameters");
							jsonObject.discard("favourite");
							jsonObject.discard("reportParameters");
							jsonObject.accumulate("favourite", isFavourite);
							jsonObject.accumulate("reportParameters", "<![CDATA[" + reportParameters + "]]>");
							logger.debug("jsonObject = " + jsonObject);
							if (ApplicationUtilities.writeReportXML(xmlFile, jsonObject, "efwsr")) {
								return "unmarked";
							}
						} else {
							return "wasNotAFavourite";
						}
					}
				
			}
		} catch (JSONException ex) {
			logger.error("JSONException", ex);
		}
		return "couldn't update";
	}
	
	
	
	/**
	 * Deletes the specified favourite file
	 *
	 * @param reportFile
	 *            The name of the file
	 * @param reportDirectory
	 *            The location of reportFile
	 */
	public void deleteFavouriteFile(String favLocation,String reportFile, String reportDirectory) {
		File xmlFile = new File(applicationProperties.getSolutionDirectory() + File.separator + reportDirectory + File.separator + reportFile);
		JSONProcessor processor = new JSONProcessor();
		JSONObject jsonObject = processor.getJSON(xmlFile.toString(), false);

		try {
			String fileTobeSearched = jsonObject.getString("favourite");
			logger.info("Trying to delete the file " + fileTobeSearched);
			boolean result = false;
			File file=new File(ApplicationProperties.getInstance().getSolutionDirectory()+File.separator+ favLocation+ File.separator+fileTobeSearched);
			if (file.delete()) {
				logger.debug("The file " + fileTobeSearched + " is successfully deleted!");
				result=true;
			}
			logger.debug("File " + fileTobeSearched + " deleted status " + result);
		} catch (JSONException ex) {
			logger.error("JSONException", ex);
		}
	}

	/**
	 * <p>
	 * The main service for printing of the dashboard view in various formats.
	 * Accepts htmlString, which is the source of the dashboard view, the format
	 * of the report to be generated i.e. pdf or png or jpeg. This service
	 * handles only such requests for which the html source is provided. The
	 * report file is written to the response stream as an attachment.
	 * </p>
	 *
	 * @param htmlString
	 *            The request parameter htmlString
	 * @param format
	 *            The request parameter format of the report to be downloaded
	 * @param request
	 *            HttpServletRequest object
	 * @param response
	 *            HttpServletResponse object
	 * @return Returns null as the required file is sent through the response as
	 *         an attachment just to avoid 404
	 */
	@RequestMapping(value = "/downloadReport", method = RequestMethod.POST)
	public @ResponseBody String downloadReport(@RequestParam("xml") String htmlString, @RequestParam("format") String format,
			HttpServletRequest request, HttpServletResponse response) {

		String resultNameTag = request.getParameter("reportName") == null ? ReportsUtility.getReportName(request.getParameter("reportName"))
				: request.getParameter("reportName");

		String reportName;
		if(request.getParameter("reportNameParam") == null)
		{
			reportName ="_" + System.currentTimeMillis();
		}
		else
		{
			reportName = request.getParameter("reportNameParam")+ "_"+ System.currentTimeMillis();;
		}

		// Get the destination file which should be served
		ReportsProcessor reportsProcessor = new ReportsProcessor();
		List<String> locationsList = reportsProcessor.generateReportUsingHTMLSource(htmlString, format, reportName);
		String destinationFile = locationsList.get(0);

		String attachmentName = reportName + "." + format;

		//ConfigurationFileReader reportPropertiesReader = new ConfigurationFileReader();
		Map<String, String> map = ConfigurationFileReader.getMapFromPropertiesFile(new File(applicationProperties.getSolutionDirectory()+File.separator+"System"+File.separator+"Reports"+File.separator+"reports.properties"));

		// Set the content type for the response from the properties file
		Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			if (entry.getKey().equals(format)) {
				response.setContentType(entry.getValue());
			}
			iterator.remove();
		}

		OutputStream outputStream = null;
		FileInputStream fileInputStream = null;
		try {
			// Set the response headers
			response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", attachmentName));

			// Write to outputStream
			fileInputStream = new FileInputStream(destinationFile);
			outputStream = response.getOutputStream();
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = fileInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.flush();
			// below code copy the download file and create the file in user
			// specified location if enablesavedresult is enabled in setting.xml
			// file
			String enableSaveResult = "true";

				if ("TRUE".equalsIgnoreCase(enableSaveResult)) {
					String tempDir = new File(ApplicationProperties.getInstance().getSolutionDirectory()+File.separator + "System" + File.separator + "Temp") + File.separator + reportName + "." + format;
					File fileToDownload = new File(tempDir);
					String fileReportPath = request.getParameter("filename");
					String reportNameParam = request.getParameter("reportNameParam") == null ? fileReportPath : request
							.getParameter("reportNameParam");
					String reportName1 = reportNameParam;
					String dirReportPath = request.getParameter("dir");
					String reportType = request.getParameter("reportType");
					String resultName = resultNameTag;
					String resultDirectory = request.getParameter("resultDirectory");

					EnableSaveResult enableSaveResultObj = new EnableSaveResult(reportName1, reportType, resultName, resultDirectory, fileToDownload,
							dirReportPath, fileReportPath);
					boolean paramValidate = enableSaveResultObj.ValidateRequestParam();

					if (paramValidate == true) {
						enableSaveResultObj.copyReportFromTemp();
						enableSaveResultObj.saveEfwResultFile();
					}

				}
			
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException occurred as the " + format + " file is not generated.", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException occurred", e);
			e.printStackTrace();
		} finally {
			ApplicationUtilities.closeResource(fileInputStream);
			ApplicationUtilities.closeResource(outputStream);
		}
		return null;
	}
	

	/**
	 * <p>
	 * This method is responsible to execute a favourite report saved earlier by
	 * the user.
	 * </p>
	 * <p>
	 * Sets the request attribute decorator and forwards the request to
	 * serviceLoadView.jsp page after processing.
	 * </p>
	 *
	 * @param directoryName
	 *            The name of the directory which is relative
	 * @param fileName
	 *            The favourite file to be executed
	 * @param request
	 *            The http request object
	 * @param response
	 *            The http response object
	 * @return A ModelAndView object
	 */
	@RequestMapping(value = "/executeFavourite", method = RequestMethod.POST)
	public ModelAndView executeFavourite(@RequestParam("dir") String directoryName, @RequestParam("file") String fileName,
			HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("decorator", "empty");
		JSONProcessor processor = new JSONProcessor();
		ApplicationProperties properties = ApplicationProperties.getInstance();
		File resource = new File(applicationProperties.getSolutionDirectory() + File.separator + directoryName + File.separator + fileName);
//		File resource = new File(fileName);
		logger.debug("Looking for the resource " + resource);
		JSONObject json = processor.getJSON(resource.toString(), false);
		String file = json.getString("savedReportFileName");
		logger.debug("The file obtained from " + directoryName + " and " + fileName + " is file " + file);
		logger.debug("Searching for the file " + file);

		UserActionsUtility userActionsUtility = new UserActionsUtility();
		String filePath = userActionsUtility.search(properties.getSolutionDirectory(), file);
		//String filePath =file;
		logger.info("The path for the file is " + filePath);
		json = processor.getJSON(filePath, false);

		ExecuteReport executeReport = new ExecuteReport();
		JSONObject parametersJSON = (JSONObject) JSONSerializer.toJSON(json.getString("reportParameters"));
		List<String> list = executeReport.execute(json.getString("reportDirectory"), json.getString("reportFile"), parametersJSON);
		logger.debug("list = " + list);
		String templateData = list.get(0);
		String dirPath = list.get(1);

		ModelAndView serviceLoadView = new ModelAndView();
		String replaceQuotesinParameters=json.getString("reportParameters").replace("\"","\\\"");
		serviceLoadView.addObject("dir", dirPath.replace("\\", "\\\\"));
		serviceLoadView.addObject("templateData", templateData);
		serviceLoadView.addObject("chartData", replaceQuotesinParameters);
		serviceLoadView.setViewName("serviceLoadView");
		return serviceLoadView;
	}

	/**
	 * The CSV data of a particular chart is returned. File is written to the
	 * response stream as an attachment.
	 *
	 * @param data
	 *            The request parameter data
	 * @param request
	 *            HttpServletRequest object
	 * @param response
	 *            HttpServletResponse object
	 * @throws SQLException 
	 */
	@RequestMapping(value = "/exportData", method = RequestMethod.GET)
	public String exportData(@RequestParam("data") String data, HttpServletRequest request, HttpServletResponse response) throws SQLException {
		logger.debug("Inside the exportData method, data = " + data);

		// Get the attachment name if provided. If not use time stamp
		String resultNameTag = request.getParameter("reportName") == null ? ReportsUtility.getReportName(request.getParameter("reportName"))
				: request.getParameter("reportName");

		String attachmentName = ReportsUtility.getReportName(request.getParameter("reportName"));
		OutputStream outputStream = null;
		FileInputStream fileInputStream = null;
		try {
			// Get CSV data as a string
			
			CSVUtility csvWriter = new CSVUtility();
			String csvData = csvWriter.getCSVData(data);
			// saving the csv file in temp folder
			String tempDir =TempDirectoryCleaner.getTempDirectory() + File.separator + attachmentName + ".csv";
			File fileToDownload = new File(tempDir);
			ApplicationUtilities.createAFile(fileToDownload, csvData);

			// Set the response headers
			response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", attachmentName + ".csv"));
			response.setContentType("application/octet-stream");
			// Write to outputStream
			fileInputStream = new FileInputStream(tempDir);
			outputStream = response.getOutputStream();
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = fileInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			// Flush and close the outputStream
			outputStream.flush();

				String enableSaveResult = "true";
				
				if ("TRUE".equalsIgnoreCase(enableSaveResult)) {
					String fileReportPath = request.getParameter("filename");
					String reportNameParam = request.getParameter("reportNameParam") == null ? fileReportPath : request
							.getParameter("reportNameParam");
					String reportName = reportNameParam;
					String dirReportPath = request.getParameter("dir");
					String reportType = request.getParameter("reportType");
					String resultName = resultNameTag;
					String resultDirectory = request.getParameter("resultDirectory");

					EnableSaveResult enableSaveResultObj = new EnableSaveResult(reportName, reportType, resultName, resultDirectory, fileToDownload,
							dirReportPath, fileReportPath);
					boolean paramValidate = enableSaveResultObj.ValidateRequestParam();

					if (paramValidate == true) {
						enableSaveResultObj.copyReportFromTemp();
						enableSaveResultObj.saveEfwResultFile();
					}
				}
			
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException occurred as the " + "csv" + " file is not generated.", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException ", e);
			e.printStackTrace();
		} finally {
			ApplicationUtilities.closeResource(outputStream);
			ApplicationUtilities.closeResource(fileInputStream);
		}
		return null;
	}

	
	/**
	 * Sets the application context or provides access to application context
	 *
	 * @param applicationContext
	 *            The applicationContext of the app
	 * @throws BeansException
	 *             If some thing goes wrong ):
	 */

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ScheduleProcessCall scheduleProcessCall = new ScheduleProcessCall();
		String schedulePath = scheduleProcessCall.getSchedulePath();
		logger.debug("schedulePath: " + schedulePath);
		File file = new File(schedulePath);
		if (file.exists()) {
			//userService = (UserService) applicationContext.getBean("userDetailsService");

			//logger.debug("password: " + currentUser.getPassword());
			String password = LoginForm.getInstance().getjPassword();
			String baseUrl = scheduleProcessCall.gettingBaseUrl();
			ScheduleProcess scheduleProcess = new ScheduleProcess();
			XmlOperation xmlOperation = new XmlOperation();
			logger.debug("baseUrl: " + baseUrl);

			List<String> listId = new ArrayList<String>();
			listId = xmlOperation.getIdFromJson(schedulePath);
			for (int idCount = 0; idCount < listId.size(); idCount++) {
				scheduleProcess.delete(listId.get(idCount));
			}
			scheduleProcessCall.scheduleOperation(schedulePath, baseUrl, password);
		}
	}
	
	/**
	 * This method is responsible to get schedule related data from request
	 * object, retrieving data from request object and convert into
	 * <code>JSONObject</code> send JSONObject for scheduling process. The http
	 * request object contains command , reportName , reportDirectory,
	 * reportFile, location, reportParameters, ScheduleOptions, EmailSettings,
	 * isActive. If request object does not contain command then it will add
	 * schedule.
	 *
	 * @param request
	 *            The http request object
	 * @return An integer that is the schedule id of currently created schedule.
	 */

	public int schedule(HttpServletRequest request) {

		logger.debug("INSIDE SaveReport Controller...");
		Map<String, String> getpropertiesFileValue = new HashMap<String, String>();
		ConfigurationFileReader propertiesFileReader = new ConfigurationFileReader();
		getpropertiesFileValue = propertiesFileReader.read("project.properties");
		String SchedulerPath = getpropertiesFileValue.get("schedularPath");
		String scheduleUrl = getpropertiesFileValue.get("schedule.url");
		String command = request.getParameter("command");
		String reportName = request.getParameter("reportName");
		String reportDirectory = request.getParameter("reportDirectory");
		String reportFile = request.getParameter("reportFile");
		String location = request.getParameter("location");
		String reportParameters = request.getParameter("reportParameters");
		String ScheduleOptions = request.getParameter("ScheduleOptions");
		String emailSettingsString = request.getParameter("EmailSettings");
		String isActive = request.getParameter("isActive");
		int maxid = 0;

		logger.debug("reportParameters: " + reportParameters);
		logger.debug("location: " + location);
		logger.debug("reportFile: " + reportFile);
		logger.debug("reportName: " + reportName);
		logger.debug("reportDirectory: " + reportDirectory);
		logger.debug("ScheduleOptions" + ScheduleOptions);
		logger.debug("emailSettingsString: " + emailSettingsString);
		logger.debug("isActive:  " + isActive);
		logger.debug("command:  " + command);

		JSONObject jsonObject = new JSONObject();
		XmlOperation xmlOperation = new XmlOperation();
		XmlOperationWithParser xmlOperationWithParser = new XmlOperationWithParser();
		ScheduleProcessCall scheduleProcessCall = new ScheduleProcessCall();
		JSONProcessor jsonProcessor = new JSONProcessor();
		if (command != null && command != "" && reportName != null && reportName != "" && reportParameters != null && reportParameters != ""
				&& ScheduleOptions != null && ScheduleOptions != "" && emailSettingsString != null && emailSettingsString != "") {
			if (command.equalsIgnoreCase("add") || command.equals("") || command == null) {

				logger.debug("Inside Add Command...");

				File file = new File(SchedulerPath);
				if (file.exists()) {
					logger.debug("File exist");

					JSONObject convertXmlToJson = new JSONObject();
					convertXmlToJson = jsonProcessor.getJSON(SchedulerPath, true);
					maxid = xmlOperation.searchMaxIdInXml(convertXmlToJson);

					logger.debug("maxid: " + maxid);
					jsonObject = addNewJobWithoutCdata(ScheduleOptions, emailSettingsString, reportParameters, isActive, reportDirectory, reportFile,
							reportName);

					logger.debug("New JSON DATA : " + jsonObject);
					String id = xmlOperationWithParser.addNewJobInExistingXML(jsonObject, SchedulerPath, maxid + 1);
					logger.debug("id:  " + id);
					String pathh = request.getContextPath();
					String servletPath = request.getServletPath();
					StringBuffer servletUrl = request.getRequestURL();
					logger.debug("path:  " + pathh);
					String baseUrlPath = servletUrl.toString().replace(servletPath, "");
					baseUrlPath = baseUrlPath.trim() + "/" + scheduleUrl;
					logger.debug("baseUrlPath:  " + baseUrlPath);
					scheduleProcessCall.scheduleSpecificJob(SchedulerPath, String.valueOf(maxid + 1), baseUrlPath);
				} else {
					jsonObject = addNewJobWithoutCdata(ScheduleOptions, emailSettingsString, reportParameters, isActive, reportDirectory, reportFile,
							reportName);
					logger.debug("New JSON DATA : " + jsonObject);

					String pathh = request.getContextPath();
					String servletPath = request.getServletPath();
					StringBuffer servletUrl = request.getRequestURL();
					logger.debug("pathh:  " + pathh);
					String baseUrlPath = servletUrl.toString().replace(servletPath, "");
					baseUrlPath = baseUrlPath.trim() + "/" + scheduleUrl;
					logger.debug("baseUrlPath:  " + baseUrlPath);

					xmlOperationWithParser.addNewJobInXML(jsonObject, SchedulerPath);

					scheduleProcessCall.scheduleSpecificJob(SchedulerPath, String.valueOf("1"), baseUrlPath);

				}
			} else if (command.equalsIgnoreCase("scheduleSpecificJob")) {
				logger.debug("Inside scheduleSpecificJob command");
				String pathh = request.getContextPath();
				String id = request.getParameter("id");
				String servletPath = request.getServletPath();
				StringBuffer servletUrl = request.getRequestURL();
				logger.debug("pathh:  " + pathh);
				String baseUrlPath = servletUrl.toString().replace(servletPath, "");
				baseUrlPath = baseUrlPath.trim() + "/" + scheduleUrl;
				logger.debug("baseUrlPath:  " + baseUrlPath);
				scheduleProcessCall.scheduleSpecificJob(SchedulerPath, id, baseUrlPath);
			}
		} else {
			try {
				throw new ConfigurationException("Parameters which is comming from request is null");
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
		return maxid + 1;
	}

	/**
	 * <p>
	 * This method is responsible to create JSONObject on the basis of given
	 * parameters name and value.
	 * </p>
	 *
	 * @param ScheduleOptions
	 *            The scheduling option parameter from request
	 * @param emailSettingsString
	 *            A string
	 * @param reportParameters
	 *            The report parameters request parameter
	 * @param isActive
	 *            a boolean
	 * @param reportDirectory
	 *            The directory of the report
	 * @param reportFile
	 *            The file under concern
	 * @param reportName
	 *            The name of the report
	 * @return <code>JSONObject</code> which contains schedule related data and
	 *         security related data.
	 */
	public JSONObject addNewJobWithoutCdata(String ScheduleOptions, String emailSettingsString, String reportParameters, String isActive,
			String reportDirectory, String reportFile, String reportName) {
		JSONObject jsonObject = new JSONObject();
		if (!(reportParameters == null)) {
			jsonObject.accumulate("reportParameters", reportParameters);

		}
		if (!(ScheduleOptions == null)) {
			jsonObject.accumulate("ScheduleOptions", ScheduleOptions);

		}

		if (!(emailSettingsString == null)) {
			jsonObject.accumulate("EmailSettings", emailSettingsString);
		}

		if (!(isActive == null)) {
			jsonObject.accumulate("isActive", isActive);
		}
		if (!(reportDirectory == null)) {
			jsonObject.accumulate("reportDirectory", reportDirectory);
		}
		if (!(reportFile == null)) {
			jsonObject.accumulate("reportFile", reportFile);
		}
		if (!(reportName == null)) {
			jsonObject.accumulate("JobName", reportName);
		}
		List<String> userDetails = BusinessRulesUtils.getUserDetails();
		logger.debug("userDetails = " + userDetails);
		JSONObject security = new JSONObject();
		security.accumulate("CreatedBy", userDetails.get(0));
		security.accumulate("Organization", "");
		jsonObject.accumulate("Security", security);
		logger.debug("JSON Before creating xml tag:" + jsonObject);
		return jsonObject;
	}
	

	
}