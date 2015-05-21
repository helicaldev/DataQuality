package com.helicaltech.pcni.scheduling;

import com.helicaltech.pcni.rules.BusinessRulesUtils;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * XmlOperationWithParser is use to do xml Operation using dom parser
 *
 * @author Prashansa
 * @version 1.1
 */
public class XmlOperationWithParser {
	private static final Logger logger = Logger.getLogger(XmlOperationWithParser.class);

	XmlOperation xmlOperation = new XmlOperation();

	/**
	 * <p>
	 * addNewJobInExistingXML() is responsible to add new schedule tag or add
	 * new job in existing xml.
	 * </p>
	 *
	 * @param newData
	 *            a <code>JSONObject</code>
	 * @param path
	 *            a <code>String</code> specify path of scheduling.xml
	 * @param id
	 *            a <code>int</code> specify id
	 * @return
	 */
	public String addNewJobInExistingXML(net.sf.json.JSONObject newData, String path, int id) {
		logger.debug("Inside addNewJobInExistingXML");
		List<String> userDetail = new ArrayList<String>();
		userDetail = BusinessRulesUtils.getUserDetails();
		logger.debug("UsreName: " + userDetail);
		logger.debug("USERDETAIL SIZE:" + userDetail.size());
		List<String> key = new ArrayList<String>();
		key = xmlOperation.findKey(newData);
		logger.debug("key.size():  " + key.size());
		String data = "";
		String idd = String.valueOf(id);
		logger.debug("idd:  " + idd);
		try {
			logger.debug("Start creating XML");
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(path);
			Node schedules = doc.getElementsByTagName("Schedules").item(0);
			Element schedule = doc.createElement("Schedule");
			Element SchedulingJob = doc.createElement("SchedulingJob");
			Element Security = doc.createElement("Security");

			Element LastExecutedOn = doc.createElement("LastExecutedOn");
			Element LastExecutionStatus = doc.createElement("LastExecutionStatus");
			Element NextExecutionOn = doc.createElement("NextExecutionOn");
			Element NoOfExecutions = doc.createElement("NoOfExecutions");

			schedule.setAttribute("id", idd);
			SchedulingJob.setAttribute("type", "EFW");
			for (int keycount = 0; keycount < key.size(); keycount++) {
				logger.debug("Inside Loop");
				Element newTag = doc.createElement(key.get(keycount));
				logger.debug("Creating XML Node");
				if (key.get(keycount).equalsIgnoreCase("ScheduleOptions")) {
					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createCDATASection(data));
					schedule.appendChild(newTag);
				} else if (key.get(keycount).equalsIgnoreCase("EmailSettings")) {
					String Formats = "";
					String Recipients = "";
					String Subject = "";
					String Body = "";
					String Zip = "";
					Element FormatsTag = doc.createElement("Formats");
					Formats = newData.getJSONObject("EmailSettings").getString("Formats");
					FormatsTag.appendChild(doc.createCDATASection(Formats));
					Element RecipientsTag = doc.createElement("Recipients");
					Recipients = newData.getJSONObject("EmailSettings").getString("Recipients");
					RecipientsTag.appendChild(doc.createCDATASection(Recipients));
					Element SubjectTag = doc.createElement("Subject");
					Subject = newData.getJSONObject("EmailSettings").getString("Subject");
					SubjectTag.appendChild(doc.createCDATASection(Subject));
					Element BodyTag = doc.createElement("Body");
					Body = newData.getJSONObject("EmailSettings").getString("Body");
					BodyTag.appendChild(doc.createCDATASection(Body));

					if (newData.getJSONObject("EmailSettings").containsKey("Zip")) {
						Element ZipTag = doc.createElement("Zip");
						Zip = newData.getJSONObject("EmailSettings").getString("Zip");
						ZipTag.appendChild(doc.createCDATASection(Zip));
						newTag.appendChild(ZipTag);
					}
					newTag.appendChild(FormatsTag);
					newTag.appendChild(RecipientsTag);
					newTag.appendChild(SubjectTag);
					newTag.appendChild(BodyTag);

					SchedulingJob.appendChild(newTag);
					schedule.appendChild(SchedulingJob);

				} else if (key.get(keycount).equalsIgnoreCase("ReportDirectory") || key.get(keycount).equalsIgnoreCase("ReportFile")) {
					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createTextNode(data));
					SchedulingJob.appendChild(newTag);
					schedule.appendChild(SchedulingJob);
				} else if (key.get(keycount).equalsIgnoreCase("reportParameters")) {
					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createCDATASection(data));
					SchedulingJob.appendChild(newTag);
					schedule.appendChild(SchedulingJob);
				} else if (key.get(keycount).equalsIgnoreCase("security")) {
					String Username = "";
					String Organization = "";
					Element UserTag = doc.createElement("CreatedBy");
					Element organizationTag = doc.createElement("Organization");
					Username = newData.getJSONObject("Security").getString("CreatedBy");
					Organization = newData.getJSONObject("Security").getString("Organization");
					organizationTag.appendChild(doc.createTextNode(Organization));
					UserTag.appendChild(doc.createTextNode(Username));
					Security.appendChild(UserTag);
					Security.appendChild(organizationTag);
					schedule.appendChild(Security);
				} else {

					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createTextNode(data));
					schedule.appendChild(newTag);

					LastExecutedOn.appendChild(doc.createTextNode("0"));
					LastExecutionStatus.appendChild(doc.createTextNode("0"));
					NextExecutionOn.appendChild(doc.createTextNode("0"));
					NoOfExecutions.appendChild(doc.createTextNode("0"));
					schedule.appendChild(LastExecutedOn);
					schedule.appendChild(LastExecutionStatus);
					schedule.appendChild(NextExecutionOn);
					schedule.appendChild(NoOfExecutions);

				}

			}
			schedules.appendChild(schedule);
			logger.debug("Schedules: " + schedules);
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path));
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
		return idd;
	}

	/**
	 * addNewJobInXML() is responsible to create new xml file in given path and
	 * add new job.
	 *
	 * @param newData
	 *            a <code>JSONObject</code>
	 * @param path
	 *            a <code>String</code> specify path of scheduling.xml
	 * @return
	 */
	public void addNewJobInXML(net.sf.json.JSONObject newData, String path) {
		logger.debug("Inside addNewJobInXML");
		List<String> key = new ArrayList<String>();
		key = xmlOperation.findKey(newData);
		logger.debug("key.size:  " + key.size());
		String data = "";
		try {
			File file = new File(path);
			logger.debug("creating new File");
			file.createNewFile();
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element schedules = doc.createElement("Schedules");
			Element schedule = doc.createElement("Schedule");
			Element schedule1 = doc.createElement("Schedule");
			Element SchedulingJob = doc.createElement("SchedulingJob");
			Element Security = doc.createElement("Security");

			Element LastExecutedOn = doc.createElement("LastExecutedOn");
			Element LastExecutionStatus = doc.createElement("LastExecutionStatus");
			Element NextExecutionOn = doc.createElement("NextExecutionOn");
			Element NoOfExecutions = doc.createElement("NoOfExecutions");

			schedule.setAttribute("id", "1");
			schedule1.setAttribute("id", "0");
			SchedulingJob.setAttribute("type", "EFW");
			logger.debug("SchedulingJob: " + SchedulingJob);
			for (int keycount = 0; keycount < key.size(); keycount++) {
				logger.debug("Inside loop:  ");
				Element newTag = doc.createElement(key.get(keycount));
				if (key.get(keycount).equalsIgnoreCase("ScheduleOptions")) {
					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createCDATASection(data));
					schedule.appendChild(newTag);
				} else if (key.get(keycount).equalsIgnoreCase("EmailSettings")) {
					String Formats = "";
					String Recipients = "";
					String Subject = "";
					String Body = "";
					String Zip = "";
					Element FormatsTag = doc.createElement("Formats");
					Formats = newData.getJSONObject("EmailSettings").getString("Formats");
					FormatsTag.appendChild(doc.createCDATASection(Formats));
					Element RecipientsTag = doc.createElement("Recipients");
					Recipients = newData.getJSONObject("EmailSettings").getString("Recipients");
					RecipientsTag.appendChild(doc.createCDATASection(Recipients));
					Element SubjectTag = doc.createElement("Subject");
					Subject = newData.getJSONObject("EmailSettings").getString("Subject");
					SubjectTag.appendChild(doc.createCDATASection(Subject));
					Element BodyTag = doc.createElement("Body");
					Body = newData.getJSONObject("EmailSettings").getString("Body");
					BodyTag.appendChild(doc.createCDATASection(Body));
					if (newData.getJSONObject("EmailSettings").containsKey("zip")) {
						Element ZipTag = doc.createElement("Zip");
						Zip = newData.getJSONObject("EmailSettings").getString("Zip");
						ZipTag.appendChild(doc.createCDATASection(Zip));
						newTag.appendChild(ZipTag);
					}
					newTag.appendChild(FormatsTag);
					newTag.appendChild(RecipientsTag);
					newTag.appendChild(SubjectTag);
					newTag.appendChild(BodyTag);

					SchedulingJob.appendChild(newTag);
					schedule.appendChild(SchedulingJob);
					logger.debug("schedule:  " + schedule);
				} else if (key.get(keycount).equalsIgnoreCase("ReportDirectory") || key.get(keycount).equalsIgnoreCase("ReportFile")) {
					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createTextNode(data));
					SchedulingJob.appendChild(newTag);
					schedule.appendChild(SchedulingJob);
				} else if (key.get(keycount).equalsIgnoreCase("reportParameters")) {
					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createCDATASection(data));
					SchedulingJob.appendChild(newTag);
					schedule.appendChild(SchedulingJob);
				} else if (key.get(keycount).equalsIgnoreCase("security")) {
					String Username = "";
					String Organization = "";
					Element UserTag = doc.createElement("CreatedBy");
					Element organizationTag = doc.createElement("Organization");
					Username = newData.getJSONObject("Security").getString("CreatedBy");
					Organization = newData.getJSONObject("Security").getString("Organization");
					organizationTag.appendChild(doc.createTextNode(Organization));
					UserTag.appendChild(doc.createTextNode(Username));
					Security.appendChild(UserTag);
					Security.appendChild(organizationTag);
					schedule.appendChild(Security);
				} else {

					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createTextNode(data));
					schedule.appendChild(newTag);

					LastExecutedOn.appendChild(doc.createTextNode("0"));
					LastExecutionStatus.appendChild(doc.createTextNode("0"));
					NextExecutionOn.appendChild(doc.createTextNode("0"));
					NoOfExecutions.appendChild(doc.createTextNode("0"));
					schedule.appendChild(LastExecutedOn);
					schedule.appendChild(LastExecutionStatus);
					schedule.appendChild(NextExecutionOn);
					schedule.appendChild(NoOfExecutions);

				}

			}
			schedules.appendChild(schedule);
			schedules.appendChild(schedule1);
			logger.debug("schedules:  " + schedules);
			doc.appendChild(schedules);
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path));
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * remove <Schedule> node from XML of id given in parameter.
	 *
	 * @param path
	 *            a <code>String</code> specify the path of xml
	 * @param id
	 *            a <Code>String</code> specify schedule id for delete.
	 */
	public void removeElementFromXml(String path, String id) {
		logger.debug("id: " + id + "  is going to delete from schedule.xml");
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(path);
			Node schedules = doc.getFirstChild();
			logger.debug(schedules.getNodeName());
			NodeList list = schedules.getChildNodes();
			logger.debug(list.getLength());
			for (int nodecount = 0; nodecount < list.getLength(); nodecount++) {

				Node node = list.item(nodecount);
				if (node.getNodeName().equals("Schedule")) {
					NamedNodeMap attr = node.getAttributes();
					Node nodeAttr = attr.getNamedItem("id");
					logger.debug("nodeAttribute: " + nodeAttr.getTextContent());
					if (nodeAttr.getTextContent().equals(id)) {
						node.getParentNode().removeChild(node);
					}
				}

			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			doc.normalize();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path));
			transformer.transform(source, result);

			logger.debug("Data deleted from schedule xml");

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Unused method
	 *
	 * @param path
	 * @return
	 */
	public boolean readXml(String path) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		boolean hasChildNode = true;
		try {
			docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(path);
			Node schedules = doc.getFirstChild();
			logger.debug(schedules.getNodeName());
			logger.debug("has chield node:  " + schedules.hasChildNodes());
			hasChildNode = schedules.hasChildNodes();
			NodeList list = schedules.getChildNodes();
			logger.debug(list.getLength());
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hasChildNode;
	}

	/**
	 * updateJobInExistingXML() is responsible for update existing
	 * scheduling.xml
	 *
	 * @param newData
	 *            a <code>JSONObject</code>
	 * @param path
	 *            a <code>String</code> specify scheduling.xml path
	 * @param id
	 *            a <code>int</code> specify id which is going to modify.
	 */
	public void updateJobInExistingXML(net.sf.json.JSONObject newData, String path, int id) {
		logger.debug("Inside addNewJobInExistingXML");
		List<String> userDetail = new ArrayList<String>();
		userDetail = BusinessRulesUtils.getUserDetails();
		logger.debug("UsreName: " + userDetail);
		logger.debug("USERDETAIL SIZE:" + userDetail.size());
		List<String> key = new ArrayList<String>();
		key = xmlOperation.findKey(newData);
		logger.debug("key.size():  " + key.size());
		String data = "";
		String idd = String.valueOf(id);
		logger.debug("idd:  " + idd);
		try {
			logger.debug("Start creating XML");
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(path);
			Node schedules = doc.getElementsByTagName("Schedules").item(0);
			Element schedule = doc.createElement("Schedule");
			Element SchedulingJob = doc.createElement("SchedulingJob");
			Element Security = doc.createElement("Security");

			schedule.setAttribute("id", idd);
			SchedulingJob.setAttribute("type", "EFW");
			for (int keycount = 0; keycount < key.size(); keycount++) {
				logger.debug("Inside Loop");
				Element newTag = doc.createElement(key.get(keycount));
				logger.debug("Creating XML Node");
				if (key.get(keycount).equalsIgnoreCase("ScheduleOptions")) {
					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createCDATASection(data));
					schedule.appendChild(newTag);
				} else if (key.get(keycount).equalsIgnoreCase("EmailSettings")) {
					String Formats = "";
					String Recipients = "";
					String Subject = "";
					String Body = "";
					String Zip = "";
					Element FormatsTag = doc.createElement("Formats");
					Formats = newData.getJSONObject("EmailSettings").getString("Formats");
					FormatsTag.appendChild(doc.createCDATASection(Formats));
					Element RecipientsTag = doc.createElement("Recipients");
					Recipients = newData.getJSONObject("EmailSettings").getString("Recipients");
					RecipientsTag.appendChild(doc.createCDATASection(Recipients));
					Element SubjectTag = doc.createElement("Subject");
					Subject = newData.getJSONObject("EmailSettings").getString("Subject");
					SubjectTag.appendChild(doc.createCDATASection(Subject));
					Element BodyTag = doc.createElement("Body");
					Body = newData.getJSONObject("EmailSettings").getString("Body");
					BodyTag.appendChild(doc.createCDATASection(Body));

					if (newData.getJSONObject("EmailSettings").containsKey("Zip")) {
						Element ZipTag = doc.createElement("Zip");
						Zip = newData.getJSONObject("EmailSettings").getString("Zip");
						ZipTag.appendChild(doc.createCDATASection(Zip));
						newTag.appendChild(ZipTag);
					}
					newTag.appendChild(FormatsTag);
					newTag.appendChild(RecipientsTag);
					newTag.appendChild(SubjectTag);
					newTag.appendChild(BodyTag);

					SchedulingJob.appendChild(newTag);
					schedule.appendChild(SchedulingJob);

				} else if (key.get(keycount).equalsIgnoreCase("ReportDirectory") || key.get(keycount).equalsIgnoreCase("ReportFile")) {
					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createTextNode(data));
					SchedulingJob.appendChild(newTag);
					schedule.appendChild(SchedulingJob);
				} else if (key.get(keycount).equalsIgnoreCase("reportParameters")) {
					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createCDATASection(data));
					SchedulingJob.appendChild(newTag);
					schedule.appendChild(SchedulingJob);
				} else if (key.get(keycount).equalsIgnoreCase("security")) {
					String Username = "";
					String Organization = "";
					Element UserTag = doc.createElement("CreatedBy");
					Element organizationTag = doc.createElement("Organization");
					Username = newData.getJSONObject("Security").getString("CreatedBy");
					Organization = newData.getJSONObject("Security").getString("Organization");
					organizationTag.appendChild(doc.createTextNode(Organization));
					UserTag.appendChild(doc.createTextNode(Username));
					Security.appendChild(UserTag);
					Security.appendChild(organizationTag);
					schedule.appendChild(Security);
				} else {

					data = newData.getString(key.get(keycount));
					newTag.appendChild(doc.createTextNode(data));
					schedule.appendChild(newTag);
				}

			}
			schedules.appendChild(schedule);
			logger.debug("Schedules: " + schedules);
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path));
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
	}

	/**
	 * <p>
	 * updateExistingXml() method is responsible for update
	 * EndAfterExecutions,NoOfExecutions node which is child node of Schedule
	 * node in scheduling.xml
	 * <p/>
	 * </p>
	 *
	 * @param jsonObject
	 * @param path
	 * @param id
	 * @see ScheduleJob
	 */
	public void updateExistingXml(JSONObject jsonObject, String path, String id) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(path);

			// Get the root element
			Node Schedules = doc.getFirstChild();

			// loop the Schedules child node
			NodeList list = Schedules.getChildNodes();

			for (int i = 0; i < list.getLength(); i++) {

				Node node = list.item(i);
				NodeList nodeList = node.getChildNodes();
				if (node.getNodeName().equals("Schedule")) {
					NamedNodeMap attr1 = node.getAttributes();
					Node nodeAttr1 = attr1.getNamedItem("id");
					if (nodeAttr1.getTextContent().equals(id)) {
						for (int nodeCount = 0; nodeCount < nodeList.getLength(); nodeCount++) {
							XmlOperation xmlOperation = new XmlOperation();
							List<String> keyList = new ArrayList<String>();
							keyList = xmlOperation.findKey(jsonObject);
							for (int keyCount = 0; keyCount < keyList.size(); keyCount++) {
								if (nodeList.item(nodeCount).getNodeName().equalsIgnoreCase(keyList.get(keyCount))) {
									nodeList.item(nodeCount).setTextContent(jsonObject.getString(keyList.get(keyCount)));
								}
							}
						}

					}
				}
			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(path));
			transformer.transform(source, result);

			JSONObject jsonObject1 = new JSONObject();
			XmlOperation xmlOperation = new XmlOperation();
			jsonObject1 = xmlOperation.getParticularObject(path, id);
			logger.debug("jsonObject1:  " + jsonObject1);
			if (jsonObject1.getJSONObject("ScheduleOptions").containsKey("EndAfterExecutions")) {
				String EndAfterExecutions = jsonObject1.getJSONObject("ScheduleOptions").getString("EndAfterExecutions");
				String NoOfExecutionsNoOfExecutions = jsonObject1.getString("NoOfExecutions");
				if (NoOfExecutionsNoOfExecutions.equalsIgnoreCase(EndAfterExecutions)
						&& Integer.parseInt(NoOfExecutionsNoOfExecutions) >= Integer.parseInt(EndAfterExecutions)) {
					ScheduleProcess schedulerProcess = new ScheduleProcess();
					schedulerProcess.delete(id);
				}
			}

			logger.debug("Done");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException sae) {
			sae.printStackTrace();
		}
	}
}
