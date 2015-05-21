package com.helicaltech.pcni.useractions;

//import com.helicaltech.pcni.resourceloader.JSONProcessor;
//import com.helical.efw.rules.BusinessRulesUtils;
//import com.helicaltech.pcni.rules.JSONUtils;
import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.rules.BusinessRulesUtils;
import com.helicaltech.pcni.rules.JSONUtils;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utility.ApplicationUtilities;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Iterator;
//import java.util.List;
import java.util.List;

/**
 * A new directory creation service is the responsibility of this handler class.
 *
 * @author Rajasekhar
 * @version 1.0
 * @since 1.1
 */
@Component
@Scope("prototype")
public class NewFolderHandler extends AbstractOperationsHandler {
	private static final Logger logger = LoggerFactory.getLogger(NewFolderHandler.class);
	private final ApplicationProperties applicationProperties = ApplicationProperties.getInstance();

	/**
	 * The method validates the given sourceArray and handles the new folder
	 * creation service in the desired locations.
	 *
	 * @param sourceArray
	 *            The request parameter sourceArray
	 * @param folderName
	 *            The new folder that has to be created
	 * @return true if folder is created successfully else will return false
	 */
	public boolean handle(String sourceArray, String folderName) {
		JSONArray sourceJSON;
		boolean anyException = false;
		try {
			sourceJSON = (JSONArray) JSONSerializer.toJSON(sourceArray);
		} catch (JSONException ex) {
			logger.error("JSONException : " + ex);
			return false;
		}
		logger.debug("sourceArray json : " + sourceJSON + ": folderName : " + folderName);
		if ("[]".equals(sourceArray) || sourceArray == null) {
			logger.error("sourceArray parameter has no strings! Operation is aborted.");
			return false;
		}

		Iterator<?> iterator = sourceJSON.iterator();
		/*
		 * Get efwFolder extension
//		 */
		String extension = getExtension();
		while (iterator.hasNext()) {
			String location = (String) iterator.next();
			File directory = new File(applicationProperties.getSolutionDirectory() + File.separator + location);
			anyException = create(folderName, anyException, extension, location, directory);
		}

		logger.debug("anyException status : " + anyException);
		return !anyException;
	}

	/**
	 * <p>
	 * Tries to create a directory with the folderName parameter. The actual
	 * directory name will be current system time. Inside that directory, a file
	 * with name index and with extension efwFolder will be created, in which a
	 * tag will be created with the user specified directory name.
	 * </p>
	 *
	 * @param folderName
	 *            The name of the new folder to be created
	 * @param anyException
	 *            a boolean which represents exception status
	 * @param extension
	 *            The extension of the xml file
	 * @param location
	 *            The location(relative) in which the directory has to be
	 *            created
	 * @param directory
	 *            The directory in which the directory has to be created
	 * @return true if folder is created successfully otherwise returns false
	 */
	private boolean create(String folderName, boolean anyException, String extension, String location, File directory) {
		logger.debug(directory+" Creating Folder at location: "+location);
		if (directory.isFile()) {
			logger.warn("Can't create a directory in a file. Bad Request. Ignoring location : {}", location);
		} else if (directory.isDirectory()) {
			logger.debug(directory+" is directory ");
			if (isIndexFilePresent(directory)) {
				logger.debug("Index File is present at location "+directory);
				if (isUserAuthenticatedToCreateDirectory(directory, extension)) {
					logger.debug("Usr is authenticated foor  "+directory);
					anyException = createDirectory(folderName, anyException, extension, location, directory);
				} else {
					return false;
				}
			} else {
				anyException = createDirectory(folderName, anyException, extension, location, directory);
			}
		} else {
			logger.error("location " + location + " doesn't satisfy a system dependent criteria");
			anyException = true;
		}
		return anyException;
	}

	/**
	 * Creates the directory and the corresponding xml meta data
	 *
	 * @param folderName
	 *            The new folder that has to be created
	 * @param anyException
	 *            a boolean which represents exception status
	 * @param extension
	 *            The extension of the xml file
	 * @param location
	 *            The location(relative) in which the directory has to be
	 *            created
	 * @param directory
	 *            The directory in which the directory has to be created
	 * @return true if the directory is created
	 */
	private boolean createDirectory(String folderName, boolean anyException, String extension, String location, File directory) {
		logger.debug("Trying to create a new directory in the location : {}", location);
		File directoryToBeCreated = new File(directory.toString() + File.separator + System.currentTimeMillis());
		if (directoryToBeCreated.mkdir()) {
			logger.error("New directory is created with system time in {} ", directory.toString());
			if (!createXML(directoryToBeCreated, folderName, extension)) {
				logger.error("Failed to create the xml with extension {}", extension);
				anyException = true;
			}
		} else {
			logger.error("New directory couldn't be created in {} ", directory.toString());
			anyException = true;
		}
		return anyException;
	}

	/**
	 * A user is authorized to create a directory only there is such a file
	 * called index.efwFolder and if his credentials match with his login
	 * credentials.
	 *
	 * @param directory
	 *            The directory under concern
	 * @param extension
	 *            The extension of the xml file
	 * @return true if user authenticated
	 */
	private boolean isUserAuthenticatedToCreateDirectory(File directory, String extension) {
		logger.debug("Checking user credentials before creating directory in {}", directory);
		if (isIndexFilePresent(directory)) {
			List<String> userDetails = BusinessRulesUtils.getUserDetails();
			JSONProcessor processor = new JSONProcessor();
			JSONObject jsonObject = processor.getJSON(directory + File.separator + "index." + extension, false);
			return JSONUtils.verifyUserCredentials(userDetails, jsonObject);
		}
		logger.debug("Credentials did not match while creating directory as there was no matching file with extension " + extension + " inside "
				+ directory);
		return true;
	}

	/**
	 * Returns a template for the index.efwFolder to be written as an xml file
	 *
	 * @param folderName
	 *            The new folder that will be written in the xml
	 * @return <code>JSONObject</code> which contains title, visibility and
	 *         security related information
	 */
	private JSONObject getNewFolderTemplate(String folderName) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.accumulate("title", folderName);
		jsonObject.accumulate("visible", "true");
		jsonObject.accumulate("security", JSONUtils.getSecurityJSONObject());
		return jsonObject;
		//return null;
	}

	/**
	 * Creates the xml file index.efwfolder in the corresponding directory
	 *
	 * @param directory
	 *            Directory where index file has to be created
	 * @param folderName
	 *            The new folder that has to be created
	 * @param extension
	 *            The extension of the xml file
	 * @return true if successfully created
	 */
	private boolean createXML(File directory, String folderName, String extension) {
		JSONObject jsonOfXMLToBeCreated = getNewFolderTemplate(folderName);
		File xmlFile = new File(directory.toString() + File.separator + "index." + extension);
		return ApplicationUtilities.writeReportXML(xmlFile, jsonOfXMLToBeCreated, extension);
	}
}
