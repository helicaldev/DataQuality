package com.helicaltech.pcni.rules.io;

//import com.helicaltech.pcni.controller.SaveReportController;
import com.helicaltech.pcni.exceptions.ImproperXMLConfigurationException;
import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.rules.JSONUtils;
import com.helicaltech.pcni.rules.interfaces.IImportOperation;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.useractions.IOOperationsUtility;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * A directory of efwsr key value extension files will be imported in to a
 * destination directory. Other types of files or directories won't be imported.
 * <p/>
 * Created by author on 21-10-2014.
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class EFWSRImportRule implements IImportOperation {
	private static final Logger logger = LoggerFactory.getLogger(EFWSRImportRule.class);
	/**
	 * Static variable of the same class
	 */
	private static EFWSRImportRule importRule = new EFWSRImportRule();
	/**
	 * The efwsr key value from setting.xml
	 */
	private final String efwsrExtension = "rdf";

	/**
	 * For Singleton structure - A private constructor
	 */
	private EFWSRImportRule() {
	}

	/**
	 * Typical singleton class instance getter
	 *
	 * @return Instance of the same class
	 */
	public static synchronized EFWSRImportRule getInstance() {
		return importRule;
	}

	/**
	 * Imports the directory represented by the directory parameter in to the
	 * corresponding destination specified as string.
	 * <p/>
	 * The destinationDirectory should actually exist on the file system. If
	 * matching file names are found in the destination directory then the
	 * conflicting file will be renamed with system current time
	 *
	 * @param directory
	 *            The directory to be imported
	 * @param destination
	 *            The destination file path as string
	 * @return false if an IOException occurs
	 */
	public boolean importFile(File directory, String destination) {
		String solutionDirectory = ApplicationProperties.getInstance().getSolutionDirectory();
		File destinationDirectory = new File(solutionDirectory + File.separator + destination);
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				if (!modifyXmlFile(file)) {
					/*
					 * Only in case of IOException the method returns false
					 */
					logger.error("Couldn't import the file " + file + ". Trying to import other files.");
				}
				try {
					FileUtils.moveFileToDirectory(file, destinationDirectory, false);
				} catch (FileExistsException e) {
					logger.info("The destination already consists the file." + file);
					String fileName = FilenameUtils.getName(file.getAbsolutePath());
					String basePath = FilenameUtils.getPath(file.getAbsolutePath());
					File modifiedFile = new File(FilenameUtils.getPrefix(file.toString()) + File.separator + basePath + System.currentTimeMillis()
							+ "_" + fileName);
					logger.debug("Trying to modify conflicting file name with system time " + modifiedFile);
					if (file.renameTo(modifiedFile)) {
						logger.debug("Successfully modified to " + file);
					} else {
						logger.debug("Couldn't rename " + file);
					}
					try {
						FileUtils.moveFileToDirectory(modifiedFile, destinationDirectory, false);
					} catch (IOException ex) {
						logger.error("IOException", ex);
					}
				} catch (IOException e) {
					logger.error("IOException", e);
					return false;
				}
			}
		}
		IOOperationsUtility.deleteEmptyDirectoryWithLogs(directory);
		return true;
	}

	/**
	 * Only a directory of files of type with extension of the key value efwsr
	 * will be imported. The method validates whether the directory consists of
	 * other extension files or not. If it consists it returns false. Similarly
	 * directories also can't be imported.
	 *
	 * @param directory
	 *            The directory to be validated
	 * @return true if successfully validated
	 */
	public boolean validateDirectory(File directory) {
		File[] files = directory.listFiles();
		if (files != null) {
			if (efwsrExtension != null) {
				logger.debug("files length : " + files.length);
				for (File file : files) {
					if (!file.isFile() && efwsrExtension.equalsIgnoreCase(ApplicationUtilities.getExtensionOfFile(file))) {
						logger.error("File " + file + " type is unknown! Can't import such files.");
						return false;
					}
					if (file.isDirectory()) {
						logger.error("File " + file + " is a directory. Can't import directories. Expecting only efwsr files.");
						return false;
					}
				}
			} else {
				try {
					throw new ImproperXMLConfigurationException("EFWSR tag value not found");
				} catch (ImproperXMLConfigurationException e) {
					logger.error("EFWSR tag value not found", e);
				}
			}
		} else {
			logger.error("Directory " + directory + " is null.");
			return false;
		}
		return true;
	}

	/**
	 * The newly imported efwsr files favourite tag will be made false and the
	 * security tag will be modified according to the currently logged in user.
	 *
	 * @param file
	 *            The file under concern
	 * @return true if successfully modifies the xml
	 */
	private boolean modifyXmlFile(File file) {
		JSONProcessor jsonProcessor = new JSONProcessor();
		JSONObject jsonObject = jsonProcessor.getJSON(file.getAbsolutePath(), false);
		String reportParameters = jsonObject.getString("reportParameters");

		jsonObject.discard("schedulingReference");
		jsonObject.discard("favourite");
		jsonObject.discard("security");
		jsonObject.discard("reportParameters");

		jsonObject.accumulate("favourite", false);
		jsonObject.accumulate("security", JSONUtils.getSecurityJSONObject());
		jsonObject.accumulate("reportParameters", "<![CDATA[" + reportParameters + "]]>");
		if (file.delete()) {
			logger.debug("Deleted file " + file + " successfully.");
		} else {
			logger.debug("Couldn't delete file " + file);
		}
		if (ApplicationUtilities.writeReportXML(file, jsonObject, "efwsr")) {
			logger.debug("Written new data into file " + file + " successfully.");
		} else {
			logger.debug("Couldn't write new data into file " + file);
			return false;
		}
		return true;
	}
}
