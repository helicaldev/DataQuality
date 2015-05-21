package com.helicaltech.pcni.useractions;

import com.helicaltech.pcni.resourceloader.JSONProcessor;
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
import java.util.*;

/**
 * A helper class which handles the rename operation of specific files and
 * folders based on the configuration from the setting.xml.
 * <p/>
 * Renaming is allowed if the user has matching credentials with the resources
 * he is trying to modify.
 *
 * @author Rajasekhar
 * @version 1.0
 * @since 1.0
 */
@Component
@Scope("prototype")
public class RenameOperationHandler extends AbstractOperationsHandler {

	private static final Logger logger = LoggerFactory.getLogger(RenameOperationHandler.class);
	/**
	 * Instance of singleton class <code>ApplicationProperties</code>
	 */
	private final ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
	/**
	 * List of <code>Map</code> objects which contain the original and new name
	 * of the <code>File</code> objects
	 */
	private List<Map<String, String>> listOfMaps;
	/**
	 * List of file extensions for which setting.xml has configuration
	 */
	private List<String> listOfExtensions;

	/**
	 * <p>
	 * Returns true if renaming of all the requested files and folders is
	 * successful. The sourceArray should not be null or empty.
	 * </p>
	 *
	 * @param sourceArray
	 *            The request parameter sourceArray
	 * @return true if the renaming is successful
	 */
	public boolean handle(String sourceArray) {
		logger.debug("sourceArray : " + sourceArray);
		if (sourceArray == null || ("[]".equals(sourceArray) || "[[]]".equals(sourceArray))) {
			return false;
		} else {
			// prepare list of files which are to be renamed.
			this.listOfMaps = new ArrayList<Map<String, String>>();
		}
		return renameAll(listOfMaps, sourceArray);
	}

	/**
	 * Validates the sourceArray and tries to rename. Returns false if the
	 * sourceArray is invalid.
	 *
	 * @param listOfMaps
	 *            A <code>List<Map></code> which contains map(s) of old and new
	 *            names
	 * @param sourceArray
	 *            The request parameter sourceArray
	 * @return false if renaming is not successful
	 */
	private boolean renameAll(List<Map<String, String>> listOfMaps, String sourceArray) {
		if (!isSourceArrayValid(sourceArray)) {
			return false;
		}
		for (Map<String, String> map : listOfMaps) {
			rename(map);
		}
		return true;
	}

	/**
	 * <p>
	 * Gets the original file name and new file name from <code>Map</code> and
	 * check it is file or directory. Based on the whether the <code>File</code>
	 * is a file or directory delegates the call to the appropriate methods.
	 * <p/>
	 * <code>Map</code> will consist of only one key value pair.
	 * </p>
	 *
	 * @param map
	 *            A <code>Map</code> which contains original file name as key
	 *            and new File name as value.
	 */
	private void rename(Map<String, String> map) {
		Set<Map.Entry<String, String>> entrySet = map.entrySet();
		String original = null;
		String newName = null;
		for (Map.Entry<String, String> entry : entrySet) {
			original = entry.getKey();
			newName = entry.getValue();
			/*
			 * Only one pair is expected. So break.
			 */
			break;
		}

		File fileToBeRenamed = new File(applicationProperties.getSolutionDirectory() + File.separator + original);
		logger.debug("Trying to rename : " + fileToBeRenamed);

		if (fileToBeRenamed.isFile()) {
			if (listOfExtensions == null) {
				this.listOfExtensions = super.getListOfExtensionsFromSettings();
			}
			renameFile(fileToBeRenamed, newName);
		} else {
			renameDirectory(fileToBeRenamed, newName);
		}
	}

	/**
	 * Renaming is not allowed if the user is not authorized to rename. The
	 * index.efwFolder file should exist and its security credentials should
	 * match with the currently logged in user credentials.
	 *
	 * @param directoryToBeRenamed
	 *            A directory name which has to be renamed
	 * @param newName
	 *            A <code>String</code> which specifies the new directory name.
	 */
	private void renameDirectory(File directoryToBeRenamed, String newName) {
		if (!isIndexFilePresent(directoryToBeRenamed)) {
			logger.error("Can't rename the directory " + directoryToBeRenamed + " due to insufficient privileges.");
			return;
		}
		if (!areUserCredentialsMatching(directoryToBeRenamed)) {
			logger.error("Can't rename the directory " + directoryToBeRenamed + " due to insufficient privileges.");
			return;
		}
		JSONProcessor processor = new JSONProcessor();
		String extension = getExtension();
		JSONObject jsonObject = processor.getJSON(directoryToBeRenamed + File.separator + "index." + extension, false);
		try {
			if (jsonObject.getString("title") != null) {
				jsonObject.discard("title");
				jsonObject.accumulate("title", newName);
				File indexFile = new File(directoryToBeRenamed + File.separator + "index." + extension);
				if (!ApplicationUtilities.writeReportXML(indexFile, jsonObject, extension)) {
					logger.error("The directory " + directoryToBeRenamed + " couldn't be renamed.");
				}
			}
		} catch (JSONException ex) {
			logger.error("JSONException : ", ex);
		}
	}

	/**
	 * Renames the file fileToBeRenamed with the newName. Renames different
	 * types of xml files like .result files or efwsr files
	 *
	 * @param fileToBeRenamed
	 *            The file to be renamed
	 * @param newName
	 *            The new name of the file
	 */
	private void renameFile(File fileToBeRenamed, String newName) {
		String actualExtension;
		String[] array = (fileToBeRenamed.toString().split("\\.(?=[^\\.]+$)"));
		if (array.length >= 2) {
			actualExtension = array[1];
			if (!listOfExtensions.contains(actualExtension)) {
				logger.error("The file " + fileToBeRenamed + " can't be renamed.");
				return;
			}
			modifyXml(fileToBeRenamed, newName, actualExtension);
		}
	}

	/**
	 * The method modifies different types of xml files like .result files or
	 * efwsr files. The tags of the corresponding xml files will be changed to
	 * reflect the new name.
	 *
	 * @param fileToBeRenamed
	 *            The file to be renamed
	 * @param newName
	 *            The new name of the file
	 * @param actualExtension
	 *            The actual extension of the file to be renamed
	 */
	private void modifyXml(File fileToBeRenamed, String newName, String actualExtension) {
		JSONProcessor processor = new JSONProcessor();
		logger.debug("Old File: "+fileToBeRenamed+" New Name: "+newName+" FileExtension: "+actualExtension);
		JSONObject jsonObject = processor.getJSON(fileToBeRenamed.toString(), false);
		try {
			if (jsonObject.getJSONObject("security") != null) {
				logger.debug("Security Tag Found");
				if (areUserCredentialsMatching(fileToBeRenamed)) {
					/*
					 * Check whether the file type is .result
					 */
					if (jsonObject.has("resultName")) {
						modifyEfwResultFile(fileToBeRenamed, jsonObject, newName);
						return;
					}
					jsonObject.discard("reportName");
					/*
					 * To maintain the structure of xml
					 */
					try {
						String reportParameters = jsonObject.getString("reportParameters");
						jsonObject.discard("reportParameters");
						jsonObject.accumulate("reportParameters", "<![CDATA[" + reportParameters + "]]>");
					} catch (JSONException ex) {
						logger.debug("There is no reportParameters string in the file");
					}

					jsonObject.accumulate("reportName", newName);
					if (!ApplicationUtilities.writeReportXML(fileToBeRenamed, jsonObject, actualExtension)) {
						logger.error("The fileToBeRenamed " + fileToBeRenamed + " couldn't be renamed.");
					}
				}
			}
		} catch (JSONException ex) {
			logger.warn("The file " + fileToBeRenamed + " can't be renamed as it has no security tag!");
		}
	}

	/**
	 * <p>
	 * Modifies the efwResult files to change the name of the file to the new
	 * name
	 * </p>
	 *
	 * @param fileToBeRenamed
	 *            The file to be renamed
	 * @param jsonObject
	 *            A <Code>JSONObject</code> which contains the JSON data file to
	 *            be renamed
	 * @param newName
	 *            A <code>String</code> which specifies new name of file.
	 */
	private void modifyEfwResultFile(File fileToBeRenamed, JSONObject jsonObject, String newName) {
		jsonObject.discard("resultName");

		jsonObject.accumulate("resultName", newName);
		if (!ApplicationUtilities.writeReportXML(fileToBeRenamed, jsonObject, "efwresult")) {
			logger.error("The fileToBeRenamed " + fileToBeRenamed + " couldn't be renamed.");
		}
	}

	/**
	 * A sourceArray is validated only if it is in the form of an array.
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
		logger.debug("json of sourceArray : " + sourceJSON);
		return prepareMap(sourceJSON);
	}

	/**
	 * Converts the input into convenient format for the sake of processing
	 *
	 * @param sourceJSON
	 *            The request parameter sourceArray's json
	 * @return true if the the files in the sourceArray really exist so that
	 *         they can be renamed
	 */
	private boolean prepareMap(JSONArray sourceJSON) {
		Iterator<?> iterator = sourceJSON.iterator();
		try {
			while (iterator.hasNext()) {
				JSONArray jsonArray = (JSONArray) iterator.next();
				logger.debug("Set of original and new Names: jsonArray : " + jsonArray);
				if (!populateListOfMaps(listOfMaps, jsonArray)) {
					return false;
				}
			}
		} catch (JSONException ex) {
			logger.error("sourceArray is not an array of arrays", ex);
			return false;
		}
		logger.debug("List on which rename operations to be performed on " + listOfMaps);
		return true;
	}

	/**
	 * Convert JSONArray into List<Map> for the sake of processing.
	 *
	 * @param listOfMaps
	 *            A <code>List<Map></code> which contains map(s) of old and new
	 *            names
	 * @param jsonArray
	 *            An array of the contents of sourceArray
	 * @return true if the the files in the sourceArray really exist so that
	 *         they can be renamed
	 */
	private boolean populateListOfMaps(List<Map<String, String>> listOfMaps, JSONArray jsonArray) {
		Iterator<?> iterator = jsonArray.iterator();
		int keyCount = 0;
		String key = null;
		String value = null;
		try {
			while (iterator.hasNext()) {
				if (keyCount == 0) {
					key = (String) iterator.next();
					logger.debug("key : " + key);
				}
				if (keyCount == 1) {
					value = (String) iterator.next();
					logger.debug("value : " + value);
					break;
				}
				keyCount++;
			}
		} catch (JSONException ex) {
			logger.error("sourceArray is not an array of arrays", ex);
			return false;
		}
		Map<String, String> map = new HashMap<String, String>();
		if (!doesFileExists(key)) {
			logger.error("The key " + key + " indicates an invalid location on file system. Operation aborted.");
			return false;
		}
		map.put(key, value);
		logger.debug("populated a map : " + map);
		listOfMaps.add(map);
		return true;
	}

	/**
	 * Checks whether the given file exists or not in the solution directory
	 *
	 * @param file
	 *            a file name
	 * @return true if file exists. Otherwise false
	 */
	private boolean doesFileExists(String file) {
		return new File(this.applicationProperties.getSolutionDirectory() + File.separator + file).exists();
	}
}
