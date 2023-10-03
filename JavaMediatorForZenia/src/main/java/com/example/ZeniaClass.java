package com.example;



import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONObject;

import com.bordercloud.sparql.SparqlClient;
import com.bordercloud.sparql.SparqlClientException;
import com.bordercloud.sparql.SparqlResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
			}else if (type.equals("yahoo-finance")) {
				yahoo(cont);
			} 
			else {
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
		//	System.out.println(linkedObject.toString());
			JSONArray Output1 = getLinkedinMappingJson(linkedObject);
			JSONArray Output2 = getLinkedinParentMappingJson(linkedObject, context);
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
							if (!duplicate.contains(value)) {

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
				context.setProperty("parentData", parentNameData(resultObject, "foafName"));
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
	
	public static void yahoo(MessageContext context) {
		 try {
			 String	outputArray;
				String	outputParentArray;
			 String symbol = (String) context.getProperty("symbol");
        	 String line, url;
             url = "https://api.zeniagraph.ai/graphql";
             CloseableHttpClient client = null;
             CloseableHttpResponse response = null;

             client = HttpClientBuilder.create().build();
             HttpPost httpPost = new HttpPost(url);
             httpPost.addHeader("Content-Type", "application/json");
             httpPost.addHeader("Accept", "");
             
//             Map<String, Object> query = new HashMap<String, Object>();
//             Map<String, Object> queryMap = new HashMap<String, Object>();
//             queryMap.put("input", "AAPL");
//             queryMap.put("source", "yahoo_finance");
//             queryMap.put("type", "company");
//
//             query.put("data", queryMap);
            String query = "query getCompaniesByCrawl($data:CompanyListCrawl!)\r\n"
            		+ "{\r\n"
            		+ "    getCompaniesByCrawl(data:$data)\r\n"
            		+ "    {\r\n"
            		+ "        name\r\n"
            		+ "        no_of_employees\r\n"
            		+ "        ticker_symbol\r\n"
            		+ "        website\r\n"
            		+ "        industry\r\n"
            		+ "        quarterly_revenue_growth\r\n"
            		+ "        current_year_revenue\r\n"
            		+ "        previous_year_revenue\r\n"
            		+ "        total_assets\r\n"
            		+ "        gross_profit\r\n"
            		+ "        market_cap\r\n"
            		+ "        last_quarterly_revenue\r\n"
            		+ "        second_last_quarterly_revenue\r\n"
            		+ "        description\r\n"
            		+ "        parent_name\r\n"
            		+ "    } \r\n"
            		+ "}";

            Map<String, Object> variables = new HashMap<String, Object>();
            Map<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("input", symbol);
            dataMap.put("source", "yahoo_finance");
            dataMap.put("type", "company");

            variables.put("data", dataMap);

            // Create an ObjectMapper instance to convert variables to JSON
//            ObjectMapper objectMapper = new ObjectMapper();
//            String variablesJson = objectMapper.writeValueAsString(variables);

            JSONObject jsonobj = new JSONObject();
            jsonobj.put("query", query);
            jsonobj.put("variables", variables);

            StringEntity entity = new StringEntity(jsonobj.toString());
 
            httpPost.setEntity(entity);
            response = client.execute(httpPost);

            BufferedReader bufReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder builder = new StringBuilder();

            while ((line = bufReader.readLine()) != null) {
                builder.append(line);
                builder.append(System.lineSeparator());
            }
            // ...
          //  System.out.println(builder.toString());
            JSONObject yahooObject = new JSONObject(builder.toString());
            JSONObject inputObject=yahooObject.getJSONObject("data").getJSONObject("getCompaniesByCrawl");
            outputArray=getYahooWithoutNodeJson(inputObject,context);
			outputParentArray=getYahooWithNodeJson(inputObject);
			context.setProperty("Array1", outputArray);
			context.setProperty("Array2", outputParentArray);

        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static JSONArray getLinkedinParentMappingJson(JSONObject jsonObject,MessageContext context) {
		// jsonArray = new JSONArray(jsonPayloadToString);
		JSONArray outputJsonArray = new JSONArray();

		if (!jsonObject.getString("company_name").equals("")) {
			String source[] = validatekey("social_url", jsonObject).split("https://www.linkedin.com/company/");
			jsonObject.put("source", "Linkedin-parent");
			jsonObject.put("Comp_Name", source[1]);
			jsonObject.put("parentName", validatekey("company_name", jsonObject));
			outputJsonArray.put(jsonObject);
			context.setProperty("parentData",parentNameData(jsonObject,"parentName"));
			
			
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
	
	
	public static String getYahooWithoutNodeJson(JSONObject jsonObject,MessageContext context) {
		JSONArray outputJsonArray=new JSONArray();
		JSONObject outputJsonObject=new JSONObject();
		outputJsonObject.put("tickerSymbol", validatekey("ticker_symbol",jsonObject));
		outputJsonObject.put("fullTimeEmployees", validatekey("no_of_employees",jsonObject));
		outputJsonObject.put("website", validatekey("website",jsonObject));
		outputJsonObject.put("industry", validatekey("industry",jsonObject));
		outputJsonObject.put("quarterly_revenue_growth", validatekey("quarterly_revenue_growth",jsonObject));
		outputJsonObject.put("current_year_revenue", validatekey("current_year_revenue",jsonObject));
		outputJsonObject.put("previous_year_revenue", validatekey("previous_year_revenue",jsonObject));
		outputJsonObject.put("TotalAssets", validatekey("total_assets",jsonObject));
		outputJsonObject.put("gross_profit", validatekey("gross_profit",jsonObject));
		outputJsonObject.put("marketCap", validatekey("market_cap",jsonObject));
		outputJsonObject.put("last_quarterly_revenue", validatekey("last_quarterly_revenue",jsonObject));
		outputJsonObject.put("second_last_quarterly_revenue", validatekey("second_last_quarterly_revenue",jsonObject));
		outputJsonObject.put("longBusinessSummary", validatekey("description",jsonObject));
		outputJsonObject.put("parentName", validatekey("parent_name",jsonObject));
		outputJsonObject.put("annual_revenue_growth", getPercentage(validatekey("current_year_revenue",jsonObject),validatekey("previous_year_revenue",jsonObject)));
		outputJsonObject.put("source","yahoo");
		outputJsonObject.put("symbol", validatekey("ticker_symbol",jsonObject));
		outputJsonObject.put("longName", validatekey("name",jsonObject));

		
		outputJsonArray.put(outputJsonObject);
		System.out.println("Data: " +outputJsonArray.toString());
		context.setProperty("parentData",parentNameData(outputJsonObject,"parentName"));

		return outputJsonArray.toString();
	}
	public static String getYahooWithNodeJson(JSONObject jsonObject) {
		JSONArray outputJsonArray=new JSONArray();
		JSONObject outputObject=new JSONObject();
		outputObject.put("source","yahoo-node");
		outputObject.put("symbol", validatekey("ticker_symbol",jsonObject));
		outputObject.put("gross_profit", getInMillions(validatekey("gross_profit",jsonObject)));
		outputObject.put("TotalAssets", getInMillions(validatekey("total_assets",jsonObject)));
		outputObject.put("current_year_revenue", getInMillions(validatekey("current_year_revenue",jsonObject)));
		outputObject.put("previous_year_revenue",  getInMillions(validatekey("previous_year_revenue",jsonObject)));
		outputObject.put("marketCap", getInMillions(validatekey("market_cap",jsonObject)));
		outputObject.put("last_quarterly_revenue", getInMillions(validatekey("last_quarterly_revenue",jsonObject)));
		outputObject.put("second_last_quarterly_revenue", getInMillions(validatekey("second_last_quarterly_revenue",jsonObject)));
		outputObject.put("quarterly_revenue_growth", format(validatekey("quarterly_revenue_growth",jsonObject)));
		//	outputObject.put("quarterly_revenue_growth", String.format("%.2f", (Double.parseDouble(validatekey("quarterly_revenue_growth",jsonObject)))*100).concat("%"));
			String annualGrowth=getAnnualGrowthPercentage(validatekey("current_year_revenue",jsonObject),validatekey("previous_year_revenue",jsonObject));
			outputObject.put("annual_revenue_growth",format(annualGrowth) );
		outputObject.put("longName", validatekey("name",jsonObject));
		//if(!validatekey("employer",jsonObject).equals("")) {
		if(jsonObject.has("employer") && jsonObject.getJSONArray("employer").length()>0) {
		JSONArray employerArray=jsonObject.getJSONArray("employer");
		JSONArray outputEmployeeArray=new JSONArray();
		for(int i=0;i<employerArray.length();i++) {
		JSONObject outputEmployeeObject=new JSONObject();
		JSONObject employeeObject=employerArray.getJSONObject(i);
		outputEmployeeObject.put("name",validatekey("full_name",employeeObject) );
		outputEmployeeObject.put("title",validatekey("occupation",employeeObject) );
		outputEmployeeArray.put(outputEmployeeObject);
		}
		outputObject.put("employer",outputEmployeeArray);
		}
		outputJsonArray.put(outputObject);
		System.out.println("Contact Data:"+outputJsonArray.toString());

		return outputJsonArray.toString();
		}
	public static String getInMillions(String inputValue) {
		if(!inputValue.equals("")) {
			double d = Double.parseDouble(inputValue); 
			d=(d/1000000);
			System.out.println(String.format("%.0f",d).concat("M"));
			return String.format("%.2f",d).concat("M");
			}
			else {
				return "";
			}
	}
	public static String getPercentage(String firstValue, String secondValue) {
		System.out.println("In Percentage");
		if(firstValue.equals("") || secondValue.equals("")) {
			return "";
		}
		else {
			Double l1=Double.parseDouble(firstValue);
			Double l2=Double.parseDouble(secondValue);
			Double l=(l1-l2)/l2;
			return String.format("%.3f",l);
		}
	}
	
	public static String getAnnualGrowthPercentage(String firstValue, String secondValue) {
		if(firstValue.equals("") || secondValue.equals("")) {
			return "";
		}
		else {
			firstValue=firstValue.replace(" M", "");
			secondValue=secondValue.replace(" M", "");
			String currentYearValue=String.valueOf((Double.parseDouble(firstValue))*1000000);
			String previousYearValue=String.valueOf((Double.parseDouble(secondValue))*1000000);
			return getPercentage(currentYearValue,previousYearValue);

		}
	}
	public static String format(String inputValue ) {
		if(!inputValue.equals("")) {
			return String.format("%.2f", (Double.parseDouble(inputValue))*100).concat("%");
			}
			else {
				return "";
			}
	}
	public static JSONArray parentNameData(JSONObject jsonObject,String key) {
		   JSONArray parent=new JSONArray();
		if (!validatekey(key, jsonObject).equals("")) {
		
			try {
			 parent.put("https://company.org/resource/".concat(getEncodedString(validatekey(key,jsonObject))));
			 return parent;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
		 parent.put("https://company.org/resource/");
		 return parent;
		}
		return parent;

	}
	public static String getEncodedString(String urlText) throws UnsupportedEncodingException {
		if (urlText != null)
			return URLEncoder.encode(urlText, "UTF-8").replace("+", "%20");
		else
			return "";
	}

}
