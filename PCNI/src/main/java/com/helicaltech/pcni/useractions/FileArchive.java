package com.helicaltech.pcni.useractions;

import com.helicaltech.pcni.exceptions.ConfigurationException;
//import com.helicaltech.pcni.resourceloader.JSONProcessor;
//import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import net.lingala.zip4j.core.ZipFile;
import net.sf.json.JSONException;
//import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

/**
 * <p>
 * This class can archive files with extension of efwExport from the setting.xml
 * in System/Temp directory.
 * </p>
 *
 * @author Rajasekhar
 * @version 1.0
 * @since 1.1
 */

public class FileArchive {

	private static Logger logger = LoggerFactory.getLogger(FileArchive.class);

	/**
	 * Archives the files in fileArray as zipFile
	 *
	 * @param zipFile
	 *            File to be zipped. The location of the file to be created
	 * @param fileArray
	 *            An array of efwsr files
	 * @return true or false based on the conditions
	 */
	public boolean archive(File zipFile, File[] fileArray) {
		ZipOutputStream zipOutputStream = null;
		try {
			FileInputStream fileInputStream;
			try {
				zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
				for (File file : fileArray) {
					ZipEntry entry;
					try {
						entry = new ZipEntry(file.getName());
						zipOutputStream.putNextEntry(entry);
					} catch (ZipException e) {
						logger.error("ZipException occurred due to duplicate entry. Prefixing time stamp to the file " + file);
						entry = new ZipEntry(System.currentTimeMillis() + file.getName());
						zipOutputStream.putNextEntry(entry);
					}
					fileInputStream = new FileInputStream(file);
					byte[] byteBuffer = new byte[1024];
					int bytesRead;
					while ((bytesRead = fileInputStream.read(byteBuffer)) != -1) {
						zipOutputStream.write(byteBuffer, 0, bytesRead);
					}
					zipOutputStream.closeEntry();
					ApplicationUtilities.closeResource(fileInputStream);
				}
			} catch (FileNotFoundException e) {
				logger.error("FileNotFoundException ", e);
				return false;
			} catch (IOException e) {
				logger.error("IOException ", e);
				return false;
			} finally {
				if (zipOutputStream != null) {
					zipOutputStream.flush();
				}
			}
		} catch (IOException e) {
			logger.error("IOException ", e);
			return false;
		} finally {
			ApplicationUtilities.closeResource(zipOutputStream);
		}
		return true;
	}

	/**
	 * Obtains the extension of the zip file from setting.xml
	 *
	 * @return returns the efwExport key value from the setting.xml
	 */

	public String getExtensionOfZipFile() {
//		ApplicationProperties properties = ApplicationProperties.getInstance();
//		JSONProcessor processor = new JSONProcessor();
		//JSONObject json = processor.getJSON(properties.getSettingPath(), false);

		String extension = null;
		try {
			extension = "crt";
			logger.debug("efwExport text value = " + extension);
		} catch (JSONException ex) {
			logger.error("Please provide efwExport tag in Extensions of settings", ex);
		} catch (ConfigurationException e) {
			logger.error("ApplicationException", e);
		}
		return extension;
	}

	/**
	 * Unzips the source in to the destination directory
	 *
	 * @param source
	 *            The source of the zip
	 * @param destination
	 *            The destination where the zip has to be unzipped
	 * @param password
	 *            The password of the zip file (if any)
	 * @return true if the operation is successful
	 */
	public boolean unzip(String source, String destination, String password) {
		try {
			ZipFile zipFile = new ZipFile(source);
			if (zipFile.isEncrypted()) {
				zipFile.setPassword(password);
			}
			zipFile.extractAll(destination);
		} catch (net.lingala.zip4j.exception.ZipException e) {
			logger.error("ZipException", e);
			return false;
		}
		return true;
	}
}