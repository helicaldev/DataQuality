package com.helicaltech.pcni.rules;

import com.helicaltech.pcni.rules.interfaces.IFolderRule;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Some business specific rules (like whether to allow the currently logged in
 * user should view the folder in question in the view or not) are applied on
 * the folders in the solution directory. For the same purpose this class
 * instance can be used.
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class EFWFolderRuleValidator implements IFolderRule {

	private static final Logger logger = LoggerFactory.getLogger(EFWFolderRuleValidator.class);

	/**
	 * Variable of the same class
	 */
	private static EFWFolderRuleValidator ruleValidator;

	/**
	 * For singleton structure
	 */
	private EFWFolderRuleValidator() {
	}

	/**
	 * Typical singleton class instance getter
	 *
	 * @return Instance of the same class
	 */
	public static synchronized EFWFolderRuleValidator getInstance() {
		if (ruleValidator == null) {
			ruleValidator = new EFWFolderRuleValidator();
		}
		return ruleValidator;
	}

	/**
	 * Currently as it stands the folder is validated only if the the user
	 * credentials in the json parameter are matching with the currently logged
	 * in user
	 *
	 * @param ruleExtensionFileJSON
	 *            The json of the file used for indicating the credentials of
	 *            the user
	 * @return The result of user credentials matching or not.
	 */
	public boolean validateRule(JSONObject ruleExtensionFileJSON) {
		List<String> userDetails = BusinessRulesUtils.getUserDetails();
		try {
			logger.debug("User: "+userDetails.get(0)+" With Extension: "+ruleExtensionFileJSON);
			if (userDetails.get(0) != null && userDetails.get(0).equals(ruleExtensionFileJSON.getJSONObject("security").getString("createdBy"))) {
				boolean matchingOrganization = false;
				String organization = ruleExtensionFileJSON.getJSONObject("security").getString("organization");
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
					return "true".equals(ruleExtensionFileJSON.getString("visible"));
				}
			}
		} catch (JSONException ex) {
			logger.error("JSONException", ex);
		}
		return false;
	}

	/**
	 * Pretty toString() of the class
	 *
	 * @return The class name itself
	 */
	public String toString() {
		return "EFWFolderRuleValidator";
	}
}
