package com.helicaltech.pcni.utility;

/**
 * <p>
 * Utility class which finds the type of OS on which the application is running
 * </p>
 *
 * @author Rajasekhar
 * @version 1.0
 * @since 1.0
 */
public final class OsCheck {
	/**
	 * enum type which contains OS name.
	 */
	private static OSType detectedOS;

	/**
	 * <p>
	 * Finds the OS
	 * </p>
	 *
	 * @return One of the values of the enum OSType
	 */
	public static OSType getOperatingSystemType() {
		if (detectedOS == null) {
			String OS = System.getProperty("os.name", "generic").toLowerCase();
			if (OS.contains("win")) {
				detectedOS = OSType.Windows;
			} else if ((OS.contains("mac")) || (OS.contains("darwin"))) {
				detectedOS = OSType.MacOS;
			} else {
				detectedOS = OSType.Linux;
			}
		}
		return detectedOS;
	}

	/**
	 * Represents the popular os types i.e Windows, or MacOS or Linux
	 */
	public enum OSType {
		Windows, MacOS, Linux
	}
}