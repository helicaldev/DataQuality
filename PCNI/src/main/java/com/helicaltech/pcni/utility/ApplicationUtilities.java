package com.helicaltech.pcni.utility;

import net.sf.json.*;
import net.sf.json.xml.XMLSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consists of a set of Utility methods used throughout the code base
 *
 * @author Rajasekhar
 * @version 1.1
 * @since 1.0
 */
public class ApplicationUtilities {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationUtilities.class);

	/**
	 * <p>
	 * This method creates net.sf.Json.JSONArray using List of map
	 * </p>
	 *
	 * @param listOfMaps
	 *            a list of map objects
	 * @return a <code>String</code> which is a JSONArray.
	 */
	public static String getJSONArray(List<Map<String, String>> listOfMaps) {
		JSONArray jsonArray = new JSONArray();
		for (Map<String, String> map : listOfMaps) {
			JSONObject jsonObject = new JSONObject();
			for (Map.Entry<String, String> entry : map.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				try {
					jsonObject.put(key, value);
				} catch (JSONException e) {
					logger.error("JSONException", e);
				}
			}
			jsonArray.add(jsonObject);
		}
		return jsonArray.toString();
	}

	
	
		/**
	 * <p>
	 * Checks whether the class in question exists in the class path.
	 * </p>
	 *
	 * @param className
	 *            a <code>String</code> which specifies class name.
	 * @return true if given String is class else return false.
	 */
	public static boolean isClass(String className) {
		boolean exists = true;
		try {
			Class.forName(className);
		} catch (ClassNotFoundException e) {
			exists = false;
			logger.error("ClassNotFoundException occurred", e);
		}
		return exists;
	}

	/**
	 * Closes a closeable resource
	 *
	 * @param resource
	 *            a {@link Closeable} interface
	 */
	public static void closeResource(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException exception) {
				logger.error("IOException " + exception);
			}
		}
	}

	/**
	 * <p/>
	 * Creates a directory if it does not exists
	 *
	 * @param directory
	 *            a <code>File</code> which specify directory name.
	 */
	public static void createDirectory(File directory) {
		if (!directory.exists()) {
			logger.debug("directory.exists() = " + directory.exists());
			if (directory.mkdir()) {
				logger.info(directory + "directory in System folder doesn't exist. Creating " + directory + "directory.");
			}
		}
	}

	/**
	 * <p>
	 * This method is responsible to create xml file using
	 * <code>TransformerFactory</code> if it does not exists. If xml file exists
	 * then it overwrites the existing file.
	 * </p>
	 *
	 * @param file
	 *            a <code>File</code> object which specifies file name.
	 * @param xmlSource
	 *            a <code>String</code> which specifies xml data.
	 * @return true if xml file is completely written otherwise false
	 */
	private static boolean stringToDom(File file, String xmlSource) {
		// Parse the given input
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(xmlSource)));

			// Write the parsed document to an xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");

			DOMSource source = new DOMSource(document);
			if (!file.exists()) {
				file.createNewFile();
			}
			StreamResult result = new StreamResult(file.toURI().getPath());
			logger.debug("path = " + file.toURI().getPath());
			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			logger.error("TransformerConfigurationException", e);
			return false;
		} catch (ParserConfigurationException e) {
			logger.error("ParserConfigurationException", e);
			return false;
		} catch (SAXException e) {
			logger.error("SAXException", e);
			return false;
		} catch (IOException e) {
			logger.error("IOException", e);
			return false;
		} catch (TransformerException e) {
			logger.error("TransformerException", e);
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * Creates a <code>File</code> with specified data
	 * </p>
	 *
	 * @param location
	 *            a <code>File</code> which specifies file location.
	 * @param data
	 *            a <code>String</code> which specifies data which has to be
	 *            written in file.
	 * @return true if data is successfully written into file otherwise false
	 */
	public static boolean createAFile(File location, String data) {
		String encoding = getEncoding();
		if (!location.exists()) {
			try {
				location.createNewFile();
			} catch (IOException e) {
				logger.debug("IOException", e);
			}
		}

		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(location);
			fileOutputStream.write(data.getBytes(encoding));
			fileOutputStream.flush();
		} catch (IOException e) {
			logger.error("IOException occurred", e);
			// In case of any anomaly, return false
			return false;
		} finally {
			closeResource(fileOutputStream);
		}
		return true;
	}

	/**
	 * Get the size of folder
	 *
	 * @param directory
	 *            a <code>File</code> which specifies a directory name.
	 * @return size of folder in bytes
	 */
	public static long getFolderSize(File directory) {
		long length = 0;
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					length += file.length();
				} else {
					length += getFolderSize(file);
				}
			}
		}
		return length;
	}

	/**
	 * Returns true if the sequence consists of pattern
	 *
	 * @param sequence
	 *            The url of the reportSource
	 * @param regEx
	 *            The regular expression
	 */

	public static boolean foundPattern(String regEx, String sequence) {
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(sequence);
		return matcher.find();
	}

	/**
	 * Gets encoding value from message.properties
	 *
	 * @return a <code>String</code> which specify encoding value from
	 *         message.properties
	 */
	public static String getEncoding() {
		//PropertiesFileReader propertiesFileReader = new PropertiesFileReader();
		//Map<String, String> propertiesMap = propertiesFileReader.read("message.properties");
		//return propertiesMap.get("encoding");
		return "UTF-8";
	}

	/**
	 * A utility method for casting a collection of objects
	 *
	 * @param clazz
	 *            a class which is in string format
	 * @param collection
	 *            The collection of objects of type clazz
	 * @param <T>
	 *            A type of type T
	 * @return Casted list of type T
	 */
	public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> collection) {
		List<T> list = new ArrayList<T>(collection.size());
		for (Object object : collection) {
			list.add(clazz.cast(object));
		}
		return list;
	}

	/**
	 * Writes an xml file with the specified topLevelKeyName
	 *
	 * @param xmlFile
	 *            a <code>File</code> which specify file name.
	 * @param jsonObject
	 *            a <code>JSONObject</code> which specify which has to be
	 *            written in xml.
	 * @param topLevelKeyName
	 *            The root key name of the xml
	 * @return true if successfully created
	 */
	public static boolean writeReportXML(File xmlFile, JSONObject jsonObject, String topLevelKeyName) {
		XMLSerializer serializer = new XMLSerializer();
		JSON json = JSONSerializer.toJSON(jsonObject);
		serializer.setTypeHintsCompatibility(true);
		serializer.setObjectName(topLevelKeyName);
		serializer.setTypeHintsEnabled(false);
		String xml = serializer.write(json);

		// Write the content to the requisite location
		logger.debug("xmlFile = " + xmlFile);
		return ApplicationUtilities.stringToDom(xmlFile, xml);
	}

	/**
	 * <p>
	 * Utility method which checks whether an Object is empty or not.
	 * </p>
	 *
	 * @param value
	 *            a <code>Object</code>
	 * @return true if Object is empty else return false.
	 */
	public static boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		} else if (value instanceof String) {
			return ((String) value).trim().length() == 0;
		} else if (value instanceof Object[]) {
			return ((Object[]) value).length == 0;
		} else if (value instanceof Collection<?>) {
			return ((Collection<?>) value).size() == 0;
		} else if (value instanceof Map<?, ?>) {
			return ((Map<?, ?>) value).size() == 0;
		} else {
			return value.toString() == null || value.toString().trim().length() == 0;
		}
	}

	/**
	 * <p>
	 * This method is responsible to get extension of file in question.
	 * <code>null</code> is returned if the file has no extension.
	 * </p>
	 *
	 * @param file
	 *            a <code>File</code> object which specifies absolute file name.
	 * @return a <code>String</code> which specifies file extension.
	 */
	public static String getExtensionOfFile(File file) {
		String[] array = (file.toString().split("\\.(?=[^\\.]+$)"));
		if (array.length >= 2) {
			return array[1];
		} else {
			return null;
		}
	}
}