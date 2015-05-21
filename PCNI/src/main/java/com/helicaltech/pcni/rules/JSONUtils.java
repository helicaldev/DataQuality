package com.helicaltech.pcni.rules;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Consists of a set of utility methods related to json processing
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class JSONUtils {

	private static final Logger logger = LoggerFactory.getLogger(JSONUtils.class);

	/**
	 * Returns the xml template of the security tag
	 *
	 * @return The template json of the security tag
	 */
	public static JSONObject getSecurityJSONObject() {
		List<String> userDetails = BusinessRulesUtils.getUserDetails();

		JSONObject security = new JSONObject();
		security.accumulate("createdBy", userDetails.get(0));
		security.accumulate("organization", userDetails.get(1) == null ? "" : userDetails.get(1));
		return security;
	}

	/**
	 * Verifies whether the currently logged in user has access to the file
	 * represented by the json parameter
	 *
	 * @param userDetails
	 *            The currently logged in user credentials list
	 * @param jsonObject
	 *            The json of the file under concern
	 * @return true if validated
	 */
	public static boolean verifyUserCredentials(List<String> userDetails, JSONObject jsonObject) {
		try {
			if (userDetails.get(0) != null && userDetails.get(0).equals(jsonObject.getJSONObject("security").getString("createdBy"))) {
				boolean matchingOrganization = false;
				String organization = jsonObject.getJSONObject("security").getString("organization");
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
					logger.debug("Matching user credentials for the file");
					return "true".equals(jsonObject.getString("visible"));
				}
			}
		} catch (JSONException ex) {
			logger.error("JSONException", ex);
		}
		logger.warn("User credentials are not matching. Bad Request");
		return false;
	}
}
