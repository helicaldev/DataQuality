package com.helicaltech.pcni.controller;

import com.helicaltech.pcni.exceptions.ConfigurationException;
import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.rules.BusinessRulesUtils;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utility.ApplicationUtilities;
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

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This controller is responsible for downloading files which are saved in user
 * specified directory if enableSavedResult tag is enabled in setting.xml file
 *
 * @author Muqtar Ahmed
 * @version 1.1
 * @since 1.0
 */
@Controller
@Component
public class EnableSaveResultController {

	private static final Logger logger = LoggerFactory.getLogger(EnableSaveResultController.class);

	/**
	 * this method is mapped with downloadEnableSavedReport request with Http
	 * get method, this method gets the file name from request parameter and
	 * search file in solution folder and download the file
	 *
	 * @param dir
	 *            directory name
	 * @param file
	 *            file name
	 * @param response
	 *            HttpServletResponse
	 * @return String sucess or failure
	 */

	@RequestMapping(value = "/downloadEnableSavedReport", method = RequestMethod.GET)
	public @ResponseBody String downLoadEnableSavedResult(@RequestParam("dir") String dir, @RequestParam("filename") String file,
			HttpServletResponse response) {
		ApplicationProperties applicationProperties = ApplicationProperties.getInstance();

		System.out.println("Solution Directory" + applicationProperties.getSolutionDirectory());
		ConfigurationFileReader reportPropertiesReader = new ConfigurationFileReader();

		String actualFile = applicationProperties.getSolutionDirectory() + File.separator + dir + File.separator + file;
//		String actualFile =dir + File.separator +file;
		// String actualFile = "D:\\EFW" + File.separator + dir + File.separator
		// + file;

		JSONProcessor processor = new JSONProcessor();
		JSONObject resultObject = processor.getJSON(actualFile, false);
		String resultFile = resultObject.getString("resultFile");
		String resultDirectory = resultObject.getString("resultDirectory");

		String downloadName = resultObject.getString("resultName");
		JSONObject security = resultObject.getJSONObject("security");
		String userName = security.getString("createdBy");
		String userOrganization = security.getString("organization");
		String absulateFileFilePath = applicationProperties.getSolutionDirectory() + File.separator + resultDirectory + File.separator + resultFile;
		String absulateDirectoryPath = applicationProperties.getSolutionDirectory() + File.separator + resultDirectory;

		if (resultFile.matches("^\\[.*\\]$")) {
			try {
				throw new ConfigurationException("resultFile tag cannot be null");
			} catch (ConfigurationException e) {
				logger.error("resultFile tag in " + resultFile + " is null");
				return "failure";
			}
		}
		if (resultDirectory.matches("^\\[.*\\]$")) {
			try {
				throw new ConfigurationException("resultDirectory tag cannot be null");
			} catch (ConfigurationException e) {
				logger.error("resultDirectory tag in " + resultFile + " is null");
				return "failure";
			}
		}
		if (downloadName.matches("^\\[.*\\]$")) {
			try {
				throw new ConfigurationException("resultName tag cannot be null");
			} catch (ConfigurationException e) {
				logger.error("resultName tag in " + resultFile + " is null");
				return "failure";
			}
		}
		if (resultDirectory != null || resultDirectory.trim().length() > 0) {
			try {
				File resultDirExist = new File(absulateDirectoryPath);
				if (!resultDirExist.exists()) {
					throw new ConfigurationException("Result Directory  does not exist");
				}
			} catch (ConfigurationException e) {
				logger.error(resultDirectory + "Does not exist");
				return "failure";
			}
		}
		if (resultFile != null || resultFile.trim().length() > 0) {
			try {
				File resultFileExist = new File(absulateFileFilePath);
				if (!resultFileExist.exists()) {
					throw new ConfigurationException("Result file does not exist");
				}
			} catch (ConfigurationException e) {
				logger.error(resultFile + "does not exist");
				return "failure";
			}
		}
		List<String> userDetails = BusinessRulesUtils.getUserDetails();

		if (userOrganization.matches("^\\[.*\\]$")) {
			userOrganization = "";
		}
		if (userName.equals(userDetails.get(0)) && userOrganization.equals(userDetails.get(1) == null ? "" : userDetails.get(1))) {
			String[] tokens = resultFile.split("\\.(?=[^\\.]+$)");
			if (tokens.length > 1) {
				String attachement = downloadName + "." + tokens[1];
				Map<String, String> map = reportPropertiesReader.read("reports.properties");
				// Set the content type for the response from the properties
				// file
				Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, String> entry = iterator.next();
					if (entry.getKey().equals(tokens[1])) {
						response.setContentType(entry.getValue());
					}
					iterator.remove();
				}
				OutputStream outputStream = null;
				FileInputStream fileInputStream = null;
				try {
					// Set the response headers
					response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", attachement));

					// Write to outputStream
					fileInputStream = new FileInputStream(absulateFileFilePath);
					outputStream = response.getOutputStream();
					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = fileInputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}
					outputStream.flush();
					ApplicationUtilities.closeResource(fileInputStream);
					ApplicationUtilities.closeResource(outputStream);
				} catch (IOException e) {
					logger.error("Error in file operation");
					e.printStackTrace();
				}
			} else {
				logger.error(resultFile + "Does not have the extension");
				return "failure";
			}
		} else {
			logger.error("User is not authorized to download the file");
			return "failure";
		}
		return null;
	}
}
