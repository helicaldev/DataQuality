package com.helicaltech.pcni.export;

import com.helicaltech.pcni.utility.ApplicationUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;

/**
 * The class is able to send e-mail messages to a single recipient or multiple
 * recipients with single or multiple attachments.
 *
 * @author Sharad Sinha
 * @version 1.1
 * @since 1.1
 */
public class SendMail {

	private static final Logger logger = LoggerFactory.getLogger(SendMail.class);

	/**
	 * This method sends email without attachments. Uses the Java Mail API.
	 *
	 * @param hostName
	 *            a <code>String</code> which specifies SMTP host name
	 * @param port
	 *            a <code>String</code> which specifies SMTP port number
	 * @param recipients
	 *            a <code>String</code> array which specifies recipients
	 * @param from
	 *            a <code>String</code> which specifies the sender
	 * @param isAuthenticated
	 *            a <code>String</code> which specifies value true or false
	 * @param isSSLEnabled
	 *            a <code>String</code> which specifies value true or false
	 * @param user
	 *            The user who is sending mail
	 * @param passCode
	 *            The password of the user who is sending the mail
	 * @param subject
	 *            a <code>String</code> which specifies the subject of mail
	 * @param body
	 *            a <code>String</code> which specifies body of mail
	 */
	public void sendMessage(String hostName, String port, String[] recipients, String from, String isAuthenticated, String isSSLEnabled, String user,
			String passCode, String subject, String body) {
		logger.info("Sending mail message....");
		final String userName = user;
		final String password = passCode;
		Properties properties = new Properties();
		properties.put("mail.smtp.user", user);
		properties.put("mail.smtp.auth", isAuthenticated);
		properties.put("mail.debug", "true");
		properties.put("mail.smtp.starttls.enable", isSSLEnabled);
		properties.put("mail.smtp.host", hostName);
		properties.put("mail.smtp.port", port);

		Session session = Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});
		session.setDebug(false);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setSentDate(new Date());
			InternetAddress[] toMailAddressArray = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				toMailAddressArray[i] = new InternetAddress(recipients[i]);
			}
			message.addRecipients(Message.RecipientType.TO, toMailAddressArray);

			message.setSubject(subject);
			message.setContent(body, "text/html; charset=" + ApplicationUtilities.getEncoding());
			Transport.send(message);
			logger.info("Message Sent Successfully!");
		} catch (MessagingException ex) {
			logger.error("MessagingException occurred", ex);
			ex.printStackTrace();
		} catch (Exception ex) {
			logger.error("Exception occurred", ex);
			ex.printStackTrace();
		}
	}

	/**
	 * This method can send emails with attachments. Uses the Java Mail API.
	 *
	 * @param hostname
	 *            a <code>String</code> which specifies SMTP host name
	 * @param port
	 *            a <code>String</code> which specifies SMTP port number
	 * @param recipients
	 *            a <code>String</code> array which specifies recipients
	 * @param from
	 *            a <code>String</code> which specifies the sender
	 * @param isAuthenticated
	 *            a <code>String</code> which specifies value true or false
	 * @param isSSLEnabled
	 *            a <code>String</code> which specifies value true or false
	 * @param user
	 *            The user who is sending mail
	 * @param passCode
	 *            The password of the user who is sending the mail
	 * @param subject
	 *            a <code>String</code> which specifies the subject of mail
	 * @param body
	 *            a <code>String</code> which specifies body of mail
	 * @param attachments
	 *            a <code>String[]</code> specify attachments which has to be
	 *            send with mail
	 */
	public void sendMessage(String hostname, String port, String[] recipients, String from, String isAuthenticated, String isSSLEnabled, String user,
			String passCode, String subject, String body, String[] attachments) {
		logger.info("Sending mail message with attachment....");
		final String userName = user;
		final String password = passCode;
		Properties properties = new Properties();
		properties.put("mail.smtp.user", user);
		properties.put("mail.smtp.auth", isAuthenticated);
		properties.put("mail.smtp.starttls.enable", isSSLEnabled);
		properties.put("mail.debug", "true");
		properties.put("mail.smtp.host", hostname);
		properties.put("mail.smtp.port", port);

		Session session = Session.getInstance(properties, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});
		session.setDebug(false);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setSubject(subject);
			message.setSentDate(new Date());

			logger.debug("recipients are: ");
			InternetAddress[] toMailAddressArray = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++) {
				toMailAddressArray[i] = new InternetAddress(recipients[i]);
				logger.debug(recipients[i]);
			}

			message.addRecipients(Message.RecipientType.TO, toMailAddressArray);
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(body, "text/html; charset=" + ApplicationUtilities.getEncoding());

			Multipart multiPart = new MimeMultipart();
			multiPart.addBodyPart(messageBodyPart);

			messageBodyPart = new MimeBodyPart();
			for (String attachment : attachments) {
				MimeBodyPart attachmentPart = new MimeBodyPart();
				attachmentPart.attachFile(attachment);
				multiPart.addBodyPart(attachmentPart);
			}

			message.setContent(multiPart);
			Transport.send(message);
			logger.info("Message Sent Successfully!");
		} catch (MessagingException ex) {
			logger.error("MessagingException occurred", ex);
			ex.printStackTrace();
		} catch (Exception ex) {
			logger.error("Exception occurred", ex);
			ex.printStackTrace();
		}
	}
}
