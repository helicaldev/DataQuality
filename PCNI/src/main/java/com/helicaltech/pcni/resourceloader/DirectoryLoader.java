package com.helicaltech.pcni.resourceloader;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.helicaltech.pcni.rules.interfaces.IFolderRule;
import com.helicaltech.pcni.singleton.ApplicationProperties;
import com.helicaltech.pcni.rules.BusinessRulesFactory;
import com.helicaltech.pcni.rules.interfaces.IBusinessRule;
import com.helicaltech.pcni.utility.ApplicationUtilities;
import com.helicaltech.pcni.login.LoginForm;
import com.helicaltech.pcni.resourceloader.JSONProcessor;

import java.io.File;
import java.util.*;

/**
 * This class is responsible for loading all the solution directory(EFW). The
 * files and folders information is put in a json format and sent to the caller.
 * The json excludes System and Images directories.
 *
 * @author Rajasekhar
 * @version 1.1
 * @since 1.0
 */
public class DirectoryLoader {

	private static final Logger logger = LoggerFactory.getLogger(DirectoryLoader.class);


	/**
	 * Member of type ApplicationProperties
	 */
	private final ApplicationProperties applicationProperties=ApplicationProperties.getInstance();
	
	/**
	 * Instantiates the settingsLoader property
	 */
	public DirectoryLoader() {
		// settingsLoader = new SettingsLoader();
	}

	/**
	 * Returns the content of the solution directory
	 *
	 * @param rootPath
	 *            Usually EFW solution directory
	 * @param visibleExtensions
	 *            The set of extensions which are supposed to be visible in the
	 *            dashboard
	 * @return The content of the solution directory
	 */
	public List<Map<String, String>> getSolutionDirectory(String rootPath) {

		// return getFoldersAndFiles(rootPath);
		return null;
	}

	/**
	 * Added to read files info from XML
	 */
//
//	public String getFoldersAndFiles(String rootPath) {
//		rootPath += "/System/templateContext.xml";
//		logger.debug("Collecting data from path : " + rootPath);
//		
//		JSONProcessor processor = new JSONProcessor();
//		JSONObject ruleExtensionFileJSON;
//		try {
//			ruleExtensionFileJSON = processor.getJSON(rootPath, false);
//		} catch (ClassCastException ex) {
//			ruleExtensionFileJSON = processor.getJSON(rootPath, true);
//		}
//
//		logger.debug(ruleExtensionFileJSON.toString());
//		// Assert.notNull(files, "files object is null!. No directories!");
//
//		return ruleExtensionFileJSON.toString();
//	}

	/**
	 * Prepares the list of solution directory content
	 *
	 * @param rootPath
	 *            Usually EFW solution directory
	 * @return The content of the solution directory
	 */

	  public List<Map<String, String>> getFoldersAndFiles(String rootPath) {
	  
	  File directory = new File(rootPath); 
	  File[] files = directory.listFiles(); 
	  // Just for	  printing purpose this list is created 
	  List<File> filesAndFolders = Arrays.asList(files); 
	  logger.debug("The files and folders are " + filesAndFolders);
	  
	  Assert.notNull(files, "files object is null!. No directories!");
	  
	  // call the method only if the files and folders size is greater than //  zero
	  
	  return prepareListOfMaps(files,rootPath); 
	  
	  }
	 
	
	private List<Map<String, String>> prepareListOfMaps(File[] files,String rootPath) {
		List<Map<String, String>> listOfFoldersAndFiles = new ArrayList<Map<String, String>>();
		
		for (File file : files) {
			if (file.isFile()) {
				//logger.debug("Inside file");
				// Split the file into its name and extension and get them in an
				// array
				
				String[] fileNameAndExtensionArray = file.getName().split("\\.(?=[^\\.]+$)");
				String actualFileExtension;
				BusinessRulesFactory rulesFactory = new BusinessRulesFactory();
				String rule="";
				if (fileNameAndExtensionArray.length <= 1) {
					logger.debug("The file " + file + " has no extension!");
					continue;
				} else {
					actualFileExtension = fileNameAndExtensionArray[fileNameAndExtensionArray.length - 1];
				}
				
		
				
						if(actualFileExtension.equalsIgnoreCase("html")){
							
								listOfFoldersAndFiles.add(include(file, "efw"));
							}
						else if(actualFileExtension.equalsIgnoreCase("rdf"))
						{
							String extensionKey="efwsr";
							
							rule="com.helicaltech.pcni.rules.EFWSRRuleValidator";
							IBusinessRule businessRule = rulesFactory.getBusinessRuleImplementation(rule);
							logger.debug("rule class = " + businessRule);
							if (businessRule.validateRule(file, extensionKey)) {
								logger.debug("Validating file  "+extensionKey);
								listOfFoldersAndFiles.add(getResourceMap(file,extensionKey));
							}
							
						} else if(actualFileExtension.equalsIgnoreCase("result"))
						{
							// Add user name from seesion
							if(file.getName().startsWith(LoginForm.getInstance().getjUserName())){
								String extensionKey="efwresult";
								
								//listOfFoldersAndFiles.add(include(file, actualFileExtension));
								rule="com.helicaltech.pcni.rules.EFWSRRuleValidator";
								
								IBusinessRule businessRule = rulesFactory.getBusinessRuleImplementation(rule);
								logger.debug("rule class = " + businessRule);
								if (businessRule.validateRule(file, extensionKey)) {
								listOfFoldersAndFiles.add(getResourceMap(file,extensionKey));
								}
							}
							
						}
						else if(actualFileExtension.equalsIgnoreCase("fav"))
						{
								String extensionKey="efwfav";
								
								//listOfFoldersAndFiles.add(include(file, actualFileExtension));
								rule="com.helicaltech.pcni.rules.EFWSRRuleValidator";
								IBusinessRule businessRule = rulesFactory.getBusinessRuleImplementation(rule);
								logger.debug("rule class = " + businessRule);
								if (businessRule.validateRule(file, extensionKey)) {
									listOfFoldersAndFiles.add(getResourceMap(file,extensionKey));
								}
							
						}
//						else if(actualFileExtension.equalsIgnoreCase("efwfolder")){
//						//	BusinessRulesFactory rulesFactory = new BusinessRulesFactory();
//							rule= "com.helicaltech.pcni.rules.EFWFolderRuleValidator";
//							IFolderRule businessRule;
//							JSONProcessor processor = new JSONProcessor();
//							JSONObject ruleExtensionFileJSON = processor.getJSON(file.toString(), false);
//							if (rule != null) {
//									businessRule = rulesFactory.getBusinessRuleImplementation(rule);
//									performRuleSpecificFolderAction(listOfFoldersAndFiles, file, ruleExtensionFileJSON.getString("title"));
//							}
//						}
			
			} else if (file.isDirectory() && !file.getName().equalsIgnoreCase("system") && !file.getName().equalsIgnoreCase("images")
					&& (file.listFiles().length > 0)) {
				boolean isPublicDirectory = true;
				
					File[] contents = file.listFiles();
					String actualFileExtension;
					for (File theFile : contents) {
				
						if(!theFile.isDirectory()) {
							
							String[] fileNameAndExtensionArray = theFile.getName().split("\\.(?=[^\\.]+$)");
							if(fileNameAndExtensionArray!= null && fileNameAndExtensionArray.length > 1){
								
								actualFileExtension = fileNameAndExtensionArray[1];
								JSONProcessor processor = new JSONProcessor();
								
								if(actualFileExtension.equalsIgnoreCase("efwfolder")){
									isPublicDirectory = false;
									BusinessRulesFactory rulesFactory = new BusinessRulesFactory();
									String ruleClass= "com.helicaltech.pcni.rules.EFWFolderRuleValidator";
									IFolderRule businessRule;
									
									if (ruleClass != null) {
										JSONObject ruleExtensionFileJSON = processor.getJSON(theFile.toString(), false);	
										businessRule = rulesFactory.getBusinessRuleImplementation(ruleClass);
										if (businessRule.validateRule(ruleExtensionFileJSON)) {
											performRuleSpecificFolderAction(listOfFoldersAndFiles, file, ruleExtensionFileJSON.getString("title"));
											//isPublicDirectory = true;
										}
									}
								}
							
							}
						}
					}
					if (isPublicDirectory) {
						performDefaultFolderAction(listOfFoldersAndFiles, file,rootPath);
						}
					}
				}
				
		
		
		return listOfFoldersAndFiles;
	}
	
	
	private void performDefaultFolderAction(List<Map<String, String>> listOfFoldersAndFiles, File file,String rootPath) {
		Map<String, String> foldersMap = new HashMap<String, String>();
//		String relativePath = file.getAbsolutePath();
		String relativePath = getRelativeSolutionPath(file.getAbsolutePath());
		foldersMap.put("type", "folder");
		foldersMap.put("name", file.getName());
		foldersMap.put("path", relativePath);
		foldersMap.put("children", ApplicationUtilities.getJSONArray(getFoldersAndFiles(file.getAbsolutePath())));
		listOfFoldersAndFiles.add(foldersMap);
	}
	
	private Map<String, String> include(File file, String extensionKey) {
		Map<String, String> foldersMap = new HashMap<String, String>();
		logger.debug("Absolute path is {} ", file.getAbsolutePath());
		//String relativePath = file.getAbsolutePath();
		String relativePath = getRelativeSolutionPath(file.getAbsolutePath());
		foldersMap.put("type", "file");
		foldersMap.put("extension", extensionKey);
		foldersMap.put("name", file.getName());
		
		foldersMap.put("path", relativePath);
		foldersMap.put("visible", "true");
		foldersMap.put("description", file.getName());
		//foldersMap.put("title", file.getName());
		foldersMap.put("title", file.getName().split("\\.(?=[^\\.]+$)")[0]);
		return foldersMap;
	}
	
/*	
	private void performRuleSpecificFolderAction(List<Map<String, String>> listOfFoldersAndFiles, File directory, String title) {
		Map<String, String> foldersMap = new HashMap<String, String>();
		String relativePath = directory.getAbsolutePath();
		foldersMap.put("type", "file");
		foldersMap.put("name", title);
		foldersMap.put("path", relativePath);
		foldersMap.put("options", new JSONObject().accumulate("selectable", "true").toString());
		logger.debug("Passing value to getFilesAndFolders " + directory);
		foldersMap.put("children", ApplicationUtilities.getJSONArray(getFoldersAndFiles(directory.getAbsolutePath())));
		listOfFoldersAndFiles.add(foldersMap);
	}
*/	
	private Map<String, String> getResourceMap(File file,String extensionKey)
	{
//		String relativePath =file.getAbsolutePath();
		String relativePath = getRelativeSolutionPath(file.getAbsolutePath());
		String fileName = file.getName();
		if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			fileName = fileName.substring(fileName.lastIndexOf(".")+1);
		else
			fileName = "";
		JSONProcessor processor = new JSONProcessor();
		Map<String, String> foldersMap = new HashMap<String, String>();
		JSONObject visibleExtensionXMLJSONObject = processor.getJSON(file.getAbsolutePath(), false);
		foldersMap.put("type", "file");
		foldersMap.put("extension", extensionKey);
		foldersMap.put("name", file.getName());
		foldersMap.put("path", relativePath);
		foldersMap.put("fileFormat", "CSV");
		/*
		 * Get the contents of the visible file into the map
		 */
		Iterator<?> keys = visibleExtensionXMLJSONObject.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			
			if (key.equals("reportName")) {
				
				foldersMap.put("title", visibleExtensionXMLJSONObject.getString(key));
				foldersMap.put("description", file.getName());
				foldersMap.put("options", new JSONObject().accumulate("selectable", "true").toString());
				continue;
			}
			foldersMap.put(key.toLowerCase(), visibleExtensionXMLJSONObject.getString(key));
		}
		return foldersMap;
	}
	
	
	/**
	 * Returns the relative location of the resource in the EFW solution
	 * directory
	 *
	 * @param path
	 *            The absolute path of the resource being passed
	 * @return Returns the relative location of the resource in the EFW
	 *         directory
	 */
	public String getRelativeSolutionPath(String path) {
		String filePath = new File(path).getAbsolutePath();
		String settingsPath = new File(applicationProperties.getSolutionDirectory()).getAbsolutePath();
		if (filePath.startsWith(settingsPath)) {
			return filePath.substring(settingsPath.length() + 1);
		}
		return null;
	}
	
	/**
	 * Applies rule specific action. Meta data of the directory in the json is
	 * accordingly prepared.
	 *
	 * @param listOfFoldersAndFiles
	 *            The listOfFoldersAndFiles
	 * @param directory
	 *            The directory under concern
	 * @param title
	 *            The title of the file
	 */
	private void performRuleSpecificFolderAction(List<Map<String, String>> listOfFoldersAndFiles, File directory, String title) {
		Map<String, String> foldersMap = new HashMap<String, String>();
		String relativePath = getRelativeSolutionPath(directory.getAbsolutePath());
		foldersMap.put("type", "folder");
		foldersMap.put("name", title);
		foldersMap.put("path", relativePath);
		foldersMap.put("options", new JSONObject().accumulate("selectable", "true").toString());
		foldersMap.put("children", ApplicationUtilities.getJSONArray(getFoldersAndFiles(directory.getAbsolutePath())));
		listOfFoldersAndFiles.add(foldersMap);
	}

}