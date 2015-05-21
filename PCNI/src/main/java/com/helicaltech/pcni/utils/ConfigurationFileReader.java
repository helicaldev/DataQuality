package com.helicaltech.pcni.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import com.helicaltech.pcni.exceptions.ConfigurationException;
import com.helicaltech.pcni.exceptions.RuntimeIOException;

/**
 * @author Rajasekhar
 */

public class ConfigurationFileReader {

	private final static Logger logger = LoggerFactory.getLogger(ConfigurationFileReader.class);
	
	@NotNull
	public static Map<String, String> getMapFromClasspathPropertiesFile(String classPathPropertiesFile) {
		InputStream inputStream = ConfigurationFileReader.class.getClassLoader().getResourceAsStream(
				classPathPropertiesFile);
		return getPropertiesMap(inputStream);
	}

	@NotNull
	private static Map<String, String> getPropertiesMap(InputStream inputStream) {
		Map<String, String> propertiesMap = new HashMap<String, String>();
		Properties properties = new Properties();
		try {
			properties.load(inputStream);
			Set<Object> keySet = properties.keySet();
			for (Object aKeySet : keySet) {
				String key = (String) aKeySet;
				String value = properties.getProperty(key);
				propertiesMap.put(key, value);
			}
		} catch (FileNotFoundException ex) {
			throw new ConfigurationException("Property file not found in the class path", ex);
		} catch (IOException e) {
			throw new RuntimeIOException("Property file could not be read", e);
		}
		return propertiesMap;
	}

	public static Map<String, String> getMapFromPropertiesFile(File configurationFile) {
		try {
			InputStream inputStream = new FileInputStream(configurationFile);
			return getPropertiesMap(inputStream);
		} catch (FileNotFoundException e) {
			throw new ConfigurationException(configurationFile + " is not present in the directory", e);
		}
	}

	public static Properties getPropertiesFromClasspathFile(String classPathPropertiesFileName) {
		InputStream inputStream = ConfigurationFileReader.class.getClassLoader().getResourceAsStream(
				classPathPropertiesFileName);
		return getProperties(inputStream);
	}

	public static Properties getPropertiesFromFile(File configurationFile) {
		try {
			InputStream inputStream = new FileInputStream(configurationFile);
			return getProperties(inputStream);
		} catch (FileNotFoundException e) {
			throw new ConfigurationException(configurationFile + " is not present in the directory", e);
		}
	}

	private static Properties getProperties(InputStream inputStream) {
		Properties properties = new Properties();
		try {
			properties.load(inputStream);
			return properties;
		} catch (FileNotFoundException ex) {
			throw new ConfigurationException("Property file not found in the class path", ex);
		} catch (IOException e) {
			throw new RuntimeIOException("Property file could not be read", e);
		}
	}
	/**
	 * This method is used to read a property file with in the EFW solution
	 * directory.
	 *
	 * @param directory
	 *            A directory with in EFW/System directory
	 * @param fileName
	 *            Name of the properties file
	 * @return A <code>Map<String, String></code> with properties as keys
	 */
	public Map<String, String> read(String directory, String fileName) {
		String path = ApplicationProperties.getInstance().getSolutionDirectory() + File.separator + "System";
		File dir = new File(path + File.separator + directory);

		ApplicationUtilities.createDirectory(dir);

		String propertiesFile = path + File.separator + directory + File.separator + fileName;

		logger.debug("propertiesFile = " + propertiesFile);
		try {
			InputStream inputStream = new FileInputStream(propertiesFile);
			return getPropertiesMap(inputStream);
		} catch (FileNotFoundException e) {
			logger.error(fileName + " is not present", e);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * This method is used to read a property file with in the web application
	 * class-path
	 *
	 * @param propertiesFile
	 *            propertyFile in the class path
	 * @return A <code>Map<String, String></code> with properties as keys
	 */

	public Map<String, String> read(String propertiesFile) {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);
		return getPropertiesMap(inputStream);
	}
}