package com.helicaltech.pcni.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.helicaltech.pcni.export.EmailUtility;
import com.helicaltech.pcni.export.ReportsUtility;
import com.helicaltech.pcni.export.SendMail;
import com.helicaltech.pcni.utils.ConfigurationFileReader;


@Controller
@Component
public class EmailController {
	
	private final static Logger logger = LoggerFactory.getLogger(EmailController.class);
	
	/**
	 * Sends email with the attachments in the requested formats to the desired
	 * recipients. The request parameters 'body' of the email, 'subject' of the
	 * email and the 'data' for csv attachment are optional; If provided they
	 * will be used.
	 *
	 * @param formats
	 *            The request parameter - formats of the attachments of the
	 *            dashboard
	 * @param recipients
	 *            The request parameter - recipients of the email
	 * @param reportSource
	 *            The request parameter - reportSource i.e. either html source
	 *            or URL
	 * @param reportSourceType
	 *            The request parameter - reportSourceType which tells whether
	 *            html is supplied or URL is supplied
	 * @param request
	 *            HttpServletRequest object
	 * @return Returns a static string to avoid 404
	 * @throws IOException 
	 */
	
	@RequestMapping(value = "/sendMail", method = RequestMethod.POST)
	public @ResponseBody String sendMail(@RequestParam("formats") String formats, @RequestParam("recipients") String recipients,
			@RequestParam("reportSource") String reportSource, @RequestParam("reportSourceType") String reportSourceType, HttpServletRequest request) throws SQLException, AddressException, MessagingException, IOException {

		logger.debug("Request Parameters: " + ", recipients: " + recipients + ", formats: " + formats + ", reportSourceType: " + reportSourceType+"  Map: "+request.getParameter("map"));

		ConfigurationFileReader propertiesFileReader = new ConfigurationFileReader();

		// Read the properties file in the EFW/System/Mail directory
		Map<String, String> propertiesMap = propertiesFileReader.read("Mail","mailConfiguration.properties");

		logger.debug("properties file map " + propertiesMap);

		Assert.notNull(propertiesMap, "The mailConfiguration.properties map is null!!");

		String hostName = propertiesMap.get("hostName");
		String port = propertiesMap.get("port");
		String from = propertiesMap.get("from");
		String isAuthenticated = propertiesMap.get("isAuthenticated");
		String isSSLEnabled = propertiesMap.get("isSSLEnabled");
		String user = propertiesMap.get("user");
		String password = propertiesMap.get("password");

		// Get body from the request parameter if present
		String body = getBody(request, propertiesMap);

		String[] totalFormats = sanitize(formats);
		logger.debug("The email formats are " + Arrays.asList(totalFormats));
		String[] totalRecipients = sanitize(recipients);

		if (logger.isInfoEnabled()) {
			logger.info((((totalFormats.length == 0) || ("[\"\"]".equals(formats))) ? "Not preparing " : "Preparing") + " attachments for the mail.");
		}
		
		
		// Get reportName from the request parameter if present
				String reportName = ReportsUtility.getReportName(request.getParameter("reportName"));

				// Get subject from the request parameter if present
				String subject = getSubject(request, propertiesMap, reportName);

				SendMail mailClient = new SendMail();
				String[] attachments = null;
				if ((totalFormats.length == 0) || ("[\"\"]".equals(formats))) {
					mailClient.sendMessage(hostName, reportName, totalRecipients, from, isAuthenticated, isSSLEnabled, user, password, subject, body);
				} else {
					// Send mail to all the recipients with all the attachments
					String csvData = request.getParameter("data");
					if ((totalFormats.length == 1) && (totalFormats[0].equalsIgnoreCase("csv"))) {
						if (csvData == null) {
							throw new RuntimeException("Couldn't process request for csv attachment. Failed to send email message.");
						}
						attachments = new String[1];
						EmailUtility.insertCsvAttachment(reportName, csvData, attachments, 0);
						try {
							send(hostName, port, from, isAuthenticated, isSSLEnabled, user, password, body, totalRecipients, subject, mailClient, attachments);
						} catch (AddressException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (MessagingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						attachments = EmailUtility.getAttachmentsArray(totalFormats, reportSource, reportSourceType, reportName, csvData);
						send(hostName, port, from, isAuthenticated, isSSLEnabled, user, password, body, totalRecipients, subject, mailClient, attachments);
					}
				}
				
//		// Clean the quotes, '[' and ']' from the first and last elements of
//		// the array
//		String[] totalFormats = formats.substring(1, formats.length() - 1).replace("\"", "").split(",");
//
//		// Clean quotes, '[' and ']' from the first and last elements of the
//		// array
//		String[] totalRecipients = recipients.substring(1, recipients.length() - 1).replace("\"", "").split(",");
//
//		// Get reportName from the request parameter if present
//		String reportName = ReportsUtility.getReportName(request.getParameter("reportName"));
//
//		// Get subject from the request parameter if present
//		String subject;
//		if (request.getParameter("subject") == null) {
//			logger.debug("subject is not provided. Using default subject");
//			subject = propertiesMap.get("subject") + reportName;
//		} else {
//			subject = request.getParameter("subject");
//		}
//		String csvData = request.getParameter("data");
//		
//		
//		//Connection connections = connectionProvider.getConnection("jdbc/dqDatabase");
//		
////		String[] attachments = EmailUtility.getAttachmentsArray(totalFormats, reportSource, reportSourceType, reportName, csvData,connections,request.getParameter("map"));
//		
//		String[] attachments = EmailUtility.getAttachmentsArray(totalFormats, reportSource, reportSourceType, reportName,csvData);
//	//	DbUtils.closeQuietly(connections);
//		
//		SendMail mailClient = new SendMail();
//		// Send mail to all the recipients with all the attachments
//		mailClient.sendMessage(hostName, port, totalRecipients, from, isAuthenticated, isSSLEnabled, user, password, subject, body, attachments);
			
		/**
		 * Write some string to the response body to avoid 404
		 */
		return "success";
	}
	
	private String getSubject(HttpServletRequest request, Map<String, String> propertiesMap, String reportName) {
		String subject;
		String subjectParameter = request.getParameter("subject");
		if (subjectParameter == null) {
			logger.debug("Subject is not provided. Using default subject from properties file.");
			subject = propertiesMap.get("subject") + reportName;
		} else {
			subject = subjectParameter;
		}
		return subject;
	}

	private String getBody(HttpServletRequest request, Map<String, String> propertiesMap) {
		String body;
		String bodyParameter = request.getParameter("body");
		if (bodyParameter == null) {
			logger.debug("body is not provided. Using default body for the email");
			body = propertiesMap.get("body");
		} else {
			body = bodyParameter;
		}
		return body;
	}

	private void send(String hostName, String port, String from, String isAuthenticated, String isSSLEnabled, String user, String password,
			String body, String[] totalRecipients, String subject, SendMail mailClient, String[] attachments) throws AddressException, MessagingException, IOException {
		mailClient.sendMessage(hostName, port, totalRecipients, from, isAuthenticated, isSSLEnabled, user, password, subject, body, attachments);
	}

	private String[] sanitize(String formats) {
		JSONArray formatsArray = null;
		try {
			formatsArray = (JSONArray) JSONSerializer.toJSON(formats);
		} catch (JSONException ex) {
			logger.error("JSONException : " + ex);
			throw new RuntimeException("Attachment formats is in unsupported format. Failed to send mail.");
		}
		Iterator<?> iterator = formatsArray.iterator();
		String[] totalFormats = new String[formatsArray.size()];
		try {
			int counter = 0;
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				totalFormats[counter] = key;
				counter++;
			}
		} catch (JSONException ex) {
			logger.error("Formats is not an array", ex);
			throw new RuntimeException("Attachment formats is in unsupported format. Failed to send mail.");
		}
		return totalFormats;
	}

}
