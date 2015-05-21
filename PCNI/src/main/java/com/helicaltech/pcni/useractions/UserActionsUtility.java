package com.helicaltech.pcni.useractions;

//import com.helical.efw.exceptions.RequiredParametersNotProvidedException;
import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * A utility class which is being used by rest of the file operation module.
 *
 * @author Rajasekhar
 * @version 1.0
 * @since 1.1
 */
public class UserActionsUtility {
	private static final Logger logger = LoggerFactory.getLogger(UserActionsUtility.class);
	/**
	 * Instance of singleton class <code>ApplicationProperties</code>
	 */
	private final ApplicationProperties properties;
	/**
	 * The EFW solution directory
	 */
	private final String solutionDirectory;

	/**
	 * Initializes the member variables properties and solutionDirectory
	 */
	public UserActionsUtility() {
		this.properties = ApplicationProperties.getInstance();
		this.solutionDirectory = properties.getSolutionDirectory();
	}

	
		/**
	 * Deletes the specified favourite file
	 *
	 * @param reportFile
	 *            The name of the file
	 * @param reportDirectory
	 *            The location of reportFile
	 */
	public void deleteFavouriteFile(String reportFile, String reportDirectory) {
		File xmlFile = new File(this.solutionDirectory + File.separator + reportDirectory + File.separator + reportFile);
		JSONProcessor processor = new JSONProcessor();
		JSONObject jsonObject = processor.getJSON(xmlFile.toString(), false);

		try {
			String fileTobeSearched = jsonObject.getString("favourite");
			logger.info("Trying to delete the file " + fileTobeSearched);
			boolean result = deleteFile(this.solutionDirectory, fileTobeSearched);
			logger.debug("File " + fileTobeSearched + " deleted status " + result);
		} catch (JSONException ex) {
			logger.error("JSONException", ex);
		}
	}

	/**
	 * Searches the solution directory and deletes the file passed as parameter
	 * fileTobeSearched. The base path is absolute.
	 *
	 * @param basePath
	 *            The location to be searched
	 * @param fileTobeSearched
	 *            The name of file which has to be deleted
	 * @return true if successfully deleted
	 */
	public boolean deleteFile(String basePath, String fileTobeSearched) {
		String path = search(basePath, fileTobeSearched);
		logger.debug("Search result for the path is " + path);
		File file = new File(path);
		if (file.delete()) {
			logger.debug("The file " + fileTobeSearched + " is successfully deleted!");
			return true;
		}
		return false;
	}

	/**
	 * Searches for the file(fileTobeSearched) in input location(basePath).
	 * Returns <code>null</code> if the file is not found.
	 *
	 * @param basePath
	 *            The location where the file to be searched
	 * @param fileTobeSearched
	 *            Name of file which has to be searched for
	 * @return The file path as string
	 */
	public String search(String basePath, String fileTobeSearched) {
		File[] files = new File(basePath).listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isFile() && fileTobeSearched.equals(file.getName())) {
					logger.debug("The file " + fileTobeSearched + " is found and its path is " + file.toString());
					return file.toString();
				} else if (file.isDirectory() && !file.getName().equalsIgnoreCase("system") && !file.getName().equalsIgnoreCase("images")) {
					logger.debug("file is " + file + " is directory");
					String path = search(file.toString(), fileTobeSearched);
					if (path != null) {
						return path;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get the reportName from the file reportFile in reportDirectory
	 *
	 * @param reportFile
	 *            A <code>String</code> which specifies report file name
	 * @param reportDirectory
	 *            A <code>String</code> which specifies directory where report
	 *            file exists
	 * @return The reportName tag from the specified file
	 */
	public String getReportName(String reportFile, String reportDirectory) {
		JSONProcessor processor = new JSONProcessor();
		JSONObject jsonObject = processor.getJSON(this.properties.getSolutionDirectory() + File.separator + reportDirectory + File.separator
				+ reportFile, false);

		return jsonObject.getString("reportName");
	}
}
