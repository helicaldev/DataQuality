package com.helicaltech.pcni.singleton;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * The singleton instance which holds the application settings. Only one
 * instance per application
 *
 * @author Unknown
 */
public class ApplicationProperties {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);

	/**
	 * The instance of the same class
	 */
	private static ApplicationProperties properties = null;

	
	private String solutionDirectory;
	
	private String scheduleTimezone;

	/**
	 * Called only with in the class.
	 */
	private ApplicationProperties() {
		logger.debug("Created the singleton " + this.getClass());
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("project.properties");
		
		Map<String, String> properties = getPropertiesMap(inputStream);
		solutionDirectory=properties.get("solutionDirectory");
		logger.debug("SolutionDirectory is" + solutionDirectory);
		
		scheduleTimezone=properties.get("schedule.timezone");
		logger.debug("Schedule Timezone is" + scheduleTimezone);
		
		setSolutionDirectory(solutionDirectory);
		setScheduleTimezone(scheduleTimezone);
	}

	
	/**
	 * Creates a singleton instance. Only one instance per application
	 *
	 * @return Returns the properties object. Creates one if it is null.
	 */
	public synchronized static ApplicationProperties getInstance() {
		if (properties == null) {
			properties = new ApplicationProperties();
		}
		return properties;
	}

	/**
	 * Returns the EFW solution directory. Path separator should be used with
	 * this value
	 *
	 * @return Returns the EFW solution directory
	 */
	public String getSolutionDirectory() {
		return solutionDirectory;
	}

	/**
	 * Sets the HDI solution directory location
	 *
	 * @param solutionDirectory
	 *            The EFW directory location
	 */
	public void setSolutionDirectory(String solutionDirectory) {
		this.solutionDirectory = solutionDirectory;
	}

	/**
	 * Returns HDI report schedule Timezone
	 *
	 * @return Returns the HDI report schedule Timezone
	 */
	public String getScheduleTimezone() {
		return scheduleTimezone;
	}

	/**
	 * Sets the HDI report schedule Timezone
	 *
	 * @param scheduleTimezone
	 *            
	 */
	public void setScheduleTimezone(String scheduleTimezone) {
		this.scheduleTimezone = scheduleTimezone;
	}
	
	private Map<String, String> getPropertiesMap(InputStream inputStream) {
		Map<String, String> propertiesMap = new HashMap<String, String>();
		Properties properties = new Properties();
		try {
			if (inputStream != null) {
				properties.load(inputStream);
				Set<Object> keySet = properties.keySet();

				for (Object aKeySet : keySet) {
					String key = (String) aKeySet;
					String value = properties.getProperty(key);
					propertiesMap.put(key, value);
				}
			} else {
				logger.error("InputStream is null!");
			}

		} catch (FileNotFoundException ex) {
			logger.error("properties file is not present", ex);
			ex.printStackTrace();
		} catch (IOException e) {
			logger.error("IOException occurred", e);
			e.printStackTrace();
		}

		Assert.notNull(propertiesMap, "Property file map is null!!");
		return propertiesMap;
	}
}
