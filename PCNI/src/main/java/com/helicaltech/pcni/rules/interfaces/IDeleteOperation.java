package com.helicaltech.pcni.rules.interfaces;

import java.io.File;

/**
 * Used for file delete operations based on various implementations
 * <p/>
 * Created by author on 12-Oct-14.
 *
 * @author Rajasekhar
 * @since 1.1
 */
public interface IDeleteOperation extends IRule {

	/**
	 * The file is deletable only when it has matching user credentials
	 *
	 * @param file
	 *            The file under concern
	 * @return true if deletable
	 */
	public boolean isDeletable(File file);

	/**
	 * Simply deletes the file
	 *
	 * @param file
	 *            The file under concern
	 */
	public void delete(File file);

}
