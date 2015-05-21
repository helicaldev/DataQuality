package com.helicaltech.pcni.rules.io;

import com.helicaltech.pcni.exceptions.ImproperXMLConfigurationException;
import com.helicaltech.pcni.export.EnableSaveResult;
import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.rules.interfaces.IDeleteOperation;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.useractions.DeleteOperationUtility;
import com.helicaltech.pcni.useractions.IOOperationsUtility;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by author on 22-10-2014.
 * <p/>
 * This singleton class handles the deletion of specific files i.e. .result
 * files
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class EFWSavedResultDeleteRule implements IDeleteOperation {

	private static final Logger logger = LoggerFactory.getLogger(EFWSavedResultDeleteRule.class);
	/**
	 * A static member of the same class
	 */
	private static EFWSavedResultDeleteRule deleteRule = new EFWSavedResultDeleteRule();

	/**
	 * Private for the purpose of singleton pattern
	 */
	private EFWSavedResultDeleteRule() {
	}

	/**
	 * The method returns the singleton object of the same class
	 *
	 * @return Returns the instance of the same class.
	 */
	public static synchronized EFWSavedResultDeleteRule getInstance() {
		return deleteRule;
	}

	/**
	 * Tells whether the file is deletable or not. Deletable only if the user
	 * credentials are matching.
	 *
	 * @param file
	 *            The file under inspection to be deleted of type .result
	 * @return true if security credentials are matching
	 */
	public boolean isDeletable(File file) {
		return new DeleteOperationUtility().areUserCredentialsMatching(file);
	}

	/**
	 * Simply deletes the file after deleting the saved result file by reading
	 * the file content
	 *
	 * @param file
	 *            The file under inspection to be deleted of type .result
	 */
	public void delete(File file) {
		JSONProcessor jsonProcessor = new JSONProcessor();
		JSONObject jsonObject = jsonProcessor.getJSON(file.toString(), false);
		String efwSaveResultExtension = new EnableSaveResult().getEnabledResultExtension();

		if (efwSaveResultExtension == null) {
			try {
				throw new ImproperXMLConfigurationException("efwResult tag is not found in settings");
			} catch (ImproperXMLConfigurationException e) {
				logger.error("efwResult tag is not found", e);
			}
		}

		if (jsonObject.has("resultDirectory")) {
			if (jsonObject.has("resultFile")) {
				String resultFile = jsonObject.getString("resultFile");
				String resultDirectory = jsonObject.getString("resultDirectory");
				if (!deleteSavedResult(resultDirectory, resultFile)) {
					logger.error("Couldn't delete the " + resultFile + " from " + resultDirectory);
				}
				IOOperationsUtility.deleteWithLogs(file);
			} else {
				logger.error("The file doesn't seem to have resultFile.");
			}
		} else {
			logger.error("The file doesn't seem to have resultDirectory.");
		}
	}

	/**
	 * The method deletes the saved result file from the directory mentioned in
	 * the file
	 *
	 * @param resultDirectory
	 *            The directory in which the result is saved
	 * @param resultFile
	 *            The file to be deleted
	 * @return false if already deleted or not found
	 */
	private boolean deleteSavedResult(String resultDirectory, String resultFile) {
		File resultFileObject = new File(ApplicationProperties.getInstance().getSolutionDirectory() + File.separator + resultDirectory
				+ File.separator + resultFile);
		if (!resultFileObject.exists()) {
			logger.info("The file " + resultFile + " has already been deleted");
			return false;
		} else {
			IOOperationsUtility.deleteWithLogs(resultFileObject);
		}
		return true;
	}
}
