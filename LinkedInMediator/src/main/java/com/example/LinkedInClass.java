package com.example;




import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Arrays;

import java.util.Iterator;
import java.util.Map;

public class LinkedInClass extends AbstractMediator {

	public boolean mediate(MessageContext cont) {
		try {
			String type = (String) cont.getProperty("Type");

			System.out.println("In the class");
			if (type.equals("linkedIn")) {
				linkedin(cont);
			
			} else {
				System.out.println("You have entered wrong company name");
				throw new Exception("Please enter valid company");
			}
		} catch (Exception e) {
			System.out.println(
					"Error occure while converting into json object either records are empty or retrival failed");

		}
		return true;
	}

	// TODO Implement your mediation logic here
	public static void linkedin(MessageContext context) {
		try {
			// URL of the API endpoint

			String company = (String) context.getProperty("CompanyName");
			System.out.println(company);
			String apiUrl = "https://linkedin-companies-data.p.rapidapi.com/?vanity_name=" + company;

			// Create a URL object
			URL url = new URL(apiUrl);

			// Open a connection to the URL
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// Set headers (replace with your headers)
			connection.setRequestProperty("X-RapidAPI-Key", "7e97d41d33msh72e73c747becc47p1cb6fejsndd85bf54e01e");
			connection.setRequestProperty("X-RapidAPI-Host", "linkedin-companies-data.p.rapidapi.com");
			connection.setRequestProperty("Content-Type", "application/json");

			// Set request method to GET
			connection.setRequestMethod("GET");

			// Read and print the response content
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();

			// System.out.println("Response Body: " + response.toString());
			JSONObject linkedObject = new JSONObject(response.toString());
			System.out.println(linkedObject.toString());
			JSONArray Output1 = getLinkedinMappingJson(linkedObject);
			JSONArray Output2 = getLinkedinParentMappingJson(linkedObject);
			context.setProperty("Array1", Output1.toString());
			context.setProperty("Array2", Output2.toString());

			// Close the connection
			connection.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public static JSONArray getLinkedinMappingJson(JSONObject jsonObject) {
		JSONArray outputJsonArray = new JSONArray();
		JSONArray custompropertiesArray = new JSONArray();
		JSONObject outputObject = new JSONObject();
		String[] standardFields = { "names", "industry", "about_us", "headquarters", "employees_num", "social_url",
				"founded", "type", "specialties", "employees" };
		outputObject.put("company_name", validatekey("company_name", jsonObject));
		outputObject.put("name", validatekey("names", jsonObject));
		outputObject.put("industry", validatekey("industry", jsonObject));
		outputObject.put("source", "linkedin");
		outputObject.put("headquarters", validatekey("headquarters", jsonObject));
		outputObject.put("employees_num", validatekey("employees_num", jsonObject));
		// outputObject.put("operating_years", jsonObject.getString("employees_num"));
		outputObject.put("about_us", validatekey("about_us", jsonObject));
		outputObject.put("social_url", validatekey("social_url", jsonObject));
		outputObject.put("founded", validatekey("founded", jsonObject));
		outputObject.put("type", validatekey("type", jsonObject));
		outputObject.put("specialties", getSpecialtiesArray(jsonObject.getJSONArray("specialties")));

		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (key.equals("employees")) {
				if (jsonObject.getJSONArray(key).length() > 0) {

					outputObject.put("employer", jsonObject.getJSONArray(key));
				}

			} else if (!Arrays.asList(standardFields).contains(key)) {
				System.out.println(key);
				if (key.contains("categories")) {
					JSONArray Categories = jsonObject.getJSONArray(key);
					if (Categories.length() > 0) {
						for (int i = 0; i < Categories.length(); i++) {

							custompropertiesArray.put(getJSONObject("Categories", Categories.getString(i)));

						}

					}
				} else if (key.contains("locations")) {
					JSONArray locations = jsonObject.getJSONArray(key);
					if (locations.length() > 0) {
						for (int i = 0; i < locations.length(); i++) {

							custompropertiesArray
									.put(getJSONObject("location", validatekey("address", locations.getJSONObject(i))));

						}

					}
				} else if (key.contains("affiliated_companies")) {
					JSONArray affiliated_companies = jsonObject.getJSONArray(key);
					if (affiliated_companies.length() > 0) {
						for (int i = 0; i < affiliated_companies.length(); i++) {

							custompropertiesArray.put(getJSONObject("affiliated_companies",
									validatekey("company_name", affiliated_companies.getJSONObject(i))));

						}

					}
				} else if (key.contains("similar_companies")) {
					JSONArray similar_companies = jsonObject.getJSONArray(key);
					if (similar_companies.length() > 0) {
						for (int i = 0; i < similar_companies.length(); i++) {

							custompropertiesArray.put(getJSONObject("similar_companies",
									validatekey("company_name", similar_companies.getJSONObject(i))));

						}

					}
				} else {

					custompropertiesArray.put(getJSONObject(key, validatekey(key, jsonObject)));
				}

			}
		}
		outputObject.put("custom_properties", custompropertiesArray);
		outputJsonArray.put(outputObject);

		// System.out.println("Source Data:" + outputJsonArray.toString());
		return outputJsonArray;

	}

	public static JSONArray getSpecialtiesArray(JSONArray specialites) {

		if (specialites.length() > 0) {

			JSONArray specialitiesJSONArray = new JSONArray();
			for (int i = 0; i < specialites.length(); i++) {
				JSONObject specialityObject = new JSONObject();
				specialityObject.put("specialities", specialites.getString(i));
				specialitiesJSONArray.put(specialityObject);
			}
			return specialitiesJSONArray;
		} else {
			return new JSONArray();
		}
	}

	public static JSONObject getJSONObject(String key, String value) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", key);
		jsonObject.put("value", value);
		return jsonObject;
	}

	public static String validatekey(String key, JSONObject jsonObject) {

		String result;
		if (jsonObject.has(key)) {
			if (!jsonObject.isNull(key)) {
				result = (String) jsonObject.get(key).toString();
			} else {
				result = "";

			}
		} else {
			result = "";

		}
		return result;
	}

	public static JSONArray getLinkedinParentMappingJson(JSONObject jsonObject) {
		// jsonArray = new JSONArray(jsonPayloadToString);
		JSONArray outputJsonArray = new JSONArray();

		if (!jsonObject.getString("company_name").equals("")) {
			String source[] = validatekey("social_url", jsonObject).split("https://www.linkedin.com/company/");
			jsonObject.put("source", "Linkedin-parent");
			jsonObject.put("Comp_Name", source[1]);
			jsonObject.put("parentName", validatekey("company_name", jsonObject));
			outputJsonArray.put(jsonObject);
		}

		return outputJsonArray;
	}



	public static String checkKey(String key) {
		if (key.equals("null")) {
			return "";
		} else
			return key;
	}
}
