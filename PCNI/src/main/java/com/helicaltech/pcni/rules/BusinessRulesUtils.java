package com.helicaltech.pcni.rules;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helicaltech.pcni.login.LoginForm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consists of a set of utility methods used by various rules related classes
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class BusinessRulesUtils {
	public static final Logger logger = LoggerFactory.getLogger(BusinessRulesUtils.class);

	/**
	 * Obtains the currently logged in user credentials as a <code>List</code>
	 *
	 * @return A list of username, organization and password in the same order
	 */
	public static List<String> getUserDetails() {
		List<String> userDetails = new ArrayList<String>();
//		ProfileUserNPrincipal activeUser = (ProfileUserNPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		logger.debug("activeUser : " + LoginForm.getInstance().getjUserName());
		userDetails.add(0, LoginForm.getInstance().getjUserName());
		//logger.debug("activeUser's organization : " + activeUser.getOrg_name());
		userDetails.add(1,null);
		userDetails.add(2, LoginForm.getInstance().getjPassword());
		//logger.debug("password:  " + userDetails.get(2));
		return userDetails;
	}

	/**
	 * Returns true if the maybeChild parameter is a child of possibleParent
	 * parameter
	 *
	 * @param maybeChild
	 *            The supposed child directory
	 * @param possibleParent
	 *            The supposed parent directory
	 * @return true if the maybeChild is a child of possibleParent
	 * @throws IOException
	 *             If some thing goes wrong during the IO Operation
	 */
	public static boolean isChild(File maybeChild, File possibleParent) throws IOException {
		final File parent = possibleParent.getCanonicalFile();
		if (!parent.exists() || !parent.isDirectory()) {
			// this cannot possibly be the parent
			return false;
		}

		File child = maybeChild.getCanonicalFile();
		while (child != null) {
			if (child.equals(parent)) {
				return true;
			}
			child = child.getParentFile();
		}
		// No match found, and we've hit the root directory
		return false;
	}
}
