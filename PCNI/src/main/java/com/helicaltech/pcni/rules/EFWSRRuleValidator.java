package com.helicaltech.pcni.rules;

import com.helicaltech.pcni.resourceloader.JSONProcessor;
import com.helicaltech.pcni.rules.interfaces.IBusinessRule;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * For EFWSR file type this class is used. This file structure is typically
 * different from efw.
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class EFWSRRuleValidator extends BusinessRule implements IBusinessRule {

	private static final Logger logger = LoggerFactory.getLogger(EFWSRRuleValidator.class);
	/**
	 * Instance variable of the same class
	 */
	private static EFWSRRuleValidator ruleValidator;

	/**
	 * For singleton structure
	 */
	private EFWSRRuleValidator() {
	}

	/**
	 * Typical singleton class instance getter
	 *
	 * @return Instance of the same class
	 */
	public static synchronized EFWSRRuleValidator getInstance() {
		if (ruleValidator == null) {
			ruleValidator = new EFWSRRuleValidator();
		}
		return ruleValidator;
	}

	/**
	 * Validates the file based on the user credentials information present in
	 * it and based on the visibility of the file present in it. Validated only
	 * if visibility is true
	 *
	 * @param file
	 *            The file under concern
	 * @param extensionKey
	 *            The extension of the file type. The tag key and not the value
	 * @return The result of user credentials matching or not. The visibility of
	 *         the file should also be true
	 */
	public boolean validateRule(File file, String extensionKey) {
		List<String> userDetails = BusinessRulesUtils.getUserDetails();
		JSONProcessor jsonProcessor = new JSONProcessor();
		JSONObject json = jsonProcessor.getJSON(file.toString(), false);
		try {
			logger.debug("Validating EFWSRRULE: for user : "+userDetails.get(0));
			if (userDetails.get(0).equals(json.getJSONObject("security").getString("createdBy"))) {
				boolean matchingOrganization = false;
				String organization = json.getJSONObject("security").getString("organization");
				logger.debug("user name has matched." + " Organization : " + organization + ". organization from userDetails list: "
						+ userDetails.get(1));
				if (userDetails.get(1) == null) {
					if (null == organization || "[]".equals(organization)) {
						matchingOrganization = true;
					}
				} else {
					if (userDetails.get(1).equals(organization)) {
						matchingOrganization = true;
					}
				}
				if (matchingOrganization) {
					logger.debug("Matching user credentials");
					return "true".equals(json.getString("visible"));
				}
			}
		} catch (JSONException ex) {
			logger.error("JSONException", ex);
		}
		return false;
	}

	/**
	 * Pretty toString method
	 *
	 * @return The class name
	 */
	public String toString() {
		return "EFWSRRuleValidator";
	}

	/**
	 * Includes the resource under concern in the json being sent to the view
	 *
	 * @param file
	 *            The file under concern
	 * @param extensionKey
	 *            The extension of the file type. The tag key and not the value
	 * @return A map of the file content
	 */
	public Map<String, String> getResourceMap(File file, String extensionKey) {
		return includeMap(file, extensionKey);
	}
}
