package com.helicaltech.pcni.useractions;

//import com.helicaltech.pcni.exceptions.ImproperXMLConfigurationException;
//import com.helicaltech.pcni.process.BaseLoader;
//import com.helicaltech.pcni.resourceloader.JSONProcessor;
//import com.helicaltech.pcni.rules.BusinessRulesUtils;
//import com.helical.efw.rules.BusinessRulesUtils;
//import com.helical.efw.rules.JSONUtils;
//import com.helicaltech.pcni.singleton.ApplicationProperties;
//import net.sf.json.JSONObject;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.rules.BusinessRulesUtils;
import com.helicaltech.pcni.rules.JSONUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class which holds the methods for use by the file operations module.
 * <p/>
 * Created by author on 11-Oct-14.
 *
 * @author Rajasekhar
 * @version 1.0
 * @since 1.1
 */
public abstract class AbstractOperationsHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractOperationsHandler.class);

	/**
	 * <p>
	 * Gets efwFolder key value from setting.xml. If it is not present in
	 * setting.xml, it will throw exception else return value of efwFolder node.
	 * </p>
	 *
	 * @return a <code>String</code> which specifies value of efwFolder node in
	 *         setting.xml
	 */
	public String getExtension() {
//		ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
//		JSONProcessor processor = new JSONProcessor();
//		JSONObject json = processor.getJSON(applicationProperties.getSettingPath(), false);
//
//		String extension = null;
//		try {
//			JSONObject extensionsJSONObject = json.getJSONObject("Extentions").getJSONObject("folder").getJSONObject("efwFolder");
//			if (extensionsJSONObject == null) {
//				throw new ImproperXMLConfigurationException("Setting.xml configuration is incorrect");
//			}
//			extension = extensionsJSONObject.getString("#text");
//			logger.debug("efwFolder key's value = " + extension);
//		} catch (JSONException ex) {
//			logger.error("Exception while retrieving efwFolder key's value", ex);
//		} catch (ImproperXMLConfigurationException e) {
//			logger.error("ImproperXMLConfigurationException", e);
//		}
		return "efwfolder";
	}

	/**
	 * Checks whether the currently logged in user credentials are matching with
	 * the ones present in the fileUnderConcern file parameter
	 *
	 * @param fileUnderConcern
	 *            A <code>File</code> object
	 * @return true if it is valid user else return false.
	 */
	public boolean areUserCredentialsMatching(File fileUnderConcern) {
		List<String> userDetails = BusinessRulesUtils.getUserDetails();
		JSONProcessor processor = new JSONProcessor();
		JSONObject jsonObject = null;
		if (fileUnderConcern.isFile()) {
			jsonObject = processor.getJSON(fileUnderConcern.toString(), false);
		} else {
			jsonObject = processor.getJSON(fileUnderConcern.toString() + File.separator + "index." + getExtension(), false);
		}
		return JSONUtils.verifyUserCredentials(userDetails, jsonObject);
		
	}

	/**
	 * Checks whether index.efwFolder exists or not in the specified directory
	 *
	 * @param fileUnderConcern
	 *            A <code>File</code> object
	 * @return true if index.efwFolder exist else return false.
	 */
	public boolean isIndexFilePresent(File fileUnderConcern) {
		return new File(fileUnderConcern.toString() + File.separator + "index." + getExtension()).exists();
	//	return true;
	}

	/**
	 * <p>
	 * Returns the list of extensions for which setting xml has configuration.
	 * For example efw, efwsr, efwFav, efwFolder etc.
	 * </p>
	 *
	 * @return List of rule attribute.
	 */
	public List<String> getListOfExtensionsFromSettings() {
	//	JSONProcessor processor = new JSONProcessor();
		//ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
		//JSONObject xmlContent = processor.getJSON("settingpath", false);
	//	BaseLoader baseLoader = new BaseLoader(applicationProperties);
	//	JSONObject jsonObject = null;
//		try {
//			jsonObject = baseLoader.getJSONOfVisibleExtensionTags(xmlContent.getJSONObject("Extentions"));
//		} catch (JSONException ex) {
//			logger.error("JSONException ", ex);
//		}
//
//		Iterator<?> iterator = null;
//		if (jsonObject != null) {
//			iterator = jsonObject.keys();
//		}
//		List<String> listOfExtensions = new ArrayList<String>();
//		if (iterator != null) {
//			while (iterator.hasNext()) {
//				String key = (String) iterator.next();
//				try {
//					if (jsonObject.getJSONObject(key).getString("@rule") != null) {
//						listOfExtensions.add(jsonObject.getJSONObject(key).getString("#text"));
//					}
//				} catch (JSONException ex) {
//					logger.info("No rule or text value for the key " + key + " is provided.");
//				}
//			}
//		}
		
		List<String> listOfExtensions = new ArrayList<String>();
		listOfExtensions.add("html");
		listOfExtensions.add("rdf");
		listOfExtensions.add("fav");
	//	listOfExtensions.add("efwfolder");
		listOfExtensions.add("result");
		
		
		logger.debug("The list of extensions for which setting xml has configuration : " + listOfExtensions);
		return listOfExtensions;
	}
}
