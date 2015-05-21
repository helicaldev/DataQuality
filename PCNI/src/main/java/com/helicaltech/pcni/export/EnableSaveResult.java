package com.helicaltech.pcni.export;

import com.helicaltech.pcni.exceptions.ConfigurationException;
import com.helicaltech.pcni.exceptions.RequiredParametersNotProvidedException;

import com.helicaltech.pcni.rules.BusinessRulesUtils;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * This class is used for copying downloaded report file from temp to user
 * specified location and creating file in user specified location
 *
 * @author Muqtar Ahmed
 * @version 1.1
 * @since 1.0
 */
public class EnableSaveResult {

	private static final Logger logger = LoggerFactory.getLogger(EnableSaveResult.class);
	ApplicationProperties properties = ApplicationProperties.getInstance();
	private String reportName;
	private String reportType;
	private String resultName;
	private String resultDirectory;
	private File scFile;
	private String dirReportPath;
	private String fileReportPath;

	/**
	 * over loaded constructor
	 *
	 * @param reportName
	 *            name of the report from request parameter
	 * @param reportType
	 *            type of report from request parameter
	 * @param resultName
	 *            result name from request parameter
	 * @param resultDirectory
	 *            location of directory where file to be copied and saved
	 * @param scFile
	 *            source report file which to be copied to destination
	 * @param dirReportPath
	 *            location of directory from request parameter as string
	 * @param fileReportPath
	 *            source report file from request parameter as string
	 */
	public EnableSaveResult(String reportName, String reportType, String resultName, String resultDirectory, File scFile, String dirReportPath,
			String fileReportPath) {
		this.reportName = reportName;
		this.reportType = reportType;
		this.resultName = resultName;
		this.resultDirectory = resultDirectory;
		this.scFile = scFile;
		this.dirReportPath = dirReportPath;
		this.fileReportPath = fileReportPath;
	}

	/**
	 * default constructor
	 */
	public EnableSaveResult() {
	}

	/**
	 * This method is used for copying report file from source to destination
	 */
	public void copyReportFromTemp() {
		List<String> userDetails = BusinessRulesUtils.getUserDetails();
		String destFile = properties.getSolutionDirectory() + File.separator + resultDirectory + File.separator + userDetails.get(0) + "_"
				+ userDetails.get(1) + "_" + scFile.getName();
		try {
			File dest = new File(destFile);
			if (!dest.exists())
				dest.createNewFile();
			FileUtils.copyFile(scFile, dest);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is used for creating file
	 *
	 * @return string success or failure
	 */

	public String saveEfwResultFile() {
		List<String> userDetails = BusinessRulesUtils.getUserDetails();
		String resultDirectoryLocation = properties.getSolutionDirectory() + File.separator + resultDirectory;
		String outputFleName = resultDirectoryLocation + File.separator + userDetails.get(0) + "_" + userDetails.get(1) + "_" + scFile.getName();
		String resultFile = userDetails.get(0) + "_" + userDetails.get(1) + "_" + scFile.getName();
		String visible = "true";

		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String currentDate = formatter.format(new Date());
		JSONObject enabledSaveResultJsonObj = new JSONObject();
		enabledSaveResultJsonObj.accumulate("reportName", reportName);
		enabledSaveResultJsonObj.accumulate("reportDir", dirReportPath);
		enabledSaveResultJsonObj.accumulate("reportFile", fileReportPath);
		enabledSaveResultJsonObj.accumulate("reportType", reportType);
		enabledSaveResultJsonObj.accumulate("resultName", resultName);
		enabledSaveResultJsonObj.accumulate("resultDirectory", resultDirectory);
		enabledSaveResultJsonObj.accumulate("resultFile", resultFile);
		enabledSaveResultJsonObj.accumulate("runDate", currentDate);
		enabledSaveResultJsonObj.accumulate("visible", visible);
		logger.debug("userDetails = " + userDetails);

		JSONObject security = new JSONObject();
		security.accumulate("createdBy", userDetails.get(0));
		security.accumulate("organization", userDetails.get(1) == null ? "" : userDetails.get(1));
		enabledSaveResultJsonObj.accumulate("security", security);
		String extension = getEnabledResultExtension();
		String[] file = outputFleName.split("\\.(?=[^\\.]+$)");
		String resultFileName = "";

		if (file.length > 1) {

			resultFileName = file[0];
		} else {
			logger.debug("File has no extension");
		}

		File xmlFile = new File(resultFileName + "." + extension);
		return ApplicationUtilities.writeReportXML(xmlFile, enabledSaveResultJsonObj, "efwresult") ? "success" : "couldn't save the file";
	}

	/**
	 * This method is used to get the extension for the file which need to be
	 * create
	 *
	 * @return String
	 */
	public String getEnabledResultExtension() {
//		ApplicationProperties properties = ApplicationProperties.getInstance();
//		JSONProcessor processor = new JSONProcessor();
//		JSONObject json = processor.getJSON(properties.getSettingPath(), false);
//		String extension = null;
//		try {
//			JSONObject extensionsJSONObject = json.getJSONObject("Extentions").getJSONObject("efwresult");
//			if (extensionsJSONObject == null) {
//				throw new ConfigurationException("");
//			}
//			extension = extensionsJSONObject.getString("#text");
//			logger.debug("efwresult text value = " + extension);
//		} catch (JSONException ex) {
//			logger.error("Please provide attribute visible to be true in the Extensions tag for efwresult", ex);
//		} catch (ConfigurationException e) {
//			logger.error("ApplicationException", e);
//		}
		return "result";
	}

	/**
	 * this method responsible for validating the parameters
	 *
	 * @return boolean value
	 */

	public boolean ValidateRequestParam() {

		if ((dirReportPath == null) || "".equals(dirReportPath) || (dirReportPath.trim().length() < 0)) {
			try {
				throw new RequiredParametersNotProvidedException("Please provide a name for dir parameter for report");
			} catch (RequiredParametersNotProvidedException e) {
				logger.error("dir parameter is null!!", e);
				return false;
			}
		}

		if ((fileReportPath == null) || "".equals(fileReportPath) || (fileReportPath.trim().length() < 0)) {
			try {
				throw new RequiredParametersNotProvidedException("Please provide a name for file parameter for report");
			} catch (RequiredParametersNotProvidedException e) {

				logger.error("file parameter is null!!", e);
				return false;
			}
		}

		if ((reportType == null) || "".equals(reportType) || (reportType.trim().length() < 0)) {
			try {
				throw new RequiredParametersNotProvidedException("Please provide a name for report type for report");
			} catch (RequiredParametersNotProvidedException e) {

				logger.error("report type for file to save is null!!", e);
				return false;
			}
		}

		if ((resultDirectory == null) || "".equals(resultDirectory) || (resultDirectory.trim().length() < 0)) {
			try {
				throw new RequiredParametersNotProvidedException("Please provide a result directory for report");
			} catch (RequiredParametersNotProvidedException e) {

				logger.error("result directory for file to save is null!!", e);
				return false;
			}
		}

		if (resultDirectory != null || resultDirectory.trim().length() > 0) {
			try {
				String chkExistDir = properties.getSolutionDirectory() + File.separator + resultDirectory;
				File file = new File(chkExistDir);
				if (!file.isDirectory()) {
					throw new ConfigurationException("Directory not exist");
				}
			} catch (ConfigurationException e) {
				e.getStackTrace();
				logger.error("Directory does not exist in file system!!!", e);
				return false;
			}
		}
		return true;
	}
}
