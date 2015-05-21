package com.helicaltech.pcni.export;

import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.utility.ApplicationUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * An utility class for the Emailing component of the application, which takes
 * care of getting the attachments generated in the Temp directory of the System
 * directory
 *
 * @author Rajasekhar
 * @since 1.1
 */
public class EmailUtility {

	private static final Logger logger = LoggerFactory.getLogger(EmailUtility.class);

	/**
	 * Returns an array of the locations of the attachments. reportsSourceType
	 * 'adhoc' means that the report is to be processed expecting that the
	 * reportSource is html data. Otherwise the reportSource is url. The class
	 * uses the phantom js related api to get the attachments.
	 * <p/>
	 * Note: Request parameter 'data' as in '/exportData' controller method has
	 * to be provided for csv to be processed
	 *
	 * @param formats
	 *            The email attachment formats
	 * @param reportSource
	 *            The html source of the report or the url
	 * @param reportSourceType
	 *            'adhoc' or not 'adhoc'
	 * @param reportName
	 *            The name of the report from the request
	 * @param csvData
	 *            The data related to csv
	 * @return An array of the locations of the attachments
	 * @throws SQLException 
	 */
	//public static String[] getAttachmentsArray(String[] formats, String reportSource, String reportSourceType, String reportName, String csvData,Connection connection,String map) {
	
	public static String[] getAttachmentsArray(String[] formats, String reportSource, String reportSourceType, String reportName, String csvData) throws SQLException {

		logger.debug("Received data : "+csvData);
		String[] attachments = new String[formats.length];

		ReportsProcessor reportsProcessor = new ReportsProcessor();
		 
		int counter = 0;
		List<String> locationsList;
		
		// To pass the HTML source file location for the rest of the reports
		// formats
		String uri = null;
		if ("adhoc".equals(reportSourceType) || reportSourceType == null) {
			// htmlString is provided to get the report
			for (String format : formats) {
				logger.debug("preparing attachment for the format: " + format);
				// The temporary HTML file needs to be generated only once.
				// For the next format the URI can be used
				if (counter == 0) {
					locationsList = reportsProcessor.generateReportUsingHTMLSource(reportSource, format, reportName);
					logger.debug("locationsList = " + locationsList);
					attachments[counter] = locationsList.get(0);
					uri = locationsList.get(1);

					// No need to proceed with the rest of the logic. Continue
					// with the next formats; update the counter.;
					counter++;
					continue;
				}

				/**
				 * Request parameter 'data' as in '/exportData' controller
				 * method has to be provided
				 */
				if ("csv".equalsIgnoreCase(format)) {
					logger.debug("Preparing file csv");
					CSVUtility csvWriter = new CSVUtility();
					//String result = csvWriter.getCSVData(csvData,map,connection);
					String result = csvWriter.getCSVData(csvData);
					logger.debug(csvData+" Fetched csvData going to write "+result);
					File tempCSVFile = new File(ApplicationProperties.getInstance().getSolutionDirectory()+File.separator + "System" + File.separator + "Temp" + File.separator + reportName + ".csv");
					ApplicationUtilities.createAFile(tempCSVFile, result);
					attachments[counter] = tempCSVFile.toString();
					counter++;
					continue;
				}

				logger.debug("HTML file already created. Now, requesting with uri: " + uri);
				locationsList = reportsProcessor.generateReportFromURI(uri, format, reportName);
				attachments[counter] = locationsList.get(0);
				counter++;
			}
		} else {
			// URI with parameters is provided to get the report
			for (String format : formats) {
				if ("csv".equalsIgnoreCase(format)) {
					CSVUtility csvWriter = new CSVUtility();
					String result = csvWriter.getCSVData(csvData);
					logger.debug(csvData+" 2: Fetched csvData going to write "+result);
					File tempCSVFile = new File(ApplicationProperties.getInstance().getSolutionDirectory()+File.separator + "System" + File.separator + "Temp"  + File.separator + reportName + ".csv");
					ApplicationUtilities.createAFile(tempCSVFile, result);
					attachments[counter] = tempCSVFile.toString();
					counter++;
					continue;
				}

				locationsList = reportsProcessor.generateReportFromURI(reportSource, format, reportName);
				attachments[counter] = locationsList.get(0);
				counter++;
			}
		}
		return attachments;
	}
	
	
	// Return the incremented counter after placing the attachment location in
		// the attachments
		public static int insertCsvAttachment(String reportName, String parameterData, String[] attachments, int arrayIndex) throws SQLException {
			CSVUtility csvWriter = new CSVUtility();
			String result = csvWriter.getCSVData(parameterData);
			File tempCSVFile = new File(TempDirectoryCleaner.getTempDirectory() + File.separator + reportName + ".csv");
			ApplicationUtilities.createAFile(tempCSVFile, result);
			if (logger.isDebugEnabled()) {
				logger.debug("CSV file " + (tempCSVFile.exists() ? "created exists." : "is not created."));
			}
			attachments[arrayIndex] = tempCSVFile.toString();
			arrayIndex++;
			return arrayIndex;
		}
}
