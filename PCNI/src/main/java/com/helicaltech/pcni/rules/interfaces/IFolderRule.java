package com.helicaltech.pcni.rules.interfaces;

import net.sf.json.JSONObject;

/**
 * Designed for validating folders for security purposes.
 *
 * @author Rajasekhar
 * @since 1.1
 */
public interface IFolderRule extends IRule {

	/**
	 * Returns true if the user is authenticated to view the folder
	 *
	 * @param ruleExtensionFileJSON
	 *            The json of the file used for indicating the credentials of
	 *            the user
	 * @return true if the user is authenticated to view the folder
	 */
	public boolean validateRule(JSONObject ruleExtensionFileJSON);

}
