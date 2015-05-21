package com.helicaltech.pcni.rules.io;

import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.rules.interfaces.IDeleteOperation;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.useractions.DeleteOperationUtility;
import com.helicaltech.pcni.useractions.IOOperationsUtility;
import com.helicaltech.pcni.useractions.UserActionsUtility;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A configuration class for the efwsr files to be deleted. Currently a file is
 * deletable only if the file has matching user credentials. The favourite files
 * of the efwsr files will also be deleted.
 * <p/>
 * Created by author on 16-10-2014.
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class EFWSRDeleteRule implements IDeleteOperation {

	private static final Logger logger = LoggerFactory.getLogger(EFWSRDeleteRule.class);
	/**
	 * Static variable of the same class
	 */
	private static EFWSRDeleteRule deleteRule = new EFWSRDeleteRule();

	/**
	 * For Singleton structure - A private constructor
	 */
	private EFWSRDeleteRule() {
	}

	/**
	 * Typical singleton class instance getter
	 *
	 * @return Instance of the same class
	 */
	public static synchronized EFWSRDeleteRule getInstance() {
		return deleteRule;
	}

	/**
	 * Returns true if user credentials are matching; which means the file is
	 * deletable. The corresponding favourite file will also be checked for
	 * valid user credentials.
	 *
	 * @param file
	 *            The file under concern
	 * @return true if user credentials are matching
	 */
	public boolean isDeletable(File file) {
		DeleteOperationUtility deleteOperationUtility = new DeleteOperationUtility();
		if (file.isFile() && deleteOperationUtility.areUserCredentialsMatching(file)) {
			JSONProcessor processor = new JSONProcessor();
			JSONObject jsonObject = processor.getJSON(file.toString(), false);
			String favourite = jsonObject.getString("favourite");
			if ("false".equalsIgnoreCase(favourite)) {
				logger.debug("File " + file + " is deletable");
				return true;
			} else {
				/*
				 * Verify the corresponding efwsr has valid credentials
				 */
				return checkCorrespondingEFWSRCredentials(file, deleteOperationUtility, favourite);
			}
		}
		return false;
	}

	/**
	 * Returns true if the favourite file of the efwsr credentials match
	 *
	 * @param file
	 *            The file under concern
	 * @param deleteOperationUtility
	 *            An instance of DeleteOperationUtility
	 * @param favourite
	 *            The favourite file of the efwsr
	 * @return true if the favourite file credentials match
	 */
	private boolean checkCorrespondingEFWSRCredentials(File file, DeleteOperationUtility deleteOperationUtility, String favourite) {
		ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
		String path = new UserActionsUtility().search(applicationProperties.getSolutionDirectory(), favourite);
		logger.debug("Favourite for the file " + file + " is " + path);
		if (path == null) {
			logger.debug("No need to update the corresponding efwsr file as it is null. " + file + " is deletable");
			return true;
		}
		if (deleteOperationUtility.areUserCredentialsMatching(new File(path))) {
			logger.debug("File " + file + " is deletable");
			return true;
		}
		return false;
	}

	/**
	 * Deletes the efwsr file by deleting the corresponding favourite file in
	 * the solution directory.
	 *
	 * @param file
	 *            The file under concern
	 */
	public void delete(File file) {
		JSONProcessor processor = new JSONProcessor();
		JSONObject jsonObject = processor.getJSON(file.toString(), false);
		String favourite;
		try {
			favourite = jsonObject.getString("favourite");
		} catch (JSONException ex) {
			logger.debug("JSONException occurred as favourite tag is not present. Simply deleting");
			IOOperationsUtility.deleteWithLogs(file);
			return;
		}
		if (!"false".equalsIgnoreCase(favourite)) {
			ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
			String path = new UserActionsUtility().search(applicationProperties.getSolutionDirectory(), favourite);
			logger.debug("Favourite file for the file " + file + " is " + path);
			if (path == null) {
				logger.debug("Favourite file for the file " + file + " is already deleted.");
				IOOperationsUtility.deleteWithLogs(file);
				return;
			}
			/*
			 * Delete the favourite first and then delete the efwsr
			 */
			if (new File(path).delete()) {
				logger.debug("Successfully deleted the favourite file for the file " + file + " for which the path is " + path);
			} else {
				logger.debug("Couldn't delete the favourite file for the file " + file + " for which the path is " + path);
			}
		}
		IOOperationsUtility.deleteWithLogs(file);
	}
}
