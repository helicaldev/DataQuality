package com.helicaltech.pcni.useractions;

//import com.helicaltech.pcni.process.BaseLoader;
//import com.helicaltech.pcni.resourceloader.JSONProcessor;
//import com.helicaltech.pcni.singleton.ApplicationProperties;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains utility method related to IO operations. Consists of method that
 * read the configuration settings for different file operations from
 * setting.xml.
 * <p/>
 * Created by author on 16-10-2014.
 *
 * @author Rajasekhar
 * @version 1.0
 * @since 1.1
 */
public class IOOperationsUtility {
	private static final Logger logger = LoggerFactory.getLogger(IOOperationsUtility.class);

	/**
	 * Simply deletes a file with appropriate logs.
	 *
	 * @param file
	 *            a <code>File</code> which has to be deleted.
	 */
	public static void deleteWithLogs(File file) {
		if (file.delete()) {
			logger.debug("Successfully deleted the file " + file);
		} else {
			logger.debug("Couldn't delete the file " + file);
		}
	}

	/**
	 * Simply deletes a directory with appropriate logs.
	 *
	 * @param directory
	 *            Name of the directory which has to be deleted.
	 */
	public static void deleteEmptyDirectoryWithLogs(File directory) {
		if (directory.delete()) {
			logger.info(directory + " is empty. No conditions for deletion. Deleted");
		} else {
			logger.info(directory + " couldn't be deleted");
		}
	}

	/**
	 * <p>
	 * Converts setting.xml operations node into
	 * <code>Map<String, Map<String, String>></code>
	 * </p>
	 *
	 * @return A <code>Map<String, Map<String, String>></code> of configuration
	 *         settings
	 */
	public Map<String, Map<String, String>> getMapOfOperationSettings() {
//		JSONProcessor processor = new JSONProcessor();
//		ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
		JSONObject jsonObject = new JSONObject();
		try {
//			jsonObject.accumulate("html", "html");
//			jsonObject.accumulate("SR", "rdf");
//			jsonObject.accumulate("efwfav", "efwfav");
//			jsonObject.accumulate("efwresult", "efwresult");
			
			jsonObject.accumulate("html","{}");
			jsonObject.accumulate("SR","{}");
			jsonObject.accumulate("efwvf","{}");
			jsonObject.accumulate("efwfav","{}");
			jsonObject.accumulate("efwFolder","{}");
//			jsonObject.accumulate("crt","efwExport");
			jsonObject.accumulate("efwresult","{}");
		} catch (JSONException ex) {
			logger.error("JSONException ", ex);
		}

		logger.debug("operations : " + jsonObject);

		Iterator<?> iterator = null;
		if (jsonObject != null) {
			iterator = jsonObject.keys();
		}
		
		Map<String, Map<String, String>> mapOfKeys = new HashMap<String, Map<String, String>>();

		if (iterator != null) {
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				try {
					JSONObject json = jsonObject.getJSONObject(key);
					if (json != null) {
//						mapOfKeys.put(key, getMapForKey(key, json));
						mapOfKeys.put(key, getMapForKey(key));
						
					}
				} catch (JSONException ex) {
					logger.error(key + " is not a JSONObject");
				}
			}
		}
		logger.debug("Returned Map: "+mapOfKeys);
		return mapOfKeys;
	}

	/**
	 * The configuration related settings for the file operations such as
	 * import, export, rename, delete etc are read from setting.xml and a map of
	 * such configuration will be prepared.
	 *
	 * @param key
	 *            Either efwsr or efwFavourite or efwResult etc.
	 * @param json
	 *            The json of setting.xml operations tag
	 * @return <code>Map<String, String></code> which contains key - any file
	 *         operation and value - corresponding class name
	 */
//	private Map<String, String> getMapForKey(String key, JSONObject json) {
	private Map<String, String> getMapForKey(String key) {
		logger.debug("Retrieving info for key: "+key);
		String clazz=null;
		String importClazz=null;
		if(key.equalsIgnoreCase("SR"))
		{
			clazz="com.helicaltech.pcni.rules.io.EFWSRDeleteRule";
			importClazz="com.helicaltech.pcni.rules.io.EFWSRImportRule";
		}
		if(key.equalsIgnoreCase("efwfav"))
		{
			clazz="com.helicaltech.pcni.rules.io.EFWFavouriteDeleteRule";
		}
		if(key.equalsIgnoreCase("efwresult"))
		{
			clazz="com.helicaltech.pcni.rules.io.EFWSavedResultDeleteRule";
		}
		if(key.equalsIgnoreCase("efwFolder"))
		{
			clazz="com.helicaltech.pcni.rules.EFWFolderRuleValidator";
		}
		
		Map<String, String> mapForKey = new HashMap<String, String>();
		
		try {
			mapForKey.put("delete",clazz);
		} catch (JSONException ex) {
			logger.info("delete tag is not provided or class attribute is not provided for the key " + key + ". Using null");
			mapForKey.put("delete", null);
		}
		
		try {
			mapForKey.put("import", importClazz);
		} catch (JSONException ex) {
			logger.info("import tag is not provided or class attribute is not provided for the key " + key + ". Using null");
			mapForKey.put("import", null);
		}
		
		return mapForKey;
	}

	/**
	 * Obtains the file types key and value as a <code>Map</code> from the
	 * setting.xml (Only the nodes for which visible attribute value true are
	 * considered).
	 *
	 * @return A <code>Map</code> of key value pairs
	 */
	public Map<String, String> getVisibleExtensionsKeyValuePairs() {
//		JSONProcessor processor = new JSONProcessor();
//		ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
//		JSONObject xmlContent = processor.getJSON(applicationProperties.getSettingPath(), false);
//		BaseLoader baseLoader = new BaseLoader(applicationProperties);
//		JSONObject jsonObject = null;
//		try {
//			jsonObject = baseLoader.getJSONOfVisibleExtensionTags(xmlContent.getJSONObject("Extentions"));
//		} catch (JSONException ex) {
//			logger.error("JSONException ", ex);
//		}
		
//		JSONObject jsonObject = new JSONObject();
//		try{
//			jsonObject.accumulate("html", "html");
//			jsonObject.accumulate("SR", "rdf");
//			jsonObject.accumulate("efwfav", "fav");
//			jsonObject.accumulate("efwresult", "efwresult");
//			jsonObject.accumulate("efwresult", "efwresult");
//			jsonObject.accumulate("efwresult", "efwresult");
//			
//			jsonObject.accumulate("html","html");
//			jsonObject.accumulate("rdf","rdf");
//			jsonObject.accumulate("EFWVF","efwvf");
//			jsonObject.accumulate("fav","efwfav");
//			jsonObject.accumulate("efwfolder","efwFolder");
//			jsonObject.accumulate("crt","efwExport");
//			jsonObject.accumulate("efwresult","efwresult");
//		}
//		catch (JSONException ex) {
//			logger.error("JSONException ", ex);
//		}
		return populateMap();
	}

	/**
	 * Iterates over the given jsonObject and prepares a key value paris map
	 *
	 * @param jsonObject
	 *            The json of the extensions tag from setting.xml
	 * @return A <code>Map<String, String></code> which is created from
	 *         JSONObject.
	 */
	private Map<String, String> populateMap() {
		Map<String, String> map = new HashMap<String, String>();
//		Iterator<?> iterator = null;
//		if (jsonObject != null) {
//			iterator = jsonObject.keys();
//		}
//		
//		if (iterator != null) {
//			while (iterator.hasNext()) {
//				String key = (String) iterator.next();
//				logger.debug("Iterating key: "+key);
//				try {
					map.put("html","html");
					map.put("rdf","SR");
					map.put("fav","efwfav");
					map.put("efwfolder","efwFolder");
					map.put("crt","efwExport");
					map.put("result","efwresult");
//				} catch (JSONException ex) {
//					logger.info("No rule or text value for the key " + key + " is provided.");
//				}
//			}
//		}
		return map;
	}
}
