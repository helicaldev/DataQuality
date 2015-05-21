package com.helicaltech.pcni.controller;

//import com.helicaltech.pcni.exceptions.RequiredParametersNotProvidedException;
//import com.helical.efw.export.TempDirectoryCleaner;
//import com.helical.efw.rules.BusinessRulesUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.helicaltech.pcni.exceptions.RequiredParametersNotProvidedException;
//import com.helical.efw.controller.SaveReportController;
import com.helicaltech.pcni.export.TempDirectoryCleaner;
import com.helicaltech.pcni.rules.BusinessRulesUtils;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.useractions.DeleteOperationHandler;
import com.helicaltech.pcni.useractions.ExportOperationHandler;
import com.helicaltech.pcni.useractions.FileArchive;
import com.helicaltech.pcni.useractions.ImportOperationHandler;
//import com.helicaltech.pcni.useractions.ImportOperationHandler;
import com.helicaltech.pcni.useractions.MoveToOperationHandler;
import com.helicaltech.pcni.useractions.NewFolderHandler;
import com.helicaltech.pcni.useractions.RenameOperationHandler;
import com.helicaltech.pcni.utility.ApplicationUtilities;

/**
 * The controller has methods that have mapping for /fileSystemOperations and
 * /importFile. The operations that it can do are newFolder creation, move,
 * delete, export, renaming of files and folders along with importing files.
 * <p/>
 * <p/>
 * A typical setting.xml referenced through out the application will be present
 * in the location System/Admin directory. The System directory is part of the
 * solution directory which consists of the framework related files.
 * <p/>
 * The configuration path of setting.xml can be found in project.properties file
 * in the class path.
 * <p/>
 *
 * @author Rajasekhar
 * @since 1.1
 */
@Component
@Controller
public class FileSystemOperationsController implements ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(FileSystemOperationsController.class);

	/**
	 * The singleton instance of the class ApplicationProperties
	 */
	private ApplicationContext applicationContext;

	/**
	 * The request parameter is of the form array([]). The destination parameter
	 * is a string. The files and folders are relative path names and not
	 * absolute.
	 * <p/>
	 * Along with the response that is either success or failure always a JSP
	 * request attribute 'response' is set for the purpose of the view
	 *
	 * @param sourceArray
	 *            The request parameter that consists of files and folders names
	 * @param action
	 *            The type of action to be completed
	 * @param request
	 *            The http request object
	 * @param response
	 *            The http response object
	 * @return success or failure based on the input processing
	 */
	@RequestMapping(value = "/fileSystemOperations", method = RequestMethod.POST)
	public @ResponseBody String executeFileSystemOperations(@RequestParam("sourceArray") String sourceArray, @RequestParam("action") String action,
			HttpServletRequest request, HttpServletResponse response) {
		logger.debug("Request parameters are sourceArray : " + sourceArray + ", action : " + action);

		if ("newFolder".equalsIgnoreCase(action)) {
			NewFolderHandler newFolderHandler = (NewFolderHandler) applicationContext.getBean("newFolderHandler");
			String folderName = request.getParameter("folderName");
			if ((folderName == null) || "".equals(folderName) || (folderName.trim().length() == 0)) {
				try {
					throw new RequiredParametersNotProvidedException("folderName is null or empty");
				} catch (RequiredParametersNotProvidedException e) {
					logger.error("folderName is null or empty", e);
					request.setAttribute("response", "folderName is null or empty");
					return "failure";
				}
			}
			if (newFolderHandler.handle(sourceArray, folderName)) {
				logger.info("Completed action newFolder");
				request.setAttribute("response", "Completed action newFolder");
				return "success";
			} else {
				logger.error("Couldn't complete action newFolder");
				request.setAttribute("response", "Couldn't complete action newFolder");
				return "failure";
			}
		} else if ("rename".equalsIgnoreCase(action)) {
			RenameOperationHandler renameOperationHandler = (RenameOperationHandler) applicationContext.getBean("renameOperationHandler");
			if (renameOperationHandler.handle(sourceArray)) {
				logger.info("Completed action rename");
				request.setAttribute("response", "Completed action rename");
				return "success";
			} else {
				logger.error("Couldn't complete action rename");
				request.setAttribute("response", "Couldn't complete action rename");
				return "failure";
			}
		} else if ("move".equalsIgnoreCase(action)) {
			MoveToOperationHandler moveToOperationHandler = (MoveToOperationHandler) applicationContext.getBean("moveToOperationHandler");
			String destination = request.getParameter("destination");
			if ((destination == null) || "".equals(destination) || (destination.trim().length() == 0)) {
				try {
					throw new RequiredParametersNotProvidedException("destination is null or empty");
				} catch (RequiredParametersNotProvidedException e) {
					logger.error("destination is null or empty", e);
					request.setAttribute("response", "destination is null or empty");
					return "failure";
				}
			}
			if (moveToOperationHandler.handle(sourceArray, destination)) {
				logger.info("Completed action move");
				request.setAttribute("response", "Completed action move");
				return "success";
			} else {
				logger.error("Couldn't complete action move");
				request.setAttribute("response", "Couldn't complete action move");
				return "failure";
			}
		} else if ("delete".equalsIgnoreCase(action)) {
			DeleteOperationHandler deleteOperationHandler = (DeleteOperationHandler) applicationContext.getBean("deleteOperationHandler");
			if ((sourceArray == null) || "[]".equals(sourceArray) || (sourceArray.trim().length() == 0)) {
				logger.error("sourceArray is null or empty");
				request.setAttribute("response", "sourceArray is null or empty");
				return "failure";
			}
			if (deleteOperationHandler.handle(sourceArray)) {
				logger.info("Completed action delete");
				request.setAttribute("response", "Completed action delete");
				return "success";
			} else {
				logger.error("Couldn't complete action delete");
				request.setAttribute("response", "Couldn't complete action delete");
				return "failure";
			}
		} else if ("export".equalsIgnoreCase(action)) {
			ExportOperationHandler exportOperationHandler = (ExportOperationHandler) applicationContext.getBean("exportOperationHandler");
			if ((sourceArray == null) || "[]".equals(sourceArray) || (sourceArray.trim().length() == 0)) {
				logger.error("sourceArray is null or empty");
				request.setAttribute("response", "sourceArray is null or empty");
				return "failure";
			}
			List<File> listOfFilesToBeZipped = exportOperationHandler.validateSource(sourceArray);
			if (listOfFilesToBeZipped == null) {
				logger.error("Invalid source. Only EFWSR type files are supported for exporting.");
				request.setAttribute("response", "Source is not valid. Only EFWSR type files are supported for exporting.");
				return "failure";
			}
			if (!export(request, response, listOfFilesToBeZipped)) {
				logger.info("Couldn't complete action export");
				request.setAttribute("response", "Couldn't complete action export");
				return "failure";
			} else {
				logger.info("Completed action export");
				request.setAttribute("response", "Completed action export");
				return "success";
			}
		}
		request.setAttribute("response", "Unknown user action. Operation aborted.");
		return "failure";
	}

	/**
	 * Imports a file on to the file system. The destination has to exist on the
	 * file system.
	 * <p/>
	 * Along with the response that is either success or failure always a JSP
	 * request attribute 'response' is set for the purpose of the view.
	 *
	 * @param destination
	 *            The destination of the import
	 * @param request
	 *            The http request object
	 * @return success or failure based on the request processing to avoid 404
	 */
	@RequestMapping(value = "/importFile", method = RequestMethod.POST)
	public @ResponseBody String executeImportOperation(@RequestParam("destination") String destination, HttpServletRequest request) {
		logger.info("destination parameter : " + destination);
		ApplicationProperties applicationProperties = ApplicationProperties.getInstance();
		File destinationFile = new File(applicationProperties.getSolutionDirectory() + File.separator + destination);
//		File destinationFile = new File(destination);
		if (destination == null || ApplicationUtilities.isEmpty(destination) || !destinationFile.exists()) {
			logger.error("The parameter 'destination' value is incorrect. Its value is null or empty or it doesn't exist on the file system.");
			request.setAttribute("response", "The parameter 'destination' value is null or empty or it doesn't exist on the file system.");
			return "failure";
		}

		ImportOperationHandler importOperationHandler = (ImportOperationHandler) applicationContext.getBean("importOperationHandler");
		if (importOperationHandler.isIndexFilePresent(destinationFile)) {
			if (!importOperationHandler.areUserCredentialsMatching(destinationFile)) {
				logger.error("The user is not authorized to save anything in destination directory. Aborting the process.");
				request.setAttribute("response", "The user is not authorized to save anything in directory " + destination);
				return "failure";
			}
		} else {
			logger.info("Index file not found in destination directory. Trying to import into public directory.");
		}

		Object fileObject = request.getAttribute("file");

		if (fileObject == null) {
			logger.error("There is no file to upload! Please select a file to upload");
			request.setAttribute("response", "There is no file to upload! Please select a file to upload.");
			return "failure";
		} else if (fileObject instanceof FileUploadException) {
			FileUploadException fileUploadException = (FileUploadException) fileObject;
			logger.error("Please select a smaller file to upload.", fileUploadException.getMessage());
			request.setAttribute("response", "Please select a smaller file to upload.");
			return "failure";
		}

		String extensionOfFileTypeToBeImported = request.getParameter("fileExtension");
		if (extensionOfFileTypeToBeImported == null) {
//			extensionOfFileTypeToBeImported = new SaveReportController().getEFWSRExtension();
			extensionOfFileTypeToBeImported = "rdf";
		}
		if (!importOperationHandler.processMultipartItem(request, (FileItem) fileObject, destination, extensionOfFileTypeToBeImported)) {
			return "failure";
		}
		logger.info("Successfully completed the action import");
		request.setAttribute("response", "Completed the action import");
		return "success";
	}

	/**
	 * Exports the zip file from the Temp location. The list of files to be
	 * zipped are zipped and stored in the Temp directory
	 *
	 * @param request
	 *            The http request object
	 * @param response
	 *            The http response object
	 * @param listOfFilesToBeZipped
	 *            The list of files to be zipped
	 * @return true or false based on the input processing
	 */
	private boolean export(HttpServletRequest request, HttpServletResponse response, List<File> listOfFilesToBeZipped) {
		logger.debug("listOfFilesToBeZipped : " + listOfFilesToBeZipped);
		File[] files = new File[listOfFilesToBeZipped.size()];
		files = listOfFilesToBeZipped.toArray(files);
		FileArchive fileArchive = new FileArchive();
		String extension = fileArchive.getExtensionOfZipFile();
		logger.debug("Exporting : "+extension);
		if (extension != null) {
			List<String> userDetails = BusinessRulesUtils.getUserDetails();
			String attachmentName = userDetails.get(0) + "_" + System.currentTimeMillis() + "." + extension;
			File fileToBeExported = new File(TempDirectoryCleaner.getTempDirectory().getAbsolutePath() + File.separator + attachmentName);
			if (!fileArchive.archive(fileToBeExported, files)) {
				logger.error("File couldn't be written to the temporary location. Aborting the operation.");
				request.setAttribute("response", "File couldn't be written to the temporary location!");
				return false;
			}
			OutputStream outputStream = null;
			FileInputStream fileInputStream = null;
			try {
				response.setContentType("application/octet-stream");
				response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", attachmentName));
				fileInputStream = new FileInputStream(fileToBeExported);
				outputStream = response.getOutputStream();
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = fileInputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
				// Flush and close the outputStream
				outputStream.flush();
			} catch (IOException e) {
				logger.error("IOException", e);
			} finally {
				ApplicationUtilities.closeResource(fileInputStream);
				ApplicationUtilities.closeResource(outputStream);
			}
		} else {
			logger.error("Improper settings configuration. Couldn't find extension for efwExport.");
			return false;
		}
		return true;
	}

	/**
	 * Sets the property with the application context
	 *
	 * @param applicationContext
	 *            The ApplicationContext of spring
	 * @throws BeansException
	 *             If something goes wrong ):
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
