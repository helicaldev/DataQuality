package com.helicaltech.pcni.process;

//import com.helicaltech.pcni.exceptions.ApplicationException;
//import com.helical.efw.resourceloader.JSONProcessor;
import com.helicaltech.pcni.resourcereader.XMLResourceReader;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * An instance of this class is typically used once in the web app starting. The
 * object reads the project.properties and takes part in initializing the
 * singleton class ApplicationProperties
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class BaseLoader {

	private static final Logger logger = LoggerFactory.getLogger(BaseLoader.class);

	private String solutionDirectory = null;

	/**
	 * Constructs an instance of this class
	 *
	 * @param properties
	 *            An instance of ApplicationProperties, which is a singleton
	 */
	public BaseLoader(ApplicationProperties properties) {
		
		this.solutionDirectory = properties.getSolutionDirectory();

	}

	public BaseLoader() {
		// this.settingPath = properties.getSolutionDirectory();
	}

	/**
	 * Loads the resources from the solution directory
	 *
	 * @return A string which represents a json of all the content of the
	 *         solution directory
	 */
	public String loadResources() {
		String resources = null;
		
		// fileValidator.setFile(settingPath);
		// If the setting.xml is present read it
		String loadSolutionDirectory = solutionDirectory;
		if (loadSolutionDirectory != null) {
			try {
				// JSONResourceReader jrd= new JSONResourceReader();
				XMLResourceReader jrd = new XMLResourceReader();
				jrd.setPath(loadSolutionDirectory);
				resources = jrd.getResources();
			} catch (Exception e) {
				logger.error("ConfigurationException occurred", e);
			}
		} else {
			logger.error("Solution Directory is null");
		}

		return resources;
	}

	/**
	 * Returns the json of the tags for which visibility is true in the
	 * setting.xml
	 *
	 *
	 * @return The json of the tags for which visibility is true
	 */
	public JSONObject getJSONOfVisibleExtensionTags(JSONObject jsonOfExtensions) {
		Iterator<?> iterator = jsonOfExtensions.keys();
		JSONObject visibleExtensionsJSON = new JSONObject();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			
			try {
				// Check whether the visible attribute is provided
				// or not. If not, control moves to the catch block as there
				// will be an exception
				if (jsonOfExtensions.getJSONObject(key) != null) {
					JSONObject json = jsonOfExtensions.getJSONObject(key);
					try {
						if ("true".equals(json.getString("@visible"))) {
							logger.debug("The key " + key + " is set to be visible in the dashboard.");
							visibleExtensionsJSON.accumulate(key, jsonOfExtensions.getJSONObject(key));
						}
					} catch (JSONException ex) {
						logger.debug("The key " + key + " is set not to be visible in the dashboard!");
					}
				}
			} catch (JSONException ex) {
				logger.debug(key + " is not a json object.");
			}
		}
		logger.debug("visibleExtensionsJSON = " + visibleExtensionsJSON);
		return visibleExtensionsJSON;
	}
}
