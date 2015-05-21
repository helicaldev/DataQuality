package com.helicaltech.pcni.export;

import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import com.helicaltech.pcni.utils.ConfigurationFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * Cleans the System/Temp directory in solution directory
 */
public class TempDirectoryCleaner {
	private final static Logger logger = LoggerFactory.getLogger(TempDirectoryCleaner.class);

	/**
	 * Cleans the System/Temp directory in solution directory
	 *
	 * @param temporaryDirectory
	 *            The location of the directory on the file system
	 */
	public static void clean(File temporaryDirectory) {
		ConfigurationFileReader reader = new ConfigurationFileReader();
		Map<String, String> messagesMap = reader.read("message.properties");

		int maxSize = Integer.parseInt(messagesMap.get("maxSize"));

		long size = ApplicationUtilities.getFolderSize(temporaryDirectory);
		logger.info("Temporary directory size = " + size);
		if (size > maxSize) {
			purgeTempFiles(temporaryDirectory);
		}
	}

	/**
	 * Deletes the directory contents
	 *
	 * @param directoryName
	 *            The directory under concern
	 */
	private static void purgeTempFiles(File directoryName) {
		logger.info("Directory to be cleaned : " + directoryName);
		logger.info("directoryName.exists() : " + directoryName.exists());
		File[] files = directoryName.listFiles();

		if (files != null) {
			for (File file : files) {
				logger.info("deleting " + file);
				file.delete();
			}
		}
	}

	/**
	 * Returns the System/Temp directory path as a string in solution directory
	 *
	 * @return The location of System/Temp directory
	 */
	public static File getTempDirectory() {
		ApplicationProperties properties = ApplicationProperties.getInstance();
		String strTempDir = properties.getSolutionDirectory() + File.separator + "System" + File.separator + "Temp";
		return new File(strTempDir);
	}
}
