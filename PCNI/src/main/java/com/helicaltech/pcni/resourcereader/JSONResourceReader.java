package com.helicaltech.pcni.resourcereader;

import com.helicaltech.pcni.exceptions.ConfigurationException;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import com.helicaltech.pcni.validator.DirectoryValidator;
import com.helicaltech.pcni.resourceloader.DirectoryLoader;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * this class implements the IRsource interface and overload the getResources
 * method, the responsibility of this class convert the directories which is
 * read from root folder to JSONArray String return the string
 *
 * @author muqtar ahmed
 * @version 1.1
 * @since 1.0
 */
public class JSONResourceReader{

	private static final Logger logger = LoggerFactory.getLogger(JSONResourceReader.class);

	/**
	 * root path
	 */
	private String path;

	/**
	 * JSONObject reference
	 */

	private JSONObject visibleExtensions;

	
	
	
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
	 * overloaded method get the directories and convert into JSONArray and
	 * return the string
	 */

	public String getResources() throws ConfigurationException {
		logger.info(this.getClass().getName());

		String jsonObj = "Dirlist here";

		DirectoryValidator dValidator = new DirectoryValidator();
		dValidator.setDirectory(path);
		if (dValidator.isDirectoryPresent()) {
			if (!dValidator.isDirectoryEmpty()) {
				DirectoryLoader dirLoader = new DirectoryLoader();
				List<Map<String, String>> dirList = dirLoader.getSolutionDirectory(getPath());
				logger.debug("Directory List: "+dirList);
				jsonObj = ApplicationUtilities.getJSONArray(dirList);
			} else {
				logger.debug("Directory is empty");
			}
		} else {
			logger.debug("Directory is not present");
			logger.info("Directory is not present");

		}

		return jsonObj;
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
