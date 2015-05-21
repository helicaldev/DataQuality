package com.helicaltech.pcni.useractions;


import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.scheduling.ScheduleProcess;
import com.helicaltech.pcni.scheduling.XmlOperationWithParser;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utils.ConfigurationFileReader;
import com.helicaltech.pcni.useractions.DeleteOperationUtility;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
//import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

/**
 * A handler class for the delete file operation. File and folder deletion in
 * the EFW solution directory is handled by this class.
 *
 * @author Rajasekhar
 * @author Prashansa
 * @version 1.0
 * @since 1.1
 */
@Component
@Scope("prototype")
public class DeleteOperationHandler extends AbstractOperationsHandler {

	private static final Logger logger = LoggerFactory.getLogger(DeleteOperationHandler.class);
	/**
	 * Instance of the singleton <code>ApplicationProperties</code>
	 */
	private final ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
	/**
	 * The list of extensions for which the setting.xml has configuration
	 */
	private List<String> extensionsList;
	/**
	 * List from the sourceArray
	 */
	private List<File> listOfFilesToBeDeleted;
	/**
	 * List from the sourceArray
	 */
	private List<File> listOfDirectoriesToBeDeleted;

	public boolean handle(String sourceArray) {
		/*
		 * First validate the source array
		 */
		return (isSourceArrayValid(sourceArray) && deleteAll());
	}

	/**
	 * <p>
	 * Deletes all the files and folders in the specified sourceArray if the
	 * user is authorized
	 * </p>
	 *
	 * @return true if the files and directories are deleted otherwise false
	 */
	private boolean deleteAll() {
		/*
		 * Pick one file from the input, See if user can delete
		 */
		if (canDeleteDirectories() && canDeleteFiles()) {
			logger.debug("Checking condition can delete or not");
			purge();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <p>
	 * If the efwsr files have schedulingReference then before deleting the file
	 * the scheduling information is also deleted from the scheduler. All the
	 * files in listOfFilesToBeDeleted will be deleted.
	 * </p>
	 */
	private void purge() {
		logger.debug("Inside purge");
		DeleteOperationUtility deleteOperationUtility = new DeleteOperationUtility();
		logger.debug("Extension List: "+extensionsList);
		deleteOperationUtility.setListOfFileExtensions(this.extensionsList);
		logger.info("List of files and directories to be deleted " + listOfFilesToBeDeleted + " and " + listOfDirectoriesToBeDeleted);
		for (File file : listOfFilesToBeDeleted) {
			logger.debug("Checking file List in purge "+file);
			String path = file.getPath();
			logger.debug("PATH :  " + path);
			Map<String, String> getpropertiesFileValue = new HashMap<String, String>();
			ConfigurationFileReader propertiesFileReader = new ConfigurationFileReader();
			getpropertiesFileValue = propertiesFileReader.read("project.properties");
			String SchedulerPath = getpropertiesFileValue.get("schedularPath");
			JSONProcessor jsonProcessor = new JSONProcessor();
			JSONObject jsonObject = new JSONObject();
			jsonObject = jsonProcessor.getJSON(path, true);
			logger.debug("RDF JSON DATA" + jsonObject);
			String scheduleReference = "";
			if (jsonObject.getJSONObject("efwsr").containsKey("schedulingReference")) {
				logger.debug("rdf contains schedulingReference");
				scheduleReference = jsonObject.getJSONObject("efwsr").getString("schedulingReference");
				ScheduleProcess scheduleProcess = new ScheduleProcess();
				scheduleProcess.delete(scheduleReference);
				XmlOperationWithParser xmlOperationWithParser = new XmlOperationWithParser();
				xmlOperationWithParser.removeElementFromXml(SchedulerPath, scheduleReference);
			}
			logger.debug("scheduleReference:  " + scheduleReference);
			logger.debug("Trying to delete the file " + file);
			deleteOperationUtility.deleteFile(file);
		}
		for (File directory : listOfDirectoriesToBeDeleted) {
			logger.debug("Trying to delete the file " + directory);
			deleteOperationUtility.deleteDirectory(directory);
		}
	}

	/**
	 * Checks whether if all the files can be deleted or not for the sake of
	 * atomicity.
	 *
	 * @return true if all the files can be deleted
	 */
	private boolean canDeleteFiles() {
		/*
		 * Return true if there are no files to be deleted
		 */
		logger.debug("Inside candeletfiles");
		if (listOfFilesToBeDeleted.isEmpty()) {
			logger.debug("List of file is empty");
			return true;
		}
		for (File file : listOfFilesToBeDeleted) {
			if (!areUserCredentialsMatching(file)) {
				return false;
			} else {
				logger.info(file + " is deletable");
			}
		}
		return true;
	}

	/**
	 * Checks whether if all the directories can be deleted or not for the sake
	 * of atomicity.
	 *
	 * @return true if all the directories can be deleted
	 */
	private boolean canDeleteDirectories() {
		DeleteOperationUtility deleteOperationUtility = new DeleteOperationUtility();
		deleteOperationUtility.setListOfFileExtensions(extensionsList);
		logger.debug("Can Delete Direcory Operation: "+extensionsList);
		if (listOfDirectoriesToBeDeleted.isEmpty()) {
			logger.debug("Can Delete Direcory Operation: Dirextory is empty");
			return true;
		}
		logger.debug("Can Delete Direcory Operation: Dirextory is Not empty");
		for (File directory : listOfDirectoriesToBeDeleted) {
			logger.debug("Can Delete Direcory Operation: Dirextory is Not empty "+directory);
			if (!deleteOperationUtility.tryDeleting(directory)) {
				logger.debug("Can Delete Direcory Operation: Dirextory is Not empty. Can't delete this ");
				return false;
			} else {
				logger.debug("Can Delete Direcory Operation: Dirextory is Not empty and it is not deletable!! returning");
				logger.info(directory + " is deletable");
			}
		}
		return true;
	}

	/**
	 * Validates the request parameter sourceArray
	 *
	 * @param sourceArray
	 *            The request parameter sourceArray
	 * @return true if validated
	 */
	private boolean isSourceArrayValid(String sourceArray) {
		JSONArray sourceJSON;
		try {
			sourceJSON = (JSONArray) JSONSerializer.toJSON(sourceArray);
		} catch (JSONException ex) {
			logger.error("JSONException : " + ex);
			return false;
		}
		if (!prepareLists(sourceJSON)) {
			return false;
		}
		extensionsList = getListOfExtensionsFromSettings();
		return (extensionsList != null);
	}

	/**
	 * <p>
	 * Prepares <code>List</code> of files and directories to be deleted.
	 * </p>
	 *
	 * @param sourceJSON
	 *            The json array of sourceArray request parameter
	 * @return false if the files does not exist on the file system
	 */
	private boolean prepareLists(JSONArray sourceJSON) {
		this.listOfFilesToBeDeleted = new ArrayList<File>();
		this.listOfDirectoriesToBeDeleted = new ArrayList<File>();
		Iterator<?> iterator = sourceJSON.iterator();
		try {
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				File file = new File(this.applicationProperties.getSolutionDirectory() + File.separator + key);
				if (!file.exists()) {
					logger.error("The key " + key + " indicates an invalid location on file system. Operation aborted.");
					return false;
				}
				if (file.isFile()) {
					listOfFilesToBeDeleted.add(file);
				} else if (file.isDirectory()) {
					listOfDirectoriesToBeDeleted.add(file);
				} else {
					logger.error("The key " + key + " is neither a directory nor file. Check properties. Operation aborted.");
					return false;
				}
			}
		} catch (JSONException ex) {
			logger.error("sourceArray is not an array. Aborting the operation.", ex);
			return false;
		}
		return true;
	}
}
