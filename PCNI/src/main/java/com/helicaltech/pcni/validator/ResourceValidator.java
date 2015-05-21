package com.helicaltech.pcni.validator;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * An instance of this class is used to validate the EFWD and EFWVF files
 *
 * @author Rajasekhar
 * @author Muqtar Ahmed
 * @author Avi
 * @since 1.0
 */
public class ResourceValidator {

	private static final Logger logger = LoggerFactory.getLogger(ResourceValidator.class);
	/**
	 * The json of the file under concern
	 */
	private final JSONObject jsonObject;

	/**
	 * Constructs the object of the same type
	 *
	 * @param jsonObject
	 *            The json of the file under concern
	 */
	public ResourceValidator(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	/**
	 * Returns false if the connection id is duplicate or if the dataMap id is
	 * duplicated
	 *
	 * @return false if the connection id is duplicate or if the dataMap id is
	 *         duplicated
	 */
	public boolean validateEwd() {
		JSONArray dataSources = jsonObject.getJSONArray("DataSources");
		JSONArray dataMaps = jsonObject.getJSONArray("DataMaps");
		ArrayList<Integer> list = new ArrayList<Integer>();
		ArrayList<Integer> arrayList = new ArrayList<Integer>();

		for (int i = 0; i < dataSources.size(); i++) {
			int id = dataSources.getJSONObject(i).getInt("@id");
			if (list.contains(id)) {
				return false;
			} else {
				list.add(id);
			}
		}

		for (int dataMap = 0; dataMap < dataMaps.size(); dataMap++) {
			int id = dataMaps.getJSONObject(dataMap).getInt("@id");
			if (arrayList.contains(id)) {
				return false;
			} else {
				arrayList.add(id);
			}
		}
		logger.info("EFWD file is validated.");
		return true;
	}

	/**
	 * Returns false if the vf file consists of duplicate value for id
	 *
	 * @return Return false if there exists a duplicate id in the charts
	 */
	public boolean validateVf() {
		JSONArray charts = jsonObject.getJSONArray("Charts");
		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int chart = 0; chart < charts.size(); chart++) {
			int id = charts.getJSONObject(chart).getInt("@id");
			if (list.contains(id)) {
				return false;
			} else {
				list.add(id);
			}
		}
		logger.info("VF file is validated.");
		return true;
	}
}
