package com.example;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.commons.json.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.json.JSONException;
import org.apache.synapse.core.axis2.Axis2MessageContext;

public class ZoomInfoClass extends AbstractMediator {

	public boolean mediate(MessageContext context) {
		String source = (String) context.getProperty("source");
		System.out.println("In zoominfo class");
		System.out.println(source);
		// TODO Implement your mediation logic here
		if (source.equals("zoominfo")) {
			JSONArray result = Zoominfo(context);
			context.setProperty("Array1", result.toString());
		} else if (source.equals("Zoominfo-parent")) {
			JSONArray result = parentZoomInfo(context);
			context.setProperty("Array2", result.toString());
		}
		return true;

	}

	public JSONArray Zoominfo(MessageContext context) {
		String jsonPayloadToString = JsonUtil
				.jsonPayloadToString(((Axis2MessageContext) context).getAxis2MessageContext());
		// JSONObject requestBody=XML.toJSONObject(payload);
		JSONArray jsonArray = new JSONArray(jsonPayloadToString);
		String[] standardFields = { "Company Name", "Primary Industry", "Employees", "ZoomInfo Company Profile URL",
				"Founded Year", "Company Country", "Ownership Type" };

		// List standardFieldsList = Arrays.asList(standardFields);
		JSONArray outputJsonArray = new JSONArray();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			JSONObject outputObject = new JSONObject();
			JSONArray sicArray = new JSONArray();
			JSONArray naicsArray = new JSONArray();
			JSONArray custompropertiesArray = new JSONArray();
			outputObject.put("Company Name", checkKey("Company Name", jsonObject));
			// outputObject.put("industry", jsonObject.getString("industry"));
			outputObject.put("source", "zoominfo");
			outputObject.put("Company Country", checkKey("Company Country", jsonObject));
			outputObject.put("Employees", checkKey("Employees", jsonObject));
			outputObject.put("Primary Industry", checkKey("Primary Industry", jsonObject));
			outputObject.put("ZoomInfo Company Profile URL", checkKey("ZoomInfo Company Profile URL", jsonObject));
			outputObject.put("Founded Year", checkKey("Founded Year", jsonObject));
			outputObject.put("Ownership Type", checkKey("Ownership Type", jsonObject));
			// String specialities="";

			Iterator<String> keys = jsonObject.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				if (key.contains("SIC Code ")) {

					JSONObject sicObject = new JSONObject();

					if (!jsonObject.get(key).equals("")) {
						sicObject.put("sic", checkKey(key, jsonObject));
						sicArray.put(sicObject);
					}
				} else if (key.contains("NAICS Code ")) {
					JSONObject naicsObject = new JSONObject();

					if (!jsonObject.get(key).equals("")) {
						naicsObject.put("naics", checkKey(key, jsonObject));
						naicsArray.put(naicsObject);
					}
				}

				else if (!Arrays.asList(standardFields).contains(key)) {
					if (!jsonObject.getString(key).equals(""))
						custompropertiesArray.put(getJSONObject(key, checkKey(key, jsonObject)));
				}
			}
			outputObject.put("sic", sicArray);
			outputObject.put("naics", naicsArray);
			// outputObject.put("industry", industryArray);

			outputObject.put("custom_properties", custompropertiesArray);
			outputJsonArray.put(outputObject);
		}
		System.out.println("Array1 completed");

		return outputJsonArray;

	}

	public JSONArray parentZoomInfo(MessageContext context) {
		String jsonPayloadToString = JsonUtil
				.jsonPayloadToString(((Axis2MessageContext) context).getAxis2MessageContext());
		// JSONObject requestBody=XML.toJSONObject(payload);

		JSONArray jsonArray = new JSONArray(jsonPayloadToString);
		// String[] standardFields={"Company Name","parentName"};

		// List standardFieldsList = Arrays.asList(standardFields);
		JSONArray outputJsonArray = new JSONArray();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			JSONObject outputObject = new JSONObject();
			// JSONArray sicArray=new JSONArray();
			// JSONArray naicsArray=new JSONArray();
//			JSONArray custompropertiesArray=new JSONArray();
			System.out.println(jsonObject.toString());
			outputObject.put("Company Name", checkKey("Company Name", jsonObject));
			outputObject.put("parentName", checkKey("parentName", jsonObject));
			outputObject.put("source", "zoominfo-parent");
					outputJsonArray.put(outputObject);
		}
		System.out.println("Array2 completed");

		return outputJsonArray;

	}

	public JSONObject getJSONObject(String key, String value) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", key);
		jsonObject.put("value", value);
		return jsonObject;
	}

	public static String checkKey(String key, JSONObject jsonObject) {
		if (jsonObject.has(key)) {
			return jsonObject.getString(key);
		} else
			return "";
	}
}