package com.helicaltech.pcni.resourcereader;

import com.helicaltech.pcni.exceptions.ConfigurationException;
import com.helicaltech.pcni.resourceloader.DirectoryLoader;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import com.helicaltech.pcni.validator.DirectoryValidator;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * this class implements the IRsource interface and overload the getResources
 * method, the responsibility of this class convert the directories which is
 * read from root path to JSONArray String return the string
 *
 * @author muqtar ahmed
 * @version 1.1
 * @since 1.0
 */
public class XMLResourceReader {

	private static final Logger logger = LoggerFactory.getLogger(XMLResourceReader.class);

	/**
	 * root path
	 */

	private String path;

	/**
	 * JSONObject reference
	 */

	private JSONObject visibleExtensions;

	/**
	 * overloaded method get the directories and convert into JSONArray and
	 * return the string
	 */

	public String getResources() throws ConfigurationException {
		logger.info("Inside " + this.getClass().getName());

		String string = null;
		DirectoryValidator directoryValidator = new DirectoryValidator();
		logger.info("efwSolution path = {} ", path);
		directoryValidator.setDirectory(path);
		if (directoryValidator.isDirectoryPresent()) {
			if (!directoryValidator.isDirectoryEmpty()) {
				DirectoryLoader directoryLoader = new DirectoryLoader();
				//List<Map<String, String>> listOfDirectories = directoryLoader.getSolutionDirectory(getPath());
				//string = ApplicationUtilities.getJSONArray(listOfDirectories);
				
				List<Map<String, String>> listOfDirectories = directoryLoader.getFoldersAndFiles(getPath());
			//	string="["+directoryLoader.getFoldersAndFiles(getPath())+"]";
				
				string = ApplicationUtilities.getJSONArray(listOfDirectories);
			//	string="[{\"name\":\"New Report\",\"path\":\"DataQualityReport/New Report\",\"children\":[{\"name\":\"Report Templates - Data Quality\",\"path\":\"DataQualityReport/New Report/Report Templates - Data Quality\",\"children\":[{\"title\":\"Data Quality APR - DK n R\",\"author\":\"RUPAM\",\"description\":\"Template For Data Quality APR - DK n R Report\",\"icon\":\"images/image.ico\",\"template\":\"dataquality_original.html\",\"extension\":\"efw\",\"visible\":\"true\",\"style\":\"clean\",\"name\":\"DataQualityApp.EFW\",\"path\":\"DataQualityReport/New Report/Report Templates - Data Quality/DataQualityApp.EFW\",\"type\":\"file\"}],\"type\":\"folder\"}] }]";
				logger.debug("listOfDirectories = {} ", string);
			} else {
				logger.info("Directory is empty");
			}
		} else {
			logger.info("Directory is not present");
		}

		return string;
	}

	/**
	 * getter method for JSONObject
	 */

	public JSONObject getVisibleExtensions() {
		return visibleExtensions;
	}

	/**
	 * setter method for JSONObject
	 */
	public void setVisibleExtensions(JSONObject visibleExtensions) {
		this.visibleExtensions = visibleExtensions;
	}

	/**
	 * getter method for root path
	 */

	public String getPath() {
		return path;
	}

	/**
	 * setter method for root path
	 */

	public void setPath(String path) {
		this.path = path;
	}
}
