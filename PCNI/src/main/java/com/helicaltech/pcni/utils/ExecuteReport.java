package com.helicaltech.pcni.utils;


import com.helicaltech.pcni.resourceloader.TemplateReader;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A template file referenced by the EFW file may have parameters of varying
 * value. They will be replaced by the corresponding values from the JSON
 * parametersJSON.
 *
 * @author Rajasekhar
 * @version 1.0
 * @since 1.1
 */
public class ExecuteReport {

	private static final Logger logger = LoggerFactory.getLogger(ExecuteReport.class);

	/**
	 * <p>
	 * The variable components in the template file will be replaced from the
	 * parametersJSON appropriately.
	 * </p>
	 *
	 * @param dirPath
	 *            directory of the EFW file
	 * @param efwFile
	 *            The name of the EFW file
	 * @param parametersJSON
	 *            The json of the parameters
	 * @return The list of html file content in the first index and dirPath in
	 *         the second index
	 */
	public List<String> execute(String dirPath, String efwFile, JSONObject parametersJSON) {

		ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
		String solutionDirectory = applicationProperties.getSolutionDirectory();
	//	JSONProcessor jsonProcessor = new JSONProcessor();
		String templateData = null;
		String template;
		//JSONObject efwFileJsonObject;

		//efwFileJsonObject = jsonProcessor.getJSON(solutionDirectory + File.separator + dirPath + File.separator + efwFile, false);
		//logger.debug("jsonObject = " + efwFileJsonObject);
		template = efwFile;
		if (template.isEmpty() || template.length() == 0) {
			logger.error("EFW file has no template element. HTML file not found.");
		} else {
			String templateFile;
			if (template.contains("solution:")) {
				templateFile = template.replaceFirst("solution:", solutionDirectory + File.separator);
			} else {
//				if(template.contains(solutionDirectory))
//				{
					
					templateFile = solutionDirectory + File.separator + dirPath + File.separator + template;
//				}
//				else
//				{
//					templateFile = dirPath + File.separator + template;
//				}
			}
			logger.debug("Reading template : "+templateFile);
			TemplateReader templateReader = new TemplateReader(new File(templateFile));
			templateData = templateReader.readTemplate();
		}
		templateData = replaceParameters(templateData, parametersJSON);
		List<String> list = new ArrayList<String>();
		list.add(0, templateData);
		list.add(1, dirPath);
		return list;
	}

	/**
	 * <p>
	 * The parameters in the templateData will be replaced with the
	 * corresponding values from the parametersJSON
	 * </p>
	 *
	 * @param templateData
	 *            The content of the template file as string
	 * @param parametersJSON
	 *            The json of the parameters
	 * @return Updated template html file as string
	 */
	protected String replaceParameters(String templateData, JSONObject parametersJSON) {
		Iterator<?> keys = parametersJSON.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (templateData.contains("${" + key + "}")) {
				try {
					JSONArray array = parametersJSON.getJSONArray(key);
					String value = array.toString();
					logger.debug("value = " + value);
					value = value.replace("[", "").replace("]", "").replace("\"", "'");
					logger.debug("The value for key " + key + "is " + value);
					templateData = templateData.replace("${" + key + "}", value);
				} catch (JSONException ex) {
					logger.debug("key " + key + "is not an array" + ". Key value = " + parametersJSON.getString(key));
					templateData = templateData.replace("${" + key + "}", parametersJSON.getString(key));
				}
			}
		}
		return templateData;
	}
}
