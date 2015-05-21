package com.helicaltech.pcni.scheduling;


import com.helicaltech.pcni.exceptions.ConfigurationException;
import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.resourceloader.TemplateReader;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import com.helicaltech.pcni.utils.ConfigurationFileReader;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * This class is responsible for doing XML related operation.
 *
 * @author Prashansa
 */
public class XmlOperation {
	private static final Logger logger = Logger.getLogger(XmlOperation.class);

	/*
 * 
 */
	public JSON convertXmlStringToJSon(String xmlData) {
		JSON json;
		XMLSerializer xmlSerializer = new XMLSerializer();
		xmlSerializer.setForceTopLevelObject(false);
		json = xmlSerializer.read(xmlData);
		return json;
	}

	/**
	 * <p>
	 * This method is responsible to convert xml into JSONArray.
	 * </p>
	 *
	 * @param path
	 *            a <code>String</code> specify the location of scheduling.xml
	 * @return JSONArray
	 */
	public JSONArray convertXmlStringIntoJSONArray(String path) {
		JSONArray jsonArray = null;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(path));
			String xml = IOUtils.toString(inputStream);
			XMLSerializer xmlSerializer = new XMLSerializer();
			xmlSerializer.setForceTopLevelObject(false);
			xmlSerializer.setTypeHintsCompatibility(false);
			xmlSerializer.setTypeHintsEnabled(false);
			jsonArray = (JSONArray) xmlSerializer.read(xml);
			logger.debug("jsonArray2:  " + jsonArray);
		} catch (JSONException ex) {
			logger.error("JSONException occurred", ex);
			ex.printStackTrace();
		} catch (IOException ex) {
			logger.error("An IOException occurred", ex);
			ex.printStackTrace();
		} finally {
			ApplicationUtilities.closeResource(inputStream);
		}
		return jsonArray;
	}

	public String convertJsonToXml(net.sf.json.JSONArray jsonArray) {

		XMLSerializer serializer = new XMLSerializer();
		serializer.setTypeHintsEnabled(false);
		serializer.setForceTopLevelObject(true);
		Map<String, String> getPropertyFileKey = new HashMap<String, String>();
		ConfigurationFileReader propertiesFileReader = new ConfigurationFileReader();
		getPropertyFileKey = propertiesFileReader.read("project.properties");

		String elementName = getPropertyFileKey.get("elementName");
		String rootName = getPropertyFileKey.get("rootName");
		logger.debug("elementName:  " + elementName + "   rootName   " + rootName);
		serializer.setElementName(elementName);
		serializer.setRootName(rootName);
		String result = serializer.write(jsonArray);
		logger.debug("result" + result);
		return result;
	}

	public String convertJsonToXml(net.sf.json.JSONObject jsonObject) {

		XMLSerializer serializer = new XMLSerializer();
		serializer.setTypeHintsEnabled(false);
		serializer.setForceTopLevelObject(true);
		serializer.setElementName("Schedule");
		serializer.setRootName("Schedules");
		String result = serializer.write(jsonObject);
		logger.debug("result" + result);
		return result;
	}

	/**
	 * find keys of <code>JSONObject</code>
	 *
	 * @param jsonobject
	 * @return List<keys> ,keys a <code>String</code> specify the key of
	 *         JSONObject.
	 */
	public List<String> findKey(net.sf.json.JSONObject jsonobject) {
		logger.debug("KEYS:" + jsonobject.keys());
		Iterator<?> keys = jsonobject.keys();
		List<String> keyy = new ArrayList<String>();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			logger.debug("key==" + key);
			keyy.add(key);
		}
		return keyy;
	}

	public net.sf.json.JSONObject modifyJSONObjectById(net.sf.json.JSONObject existingData, net.sf.json.JSONObject modifiedData) {
		List<String> key = findKey(modifiedData);
		for (int keycount = 0; keycount < key.size(); keycount++) {
			existingData.getJSONObject("Schedules").getJSONObject("Schedule").put(key.get(keycount), modifiedData.get(key.get(keycount)));
		}
		logger.debug("existingData:  " + existingData);
		return existingData;

	}

	public net.sf.json.JSONObject modifyDataById(net.sf.json.JSONObject existingData, net.sf.json.JSONObject modifiedData, int index) {
		List<String> key = findKey(modifiedData);
		boolean searchId = searchId(existingData, modifiedData.getString("@id"));
		if (searchId) {
			for (int keycount = 0; keycount < key.size(); keycount++) {
				existingData.getJSONArray("Schedules").getJSONObject(index).put(key.get(keycount), modifiedData.get(key.get(keycount)));
			}
		} else {
			try {
				throw new ConfigurationException("Job which you are trying to modify is not exist in XML");
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
		logger.debug("existingData:  " + existingData);
		return existingData;

	}

	public net.sf.json.JSONObject deleteJobFromXml(net.sf.json.JSONObject jsonobject, String id) {

		net.sf.json.JSONArray jsonArray = new net.sf.json.JSONArray();
		jsonArray = jsonobject.getJSONArray("Schedules");
		for (int jsonArrayCount = 0; jsonArrayCount < jsonArray.size(); jsonArrayCount++) {
			if (jsonArray.getJSONObject(jsonArrayCount).getString("@id").equals(id)) {
				jsonobject.getJSONArray("Schedules").remove(jsonArrayCount);

			}

		}
		logger.debug("JSON object after deleted ID" + id + ": " + jsonobject);
		return jsonobject;

	}

	public String writeStringIntoFile(String data, String path) {

		FileOutputStream fop = null;
		File file;
		try {

			file = new File(path);
			fop = new FileOutputStream(file);
			if (!file.exists()) {
				file.createNewFile();
			}

			// get the content in bytes
			byte[] contentInBytes = data.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {

					fop.flush();
					fop.close();
					System.gc();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "ok";
	}

	/**
	 * Search id in given JSONObject
	 *
	 * @param json
	 *            a <code>JSONObject</code>
	 * @param id
	 *            a <code>String</code> specify id
	 * @return boolean value ,if id exist in JSONObject then return true else
	 *         return false
	 */
	public boolean searchId(net.sf.json.JSONObject json, String id) {
		boolean result = false;
		StringBuffer sb = new StringBuffer();
		sb.append("\"@id\"");
		sb.append(":");
		sb.append("\"");
		sb.append(id);
		sb.append("\"");
		logger.debug("sb: " + sb);
		if (json.toString().contains(sb.toString().trim())) {
			result = true;
		}
		return result;
	}

	public String createNewFileAndAddJob(net.sf.json.JSONObject newData, String path) {
		net.sf.json.JSONArray jsonArray = new net.sf.json.JSONArray();
		System.out.println("Check ReportURL Contains or not:" + newData.containsKey("ReportURL"));
		if (newData.containsKey("ReportURL")) {
			newData.accumulate("@id", 1);
			jsonArray.add(0, newData);
			String data = convertJsonToXml(jsonArray);
			logger.debug("DATA" + data);
			writeStringIntoFile(data, path);
		} else {
			try {
				throw new ConfigurationException("ReportURL Not found");
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}

		}
		return "filecreated";
	}

	public String addNewJobInExistingXML(net.sf.json.JSONObject newData, String path) {
		logger.debug("Inside addNewJobInExistingXML1");
		logger.debug("newData:" + newData);
		File file = new File(path);
		net.sf.json.JSONObject jsonObject = new net.sf.json.JSONObject();
		String writeData = "";
		int id = 0;
		if (file.exists()) {
			JSONProcessor jsonProcessor = new JSONProcessor();
			jsonObject = jsonProcessor.getJSON(path, true);
			logger.debug("jsonObject:  " + jsonObject);
			boolean validXml = validateXml(jsonObject);
			logger.debug("validXml:  " + validXml);
			id = searchMaxIdInXml(jsonObject);
			logger.debug("id:" + id);
			if (validXml) {
				logger.debug("validXml");
				if (newData.containsKey("ReportURL")) {
					if (jsonObject.get("Schedules") instanceof net.sf.json.JSONArray) {
						logger.debug("Inside jsonArray..");
						newData.accumulate("@id", id + 1);
						logger.debug("newData: " + newData);

						jsonObject.accumulate("Schedules", newData);
						logger.debug("===========" + jsonObject.getJSONArray("Schedules"));
						writeData = convertJsonToXml(jsonObject.getJSONArray("Schedules"));
					} else {
						logger.debug("IT IS JSONOBJECT..++");
						newData.accumulate("@id", id + 1);
						logger.debug("newData: " + newData);

						jsonObject.getJSONObject("Schedules").accumulate("Schedule", newData);
						logger.debug("jsonObject:====+++  " + jsonObject);
						writeData = convertJsonToXml(jsonObject.getJSONObject("Schedules").getJSONArray("Schedule"));

					}
					writeStringIntoFile(writeData, path);
				} else {
					try {
						throw new ConfigurationException("ReportURL Not found");
					} catch (ConfigurationException e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					throw new ConfigurationException("XML is not valid");
				} catch (ConfigurationException e) {

					e.printStackTrace();
				}
			}
		} else {
			try {
				throw new ConfigurationException("XML file you are trying to modify is not available");
			} catch (ConfigurationException e) {

				e.printStackTrace();
			}
		}
		logger.debug("writeData: " + writeData);
		logger.debug("NEW ID " + id + 1);
		return String.valueOf(id + 1);

	}

	public String modifyJobByIdWriteInFile(net.sf.json.JSONObject newData, String path) {
		File file = new File(path);
		net.sf.json.JSONObject jsonObject = new net.sf.json.JSONObject();
		net.sf.json.JSONArray jsonArray = new net.sf.json.JSONArray();
		TemplateReader readFile = new TemplateReader(file);
		String xmlData = readFile.readTemplate();
		String writeData = "";
		logger.debug("xmlData:  " + xmlData);
		JSONProcessor jsonProcessor = new JSONProcessor();
		if (convertXmlStringToJSon(xmlData).isArray()) {
			jsonObject = jsonProcessor.getJSON(path, true);
			jsonArray = jsonObject.getJSONArray("Schedules");
			boolean checkIdAvailableorNot = searchId(jsonObject, newData.getString("@id"));
			logger.debug("checkIdAvailableorNot:" + checkIdAvailableorNot + "===" + newData.getString("@id"));
			if (checkIdAvailableorNot) {
				for (int jsonArraycount = 0; jsonArraycount < jsonArray.size(); jsonArraycount++) {

					if (jsonArray.getJSONObject(jsonArraycount).getString("@id").equals(newData.getString("@id"))) {
						logger.debug("Inside If");

						jsonObject = modifyDataById(jsonObject, newData, jsonArraycount);
					}

				}
				logger.debug("jsonObject:  " + jsonObject);
				writeData = convertJsonToXml(jsonObject.getJSONArray("Schedules"));

				logger.debug("writeData: " + writeData);

				writeStringIntoFile(writeData, path);

			} else {
				try {
					throw new ConfigurationException("Job Which you are trying to modify is not available in XML");
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
			}
		} else {
			logger.debug("JSON object");
			jsonObject = jsonProcessor.getJSON(path, true);
			logger.debug("jsonObject+  " + jsonObject);
			boolean checkIdAvailableorNot = searchId(jsonObject, newData.getString("@id"));
			logger.debug("checkIdAvailableorNot:" + checkIdAvailableorNot + "===" + newData.getString("@id"));
			if (checkIdAvailableorNot) {
				jsonObject = modifyJSONObjectById(jsonObject, newData);
				writeData = convertJsonToXml(jsonObject.getJSONObject("Schedules"));
				writeStringIntoFile(writeData, path);

			} else {
				try {
					throw new ConfigurationException("Job Which you are trying to modify is not available in XML");
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
			}
		}
		return "ok";
	}

	/**
	 * @param jsonobject
	 * @return
	 */
	public int searchMaxIdInXml(net.sf.json.JSONObject jsonobject) {
		net.sf.json.JSONArray jsonArray = new net.sf.json.JSONArray();
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		int maxValue = 0;
		boolean validJSon = validateXml(jsonobject);
		if (jsonobject != null) {
			if (validJSon) {
				if (jsonobject.get("Schedules") instanceof net.sf.json.JSONArray) {
					logger.debug("JSONArray Found");
					jsonArray = jsonobject.getJSONArray("Schedules");

					for (int jsonArrayCount = 0; jsonArrayCount < jsonArray.size(); jsonArrayCount++) {

						String ids = jsonArray.getJSONObject(jsonArrayCount).getString("@id");
						int id = Integer.parseInt(ids);
						logger.debug("id+ " + id);
						arrayList.add(id);
					}
				} else if (jsonobject.getJSONObject("Schedules").get("Schedule") instanceof net.sf.json.JSONObject) {

					logger.debug("JSONObject Found");
					logger.debug("id+:  " + jsonobject.getJSONObject("Schedules").getJSONObject("Schedule").getInt("@id"));
					arrayList.add(jsonobject.getJSONObject("Schedules").getJSONObject("Schedule").getInt("@id"));
				}
				maxValue = Collections.max(arrayList);
				logger.debug("Maximum value: " + maxValue);
			} else {
				try {
					throw new ConfigurationException("Not a valid XML ");
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
			}
		} else {
			try {
				throw new ConfigurationException("JSONObject is Null");
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
		logger.debug("Max ID: " + maxValue);
		return maxValue;
	}

	public boolean validateXml(net.sf.json.JSONObject jsonObject) {
		logger.debug("Inside Validate..");
		boolean tagAvailable = false;

		tagAvailable = jsonObject.has("Schedules") ? true : false;
		// if(tagAvailable){
		// tagAvailable=jsonObject.getJSONObject("Schedules").getJSONObject("SchedulingJob").has("ReportURL")
		// ? true : false;
		// }else{
		// tagAvailable=false;
		// }
		logger.debug("tagAvailable:" + tagAvailable);
		return tagAvailable;

	}

	public boolean validateXmlForJSONArray(net.sf.json.JSONObject jsonObject) {
		logger.debug("Inside Validate..");
		boolean tagAvailable = true;
		tagAvailable = jsonObject.has("Schedules") ? true : false;
		logger.debug("schedulestagAvailable:" + tagAvailable);
		if (tagAvailable) {
			net.sf.json.JSONArray jsonArray = new net.sf.json.JSONArray();
			jsonArray = jsonObject.getJSONArray("Schedules");
			for (int arrayCount = 0; arrayCount < jsonArray.size(); arrayCount++) {
				tagAvailable = jsonArray.getJSONObject(arrayCount).getJSONObject("SchedulingJob").has("ReportURL") ? true : false;
			}
		} else {
			tagAvailable = false;
		}
		logger.debug("tagAvailable" + tagAvailable);
		return tagAvailable;

	}

	public void writeIntoFileUsingRandomAccess(String data, String path) {
		logger.debug("Inside randomaccess file..");
		RandomAccessFile file;
		File file1 = new File(path);
		try {
			file = new RandomAccessFile(file1, "rw");
			file.write(data.getBytes());
			file.close();
			Runtime.getRuntime().gc();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public net.sf.json.JSONObject getParticularObject(String path, String id) {
		net.sf.json.JSONObject jsonObject = new JSONObject();
		net.sf.json.JSONObject jsonObject1 = new JSONObject();
		JSONProcessor jsonProcessor = new JSONProcessor();
		jsonObject = jsonProcessor.getJSON(path, true);
		logger.debug("size" + jsonObject.size());
		logger.debug("====" + jsonObject.getJSONArray("Schedules").size());
		for (int count = 0; count < jsonObject.getJSONArray("Schedules").size(); count++) {
			if (jsonObject.getJSONArray("Schedules").getJSONObject(count).getString("@id").equals(id)) {
				logger.debug("ID available..");
				logger.debug("===++=" + jsonObject.getJSONArray("Schedules").getJSONObject(count));
				jsonObject1 = jsonObject.getJSONArray("Schedules").getJSONObject(count);
			}
		}
		logger.debug("jsonObject:  " + jsonObject1);

		return jsonObject1;

	}

	/**
	 * <p>
	 * Get list of id from xml
	 * </p>
	 *
	 * @param path
	 *            a <code>String</code> specify path of xml
	 * @return List of id
	 * @see EFWController
	 */
	public List<String> getIdFromJson(String path) {
		JSONProcessor jsonProcessor = new JSONProcessor();
		JSONObject jsonObject = new JSONObject();
		jsonObject = jsonProcessor.getJSON(path, true);
		System.out.println("jsonObject:  " + jsonObject);
		File file = new File(path);
		JSONArray jsonArray = new JSONArray();
		List<String> listOfId = new ArrayList<String>();
		if (file.exists()) {
			jsonArray = jsonObject.getJSONArray("Schedules");
			for (int arrayCount = 0; arrayCount < jsonArray.size(); arrayCount++) {
				System.out.println(jsonArray.getJSONObject(arrayCount).containsKey("@id"));
				System.out.println(jsonArray.getJSONObject(arrayCount).getString("@id"));
				listOfId.add(jsonArray.getJSONObject(arrayCount).getString("@id"));
			}
		}
		return listOfId;
	}
}
