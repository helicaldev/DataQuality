package com.helicaltech.pcni.rules.interfaces;

import java.io.File;
import java.util.Map;

/**
 * The interface is designed to prepare the json object being sent to the
 * browser, which is being displayed as the file tree, based on various
 * conditions.
 *
 * @author Rajasekhar
 * @since 1.1
 */
public interface IBusinessRule extends IRule {
	/**
	 * True if satisfying the conditions. Otherwise false.
	 *
	 * @param file
	 *            The file under concern
	 * @param extensionKey
	 *            The extension of the file type. The tag key and not the value
	 * @return true if satisfying the conditions
	 */
	public boolean validateRule(File file, String extensionKey);

	/**
	 * Returns a map of the validated file content
	 *
	 * @param file
	 *            The file under concern
	 * @param extensionKey
	 *            The extension of the file type. The tag key and not the value
	 * @return <code>Map</code> of the file
	 */
	public Map<String, String> getResourceMap(File file, String extensionKey);
}