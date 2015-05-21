package com.helicaltech.pcni.resourceloader;


import com.helicaltech.pcni.utility.ApplicationUtilities;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Used throughout the application for getting the json of an xml file. Used
 * heavily than any other class.
 *
 * @author Rajasekhar
 * @author Muqtar Ahmed
 * @version 1.1
 * @since 1.0
 */
public class JSONProcessor {

	private static final Logger logger = LoggerFactory.getLogger(JSONProcessor.class);

	/**
	 * Returns the json of a file. The top level key of xml is excluded if the
	 * boolean flag is false.
	 *
	 * @param resource
	 *            The file under concern
	 * @param flag
	 *            true or false for exclusion or inclusion of top level key
	 * @return The json of the resource
	 */
	public JSONObject getJSON(String resource, boolean flag) {
		logger.debug("Preparing JSON for the resource = " + resource);
		JSONObject json = null;
	//	JSONArray js=null;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(resource));
			String xml = IOUtils.toString(inputStream);
			XMLSerializer xmlSerializer = new XMLSerializer();
			if (flag) {
				xmlSerializer.setForceTopLevelObject(true);
			}
			xmlSerializer.setTypeHintsCompatibility(false);
			xmlSerializer.setTypeHintsEnabled(false);
			json = (JSONObject) xmlSerializer.read(xml);
		} catch (JSONException ex) {
			logger.error("JSONException occurred", ex);
			ex.printStackTrace();
		} catch (IOException ex) {
			logger.error("An IOException occurred", ex);
			ex.printStackTrace();
		} finally {
			ApplicationUtilities.closeResource(inputStream);
		}
		return json;
	}

	/**
	 * Returns the json array of a file. The top level key of xml is excluded if
	 * the boolean flag is false.
	 *
	 * @param resource
	 *            The file under concern
	 * @param flag
	 *            true or false for exclusion or inclusion of top level key
	 * @return The json array of the resource
	 */
	public JSONArray getJSONArray(String resource, boolean flag) {
		logger.debug("Preparing JSONArray for the resource = " + resource);
		JSONArray json = null;

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(resource));
			String xml = IOUtils.toString(inputStream);
			XMLSerializer xmlSerializer = new XMLSerializer();
			if (flag) {
				xmlSerializer.setForceTopLevelObject(true);
			}
			xmlSerializer.setTypeHintsCompatibility(false);
			xmlSerializer.setTypeHintsEnabled(false);
			json = (JSONArray) xmlSerializer.read(xml);
		} catch (JSONException ex) {
			logger.error("JSONException occurred", ex);
			ex.printStackTrace();
		} catch (IOException ex) {
			logger.error("An IOException occurred", ex);
			ex.printStackTrace();
		} finally {
			ApplicationUtilities.closeResource(inputStream);
		}
		return json;
	}
}
