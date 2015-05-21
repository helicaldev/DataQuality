package com.helicaltech.pcni.export;

import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.exceptions.ConfigurationException;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import com.helicaltech.pcni.utility.OsCheck;


//import com.helicaltech.pcni.utility.PropertiesFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * The reports that are downloaded in the HDI are processed by this object.
 *
 * @author Rajasekhar
 * @since 1.0
 */
public class ReportsProcessor {

	private final static Logger logger = LoggerFactory.getLogger(ReportsProcessor.class);

	/**
	 * The location of the screen shot js file
	 */
	private String scriptLocation;

	/**
	 * The location of the phantom js is different for different os types. The
	 * method returns the appropriate binary corresponding to the os. Currently
	 * only Windows, Mac OS and Linux are supported.
	 *
	 * @return The location of the phantom js
	 */
	private static String getPhantomLocation() {
		OsCheck.OSType ostype = OsCheck.getOperatingSystemType();
		String phantomLocation;
		PhantomLocationResolver locationResolver = new PhantomLocationResolver();
		switch (ostype) {
		case Windows: {
			phantomLocation = locationResolver.resolve("Windows");
			break;
		}
		case MacOS: {
			phantomLocation = locationResolver.resolve("Mac");
			break;
		}
		default:
			phantomLocation = locationResolver.resolve("Linux");
		}
		if (phantomLocation == null) {
			try {
				throw new ConfigurationException("");
			} catch (ConfigurationException e) {
				logger.error("phantomLocation is null. Check phantomjs binary is present or not", e);
				e.printStackTrace();
			}
		}
		return phantomLocation;
	}

	/**
	 * This method returns the list of file paths when provided with the
	 * htmlSource. This method will create a temporary html file on the file
	 * system and use it as a source to generate report. The list consists of
	 * the report path as the first index and the second index is the html from
	 * which that report screen shot is taken
	 *
	 * @param htmlSource
	 *            The HTML source as string
	 * @param format
	 *            The type of the report to be generated
	 * @param reportName
	 *            The name of the report to be generated
	 * @return The location of the report on the file system
	 */

	public List<String> generateReportUsingHTMLSource(String htmlSource, String format, String reportName) {

//		PropertiesFileReader reader = new PropertiesFileReader();
//		Map<String, String> messagesMap = reader.read("message.properties");

		String encoding ="UTF-8";
		logger.debug("encoding = " + encoding);

		logger.debug("Actual string htmlString = " + htmlSource + ", format = " + format + ", reportName = " + reportName);

		ReportsUtility reportsUtility = new ReportsUtility();

		htmlSource = reportsUtility.decodeURLEncoding(htmlSource, encoding);

		htmlSource = reportsUtility.decodeBase64Encoding(htmlSource, encoding);

		File temporaryDirectory = new File(ApplicationProperties.getInstance().getSolutionDirectory()+File.separator + "System" + File.separator + "Temp");

		// Create Temp directory if it doesn't exists
		ApplicationUtilities.createDirectory(temporaryDirectory);

		try {
			File temporaryHTMLFile = File.createTempFile(reportName, ".html", temporaryDirectory);

			logger.debug("temporaryHTMLFile = " + temporaryHTMLFile);

			
			if (isCompletelyWritten(temporaryHTMLFile, htmlSource, encoding)) {
				logger.info("htmlString is written successfully with encoding " + encoding);
				return generateReportFromURI(temporaryHTMLFile.toString(), format, reportName);
			}
		} catch (IOException e) {
			logger.error("IOException occured while writing file", e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method returns a list of file locations on the file system when
	 * provided with the URI. The list consists of the report path as the first
	 * index and the second index is the html from which that report screen shot
	 * is taken.
	 *
	 * @param reportSourceURI
	 *            The URI of the input html file
	 * @param reportName
	 *            The name of the report to be generated
	 * @param format
	 *            The type of the report to be generated
	 * @return The location of the report on the file system
	 */

	public List<String> generateReportFromURI(String reportSourceURI, String format, String reportName) {

		if (ApplicationUtilities.foundPattern("[&]", reportSourceURI)) {
			logger.debug("reportSourceURI = " + reportSourceURI + ". Found parameters. Replacing & with #");
			reportSourceURI = reportSourceURI.replace("&", "#");
			this.scriptLocation = getScriptLocation(true);
		} else {
			this.scriptLocation = getScriptLocation(false);
		}

		String destinationFile = new File(ApplicationProperties.getInstance().getSolutionDirectory()+File.separator + "System" + File.separator + "Temp") + File.separator + reportName + "." + format;
		List<String> locationsList = new ArrayList<String>();
		try {
			String phantomLocation = getPhantomLocation();

			PhantomJS phantomJS = new PhantomJS(phantomLocation, scriptLocation, reportSourceURI, destinationFile);
			Thread phantomThread = new Thread(phantomJS);
			phantomThread.setName("phantomThread");
			logger.info("CurrentThread = " + Thread.currentThread() + ". Starting phantomThread to generate the report format.");
			phantomThread.start();
			phantomThread.join();
			logger.info("phantomThread execution is completed. Resuming application thread " + Thread.currentThread().getName());
			locationsList.add(0, destinationFile);
			locationsList.add(1, reportSourceURI);
		} catch (InterruptedException e) {
			logger.error("InterruptedException occured", e);
			e.printStackTrace();
		}
		return locationsList;
	}

	/**
	 * The corresponding screen shot js file location is returned based on the
	 * condition whether requested with url or not.
	 *
	 * @param isRequestedWithURL
	 *            if true the js file should screenshot_url
	 * @return Returns the screenshot java script file location
	 */

	private String getScriptLocation(boolean isRequestedWithURL) {
		if (isRequestedWithURL) {
			this.scriptLocation = getClass().getClassLoader().getResource("/HDIPhantomjs/screenshot_url.js").getFile();
		} else {
			this.scriptLocation = getClass().getClassLoader().getResource("/HDIPhantomjs/screenshot.js").getFile();
		}

		File screenshotFile = new File(scriptLocation);
		scriptLocation = screenshotFile.getAbsolutePath();

		if (scriptLocation == null) {
			try {
				throw new ConfigurationException("");
			} catch (ConfigurationException e) {
				logger.error("scriptLocation is null. Check screenshot.js is present or not", e);
				e.printStackTrace();
			}
		}
		return scriptLocation;
	}
	
	
	
	
	/**
	 * returns true only if the file is completely written
	 *
	 * @param file
	 *            The file to written
	 * @param htmlString
	 *            The content of the file to be written
	 * @param encoding
	 *            Encoding of the content. Usually utf-8
	 * @return true only if the file is completely written
	 */
	public boolean isCompletelyWritten(File file, String htmlString, String encoding) {
		RandomAccessFile stream = null;
		try {
			stream = new RandomAccessFile(file, "rw");
			byte[] html = htmlString.getBytes(encoding);
			stream.write(html);
			return true;
		} catch (IOException e) {
			logger.info("Skipping file " + file.getName() + " as it's not completely written");
		} finally {
			ApplicationUtilities.closeResource(stream);
		}
		return false;
	}

}