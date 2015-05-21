package com.helicaltech.pcni.rules.interfaces;

import java.io.File;

/**
 * Designed for file import facility
 * <p/>
 * Created by author on 21-10-2014.
 *
 * @author Rajasekhar
 * @since 1.1
 */

public interface IImportOperation extends IRule {
	/**
	 * Imports the absolute file represented by the file parameter in to the
	 * corresponding destination
	 *
	 * @param file
	 *            The file to be imported
	 * @param destination
	 *            The destination file path as string
	 * @return false if an IOException occurs
	 */
	public boolean importFile(File file, String destination);

	/**
	 * Returns true if the directory is validated i.e. if the directory further
	 * doesn't consist of any other directories and only consists of efwsr files
	 *
	 * @param directory
	 *            The directory to be validated
	 * @return true if the directory is validated
	 */
	public boolean validateDirectory(File directory);
}
