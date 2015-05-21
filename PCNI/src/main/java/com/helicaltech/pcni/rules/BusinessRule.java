package com.helicaltech.pcni.rules;


//import com.helicaltech.pcni.resourceloader.SettingsLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is created for the purpose of code reuse. The file tree in the HDI
 * left panel (At least as of version 1.1) shows a set of folder files. Some
 * need to be shown to the user and some shouldn't be. For such purposes and for
 * the sake of the data that is being shown to the user this business rules
 * related classes are designed.
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class BusinessRule {

	//private static final Logger logger = LoggerFactory.getLogger(BusinessRule.class);

	/**
	 * The file content will be put in a map and added to the json being sent to
	 * the view
	 *
	 * @param file
	 *            The file under concern to be included in the json
	 * @param extensionKey
	 *            The file extension key in setting.xml
	 * @return The map of the file content
	 */
	public Map<String, String> includeMap(File file, String extensionKey) {
		Map<String, String> foldersMap = new HashMap<String, String>();
//		JSONProcessor processor = new JSONProcessor();
//		logger.debug("Absolute path is {} ", file.getAbsolutePath());
//		SettingsLoader settingsLoader = new SettingsLoader();
//		JSONObject visibleExtensionXMLJSONObject = processor.getJSON(file.getAbsolutePath(), false);
//		String visible = visibleExtensionXMLJSONObject.getString("visible");
//		if ("TRUE".equalsIgnoreCase(visible)) {
//			String relativePath = settingsLoader.getRelativeSolutionPath(file.getAbsolutePath());
//			foldersMap.put("type", "file");
//			foldersMap.put("extension", extensionKey);
//			foldersMap.put("name", file.getName());
//			foldersMap.put("path", relativePath);
//			/*
//			 * Get the contents of the visible file into the map
//			 */
//			Iterator<?> keys = visibleExtensionXMLJSONObject.keys();
//			while (keys.hasNext()) {
//				String key = (String) keys.next();
//				if (key.equals("reportName")) {
//					foldersMap.put("title", visibleExtensionXMLJSONObject.getString(key));
//					foldersMap.put("description", file.getName());
//					foldersMap.put("options", new JSONObject().accumulate("selectable", "true").toString());
//					continue;
//				}
//				foldersMap.put(key.toLowerCase(), visibleExtensionXMLJSONObject.getString(key));
//			}
//		}
		/*
		 * If the if condition fails the file is of type other than that are
		 * visible in the setting.xml
		 */
		return foldersMap;
	}
}
