package com.example;

import com.bordercloud.sparql.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ZeniaClass extends AbstractMediator {

	public boolean mediate(MessageContext cont) {
		try {
			String type = (String) cont.getProperty("Type");

			System.out.println("In the class");
			if (type.equals("linkedIn")) {
				linkedin(cont);
			} else if (type.equals("dbpedia")) {
				dbpedia(cont);
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
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

	public static void dbpedia(MessageContext context) {
		try {
			String company = (String) context.getProperty("CompanyName");
			System.out.println(company);
			URI endpoint = new URI("https://dbpedia.org/sparql/");
			String querySelect = "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
					+ "        PREFIX dbp: <http://dbpedia.org/property/>\n"
					+ "        PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "\n"
					+ "        SELECT ?founded ?industry ?headquarter ?locationCity ?numberOfEmployees ?companyType ?rdfsLabel ?foafName ?dbpName ?abstract ?employer\n"
					+ "        WHERE {\n" + "        OPTIONAL{ <http://dbpedia.org/resource/" + company
					+ "> dbo:abstract ?abstract. FILTER (LANGMATCHES(LANG(?abstract), \"en\"))}\n"
					+ "        OPTIONAL{ <http://dbpedia.org/resource/" + company + "> dbo:foundingDate ?founded.}\n"
					+ "        OPTIONAL{ <http://dbpedia.org/resource/" + company + "> dbo:industry ?industry.}\n"
					+ "        OPTIONAL{ <http://dbpedia.org/resource/" + company + "> dbo:headquarter ?headquarter.}\n"
					+ "        OPTIONAL{ <http://dbpedia.org/resource/" + company + "> dbo:type ?companyType.}\n"
					+ "        OPTIONAL{ <http://dbpedia.org/resource/" + company
					+ "> dbo:locationCity ?locationCity.}\n" + "        OPTIONAL{ <http://dbpedia.org/resource/"
					+ company + "> dbo:numberOfEmployees ?numberOfEmployees.}\n"
					+ "        OPTIONAL{ ?employer dbo:employer <http://dbpedia.org/resource/" + company + ">.}\n"
					+ "        OPTIONAL{ <http://dbpedia.org/resource/" + company
					+ "> rdfs:label ?rdfsLabel. FILTER (LANGMATCHES(LANG(?rdfsLabel), \"en\"))}\n"
					+ "        OPTIONAL{ <http://dbpedia.org/resource/" + company
					+ "> foaf:name ?foafName. FILTER (LANGMATCHES(LANG(?foafName), \"en\"))}\n"
					+ "        OPTIONAL{ <http://dbpedia.org/resource/" + company
					+ "> dbp:name ?dbpName. FILTER (LANGMATCHES(LANG(?dbpName), \"en\"))}\n" + "        }";
		//	System.out.println(querySelect);
			SparqlClient sc = new SparqlClient(false);
			sc.setEndpointRead(endpoint);
			SparqlResult sr = sc.query(querySelect);

			// Check if there are results
			if (sr.getModel().getRowCount() > 0) {
				JSONArray resultsArray = new JSONArray();
				Map<String, ArrayList<String>> columnArrays = new HashMap<>();
				JSONObject resultObject = new JSONObject();

				for (HashMap<String, Object> row : sr.getModel().getRows()) {

					for (String variable : sr.getModel().getVariables()) {
						String value = String.valueOf(row.get(variable));

						// Check if the variable already exists in the columnArrays map
						if (columnArrays.containsKey(variable)) {

							ArrayList<String> duplicate = columnArrays.get(variable);
							if (duplicate.contains(value)) {
								System.out.println("value already there");

							} else {
								duplicate.add(value);
								columnArrays.put(variable, duplicate);
							}
						} else {
							ArrayList<String> single = new ArrayList<String>();
							single.add(value);
							columnArrays.put(variable, single);
						}
					}

					// Add the arrays to the resultObject

				}
				JSONArray customArray = new JSONArray();
				if (columnArrays.size() > 0) {
					for (Map.Entry<String, ArrayList<String>> entry : columnArrays.entrySet()) {
						if (entry.getValue().size() == 1) {
							if (entry.getKey().contains("locationCity")) {
								for (int Listdata = 0; Listdata < entry.getValue().size(); Listdata++) {

									JSONObject multi = new JSONObject();
									multi.put("name", "locationCity");
									multi.put("value",
											checkKey(entry.getValue().get(Listdata)).replace("http://dbpedia.org/resource/", ""));
									customArray.put(multi);

									// }

								}
							}
							else {
							resultObject.put(entry.getKey(),
									checkKey(entry.getValue().get(0)).replace("http://dbpedia.org/resource/", ""));
							}
						}

						else if (entry.getValue().size() > 1) {

							JSONArray multiArray = new JSONArray();

							if (entry.getKey().contains("locationCity")) {
								for (int Listdata = 0; Listdata < entry.getValue().size(); Listdata++) {

									JSONObject multi = new JSONObject();
									multi.put("name", "locationCity");
									multi.put("value",
											checkKey(entry.getValue().get(Listdata)).replace("http://dbpedia.org/resource/", ""));
									customArray.put(multi);

									// }

								}
							}
							else {

							for (int Listdata = 0; Listdata < entry.getValue().size(); Listdata++) {

								JSONObject multi = new JSONObject();
								multi.put(entry.getKey(),
										checkKey(entry.getValue().get(Listdata)).replace("http://dbpedia.org/resource/", ""));
								multiArray.put(multi);

							}
							resultObject.put(entry.getKey(), multiArray);

						}
							}
					}
					resultObject.put("source", "dbpedia");
					resultObject.put("name", company);
					resultObject.put("URI", "<http://dbpedia.org/resource/".concat(company));
					resultObject.put("custom_properties", customArray);
					resultsArray.put(resultObject);
				} else {
					System.out.println("No data in result array");
				}
				// Convert the JSON array to a JSON string
				/// String jsonString = resultsArray.toString(2); // Indent for pretty printing
				JSONArray empOutput = getdbpediaContactMappingJson(company);
				context.setProperty("Array1", resultsArray.toString(2));
				context.setProperty("Array2", empOutput.toString(2));
				// Print the JSON string
				// System.out.println(jsonString);
			}
		} catch (URISyntaxException | SparqlClientException e) {
			System.out.println(e);
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

	public static JSONArray getdbpediaContactMappingJson(String company) {
		JSONArray resultsArray = new JSONArray();
		try {
			URI endpoint = new URI("https://dbpedia.org/sparql/");
			String querySelect = "SELECT ?person ?dbpName ?rdfslabel ?foafName  ?dbp_occupation ?info\r\n" + "where\r\n"
					+ "{\r\n" + "	?person dbo:employer <http://dbpedia.org/resource/" + company + ">.\r\n"
					+ "OPTIONAL{ ?person dbp:name ?dbpName FILTER (LANGMATCHES(LANG(?dbpName ), \"en\"))}\r\n"
					+ "OPTIONAL{ ?person rdfs:label ?rdfslabel FILTER (LANGMATCHES(LANG(?rdfslabel), \"en\"))}\r\n"
					+ "OPTIONAL{ ?person foaf:name ?foafName FILTER (LANGMATCHES(LANG(?foafName), \"en\"))}\r\n"
					+ "OPTIONAL{ ?person dbp:occupation ?dbp_occupation }\r\n"
					+ "OPTIONAL{ ?person dbo:abstract ?info FILTER (LANGMATCHES(LANG(?info), \"en\"))}\r\n" + "}";
			SparqlClient sc = new SparqlClient(false);
			sc.setEndpointRead(endpoint);
			SparqlResult sr = sc.query(querySelect);

			// Check if there are results
			if (sr.getModel().getRowCount() > 0) {

				for (HashMap<String, Object> row : sr.getModel().getRows()) {
					JSONObject resultObject = new JSONObject();
					for (String variable : sr.getModel().getVariables()) {
						String value = String.valueOf(row.get(variable));
						if (variable.contains("foafName")) {
							resultObject.put("contact Name", checkKey(value));
						} else if (variable.contains("dbp_occupation")) {

							resultObject.put("Occupation", checkKey(value));
						} else if (variable.contains("dbpName") || variable.contains("rdfslabel")) {
							if (variable.contains("dbpName") && !checkKey(value).equals(""))
								resultObject.put("foafName", checkKey(value));
							else if (variable.contains("rdfslabel") && !checkKey(value).equals(""))
								resultObject.put("foafName", checkKey(value));
						} else {
							resultObject.put(variable, checkKey(value));
						}
					}
					resultObject.put("Name",
							String.valueOf(row.get("person")).replace("http://dbpedia.org/resource/", ""));
					resultObject.put("source","Dbpedia-contact");
					resultsArray.put(resultObject);
				}

				;
			}

		} catch (URISyntaxException | SparqlClientException e) {
			System.out.println(e);
			e.printStackTrace();
		}
		return resultsArray;
	}

	public static String checkKey(String key) {
		if (key.equals("null")) {
			return "";
		} else
			return key;
	}
}
