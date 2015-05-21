package com.helicaltech.pcni.export;

import com.helicaltech.pcni.singleton.ApplicationProperties;

import java.io.File;

/**
 * Depending on the os type the location of the phantom binary in the system
 * directory is determined by this class object. The location of the phantom js
 * is different for different os types. The method returns the appropriate
 * binary corresponding to the os. Currently only Windows, Mac OS and Linux are
 * supported.
 *
 * @author Rajasekhar
 * @since 1.0
 */
public class PhantomLocationResolver {

	private final ApplicationProperties applicationProperties;

	/**
	 * Sets the singleton instance applicationProperties to the member variable
	 * applicationProperties
	 */
	public PhantomLocationResolver() {
		this.applicationProperties = ApplicationProperties.getInstance();
	}

	/**
	 * Resolves the location of the phantom js binary
	 *
	 * @param osType
	 *            The type of os
	 * @return The location of the phantom js binary
	 */
	public String resolve(String osType) {
		String phantomLocation;
		if ("Windows".equals(osType)) {
			phantomLocation = applicationProperties.getSolutionDirectory() + File.separator + "System" + File.separator + "Reports" + File.separator
					+ "windows_phantomjs.exe";
		} else if ("Mac".equals(osType)) {
			phantomLocation = applicationProperties.getSolutionDirectory() + File.separator + "System" + File.separator + "Reports" + File.separator
					+ "macosx_phantomjs";
		} else {
			phantomLocation = applicationProperties.getSolutionDirectory() + File.separator + "System" + File.separator + "Reports" + File.separator
					+ "linux_phantomjs";
		}

		File file = new File(phantomLocation);
		phantomLocation = file.getAbsolutePath();
		return phantomLocation;
	}
}
