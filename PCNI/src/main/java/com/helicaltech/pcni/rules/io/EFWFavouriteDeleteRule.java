package com.helicaltech.pcni.rules.io;

import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.rules.interfaces.IDeleteOperation;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.useractions.DeleteOperationUtility;
import com.helicaltech.pcni.useractions.IOOperationsUtility;
import com.helicaltech.pcni.useractions.UserActionsUtility;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * A configuration class for the efw favourite files to be deleted. Currently a
 * file is deletable only if the file has matching user credentials.
 * <p/>
 * Created by author on 16-10-2014.
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class EFWFavouriteDeleteRule implements IDeleteOperation {

	private static final Logger logger = LoggerFactory.getLogger(EFWFavouriteDeleteRule.class);
	/**
	 * An variable of type of type EFWFavouriteDeleteRule
	 */
	private static EFWFavouriteDeleteRule deleteRule = new EFWFavouriteDeleteRule();

	/**
	 * Private for the purpose of singleton pattern
	 */
	private EFWFavouriteDeleteRule() {
	}

	/**
	 * Singleton getter
	 *
	 * @return An instance of the same class
	 */
	public static synchronized EFWFavouriteDeleteRule getInstance() {
		return deleteRule;
	}

	/**
	 * Returns true if user credentials are matching which means the file is
	 * deletable
	 *
	 * @param file
	 *            The file under concern
	 * @return true if user credentials are matching
	 */
	public boolean isDeletable(File file) {
		if (new DeleteOperationUtility().areUserCredentialsMatching(file)) {
			logger.debug("The file " + file + " is deletable");
			return true;
		} else {
			logger.debug("The file " + file + " is not deletable");
			return false;
		}
	}

	/**
	 * Deletes the favourite file and updates the corresponding efwsr file from
	 * which it is created.
	 *
	 * @param file
	 *            The file under concern
	 */
	public void delete(File file) {
		JSONProcessor jsonProcessor = new JSONProcessor();
		JSONObject jsonObject = jsonProcessor.getJSON(file.toString(), false);
		String efwsrFile = null;
		try {
			efwsrFile = jsonObject.getString("savedReportFileName");
		} catch (JSONException ex) {
			logger.error("JSONException", ex);
		}
		if (efwsrFile != null) {
			updateCorrespondingFile(file, jsonProcessor, efwsrFile);
		}
		IOOperationsUtility.deleteWithLogs(file);
	}

	/**
	 * The efwsr file referenced by the favourite file will be updated(Its
	 * favourite tag will be false)
	 *
	 * @param file
	 *            The file under concern
	 * @param processor
	 *            JSONProcessor instance
	 * @param efwsrFile
	 *            The corresponding efwsr file for the favourite file
	 */
	private void updateCorrespondingFile(File file, JSONProcessor processor, String efwsrFile) {
		ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
		String efwsrFilePath = new UserActionsUtility().search(applicationProperties.getSolutionDirectory(), efwsrFile);
		logger.debug("EFWSR file for " + file + " is " + efwsrFilePath);
		if (efwsrFilePath == null) {
			logger.debug("EFWSR file for " + file + " is already deleted. Simply deleting the favourite file.");
			IOOperationsUtility.deleteWithLogs(file);
			return;
		}
		JSONObject json = processor.getJSON(efwsrFilePath, false);
		/*
		 * To maintain the structure of xml
		 */
		json.discard("favourite");
		try {
			String reportParameters = json.getString("reportParameters");
			json.discard("reportParameters");
			json.accumulate("reportParameters", "<![CDATA[" + reportParameters + "]]>");
		} catch (JSONException ex) {
			logger.error("There is no reportParameters string in the file", ex);
		}
		json.accumulate("favourite", "false");
		if (ApplicationUtilities.writeReportXML(new File(efwsrFilePath), json, "efwsr")) {
			logger.info("The file " + efwsrFilePath + " is successfully updated");
		}
	}
}
