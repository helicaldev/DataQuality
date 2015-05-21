package com.helicaltech.pcni.export;

import com.helicaltech.pcni.utility.ApplicationUtilities;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The instance of this class is a separate thread which generates the pdf or
 * png or jpeg formats on the file system.
 *
 * @author Rajasekhar
 * @since 1.0
 */
public class PhantomJS implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(PhantomJS.class);

	/**
	 * The location of the phantom js binary
	 */
	private String phantomLocation;

	/**
	 * The file used by phantom js to create a screenshot
	 */
	private String inputFileString;

	/**
	 * The output of the phantom js on the file system(Temp directory)
	 */
	private String destinationFileString;

	/**
	 * The location of the screenshot.js file
	 */
	private String scriptLocation;

	/**
	 * Constructs an object of this class by setting all the required fields
	 *
	 * @param phantomLocation
	 *            The location of the phantom js binary
	 * @param scriptLocation
	 *            The location of the sreenshot.js file
	 * @param inputFileString
	 *            The file used by phantom js to create a screenshot
	 * @param destinationFileString
	 *            The output of the phantom js on the file system(Temp
	 *            directory)
	 */
	public PhantomJS(String phantomLocation, String scriptLocation, String inputFileString, String destinationFileString) {
		this.inputFileString = inputFileString;
		this.destinationFileString = destinationFileString;
		this.scriptLocation = scriptLocation;
		this.phantomLocation = phantomLocation;
	}

	/**
	 * The url decoded string
	 *
	 * @param urlEncodedString
	 *            The string under concern which is supposed to be url encoded
	 * @param encoding
	 *            The encoding; usually utf-8
	 * @return the url decoded string
	 */
	private String urlDecode(String urlEncodedString, String encoding) {
		try {
			return URLDecoder.decode(urlEncodedString, encoding);
		} catch (UnsupportedEncodingException e) {
			logger.error("Character encoding is not supported " + encoding);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * true if the string is url encoded. Otherwise false
	 *
	 * @param string
	 *            The string under concern which is supposed to be url encoded
	 * @return true if the string is url encoded
	 */
	private boolean isStringURLEncoded(String string) {
		Pattern pattern = Pattern.compile("[%]");
		Matcher matcher = pattern.matcher(string);
		return matcher.find();
	}

	/**
	 * Creates an operating system process by invoking the phantom js. The
	 * thread which invokes this thread will be temporarily resumed till this
	 * process completes. The phantom js creates the screen shot of the html
	 * from a file on the file system.
	 */
	private void create() {
		Process process;

		/*
		 * Fixed the bug 'parse error' in case the strings passed to the command
		 * line consist of url encoding.
		 */
		String encoding = ApplicationUtilities.getEncoding();

		if (isStringURLEncoded(phantomLocation)) {
			phantomLocation = urlDecode(phantomLocation, encoding);
		}

		if (isStringURLEncoded(scriptLocation)) {
			scriptLocation = urlDecode(scriptLocation, encoding);
		}

		if (isStringURLEncoded(inputFileString)) {
			inputFileString = urlDecode(inputFileString, encoding);
		}

		if (isStringURLEncoded(destinationFileString)) {
			destinationFileString = urlDecode(destinationFileString, encoding);
		}

		File input = new File(inputFileString);
		if (input.exists()) {
			logger.info("input html File exists.");
		} else {
			logger.info("input html File doesn't exists.");
		}

		InputStream inputStream = null;

		InputStream errorStream = null;

		logger.info("phantom call = " + phantomLocation + " " + scriptLocation + " " + inputFileString + " " + destinationFileString);

		List<String> command = new ArrayList<String>();
		command.add(0, phantomLocation);
		command.add(1, scriptLocation);
		command.add(2, inputFileString);
		command.add(3, destinationFileString);
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			process = processBuilder.start();

			// Read the output of Phantom JS and also the error stream for
			// errors
			inputStream = process.getInputStream();
			errorStream = process.getErrorStream();

			logger.info("phantom message is " + IOUtils.toString(inputStream, encoding));

			logger.info("phantom error message is " + IOUtils.toString(errorStream, encoding));

			process.waitFor();
		} catch (IOException e) {
			logger.error("IOException during phantom call", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException during phantom call", e);
			e.printStackTrace();
		} finally {
			ApplicationUtilities.closeResource(inputStream);
			ApplicationUtilities.closeResource(errorStream);
		}

		logger.info("phantom js has completed writing file");

		logger.info("Thread.currentThread() = " + Thread.currentThread() + " finished execution. Returning exit value.");

		if (new File(destinationFileString).exists()) {
			logger.info("pdfFile exists.");
		} else {
			logger.info("File doesn't exists.");
		}
	}

	/**
	 * Starts the phantom thread
	 */
	@Override
	public void run() {
		create();
	}
}
