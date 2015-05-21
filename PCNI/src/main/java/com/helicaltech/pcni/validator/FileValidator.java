package com.helicaltech.pcni.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;

/**
 * The various framework related xml files like efw, efwd, and efwvf have a
 * pre-defined file structure in xsd format. The efw files are validated using
 * the xsd file present at System/XSDfile directory using an instance of this
 * class.
 *
 * @author Rajasekhar
 * @author Muqtar Ahmed
 * @version 1.0
 * @since 1.0
 */
public class FileValidator {

	private static final Logger logger = LoggerFactory.getLogger(FileValidator.class);

	/**
	 * The file under concern
	 */
	private String file;

	/**
	 * Getter for the file
	 *
	 * @return The file itself
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Setter method for the file
	 *
	 * @param file
	 *            The file under concern
	 */
	public void setFile(String file) {
		this.file = file;
	}

	/**
	 * Checks whether the file exists physically or not
	 *
	 * @return true if the file physically exists
	 */
	public boolean isFilePresent() {
		boolean fileExist = false;
		File xmlFle = new File(file);
		if (xmlFle.isFile() && xmlFle.exists()) {
			fileExist = true;
		}
		logger.info("The file " + file + " exists.");
		return fileExist;
	}

	/**
	 * Validates the xml file like efw whether it conforms to the corresponding
	 * xsd or not.
	 *
	 * @param xmlFile
	 *            The file under concern
	 * @param xsdPath
	 *            The location of XSDfile in System directory
	 * @return true if the file conforms to the xsd
	 */
	public boolean validateFileUsingXSD(String xmlFile, String xsdPath) {
		boolean isValid = true;
		File xmlToValidate = new File(xmlFile);

		try {
			SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
			File schemaLocation = new File(xsdPath);
			Schema schema = factory.newSchema(schemaLocation);
			Validator validator = schema.newValidator();
			Source source = new StreamSource(xmlToValidate);
			validator.validate(source);
		} catch (SAXException ex) {
			isValid = false;
			logger.error(xmlToValidate + " is not valid", ex);
		} catch (IOException e) {
			logger.error("IOException occurred", e);
			isValid = false;
		}
		return isValid;
	}
}
