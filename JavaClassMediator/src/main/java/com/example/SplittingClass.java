package com.example;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONObject;

public class SplittingClass extends AbstractMediator {
	public boolean mediate(MessageContext context) {
		String inputJson = JsonUtil.jsonPayloadToString(((Axis2MessageContext) context).getAxis2MessageContext());
		boolean flag = false;
		String outputArray;
		String outputParentArray;
		JSONArray jsonArray = new JSONArray(inputJson);
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			if (jsonObject.has("source")) {
				if (jsonObject.get("source").equals("dbpedia")) {
					outputArray = getDbpediaMapping(jsonObject);
					outputParentArray = getDbpediaContactMapping(jsonObject);
					context.setProperty("Array1", outputArray);
					context.setProperty("Array2", outputParentArray);
					context.setProperty("parentData", parentNameData(jsonObject));
					System.out.println("in dbpedia source");
					flag = true;
					break;
				} else if (jsonObject.get("source").equals("linkedin")) {
					outputArray = getLinkedinMappingJson(jsonObject);
					outputParentArray = getLinkedinParentMapping(jsonObject);
					context.setProperty("Array1", outputArray);
					context.setProperty("Array2", outputParentArray);
					context.setProperty("parentData", parentNameData(jsonObject));
					System.out.println("in linkedin source");
					flag = true;
					break;
				} else if (jsonObject.get("source").equals("yahoo_finance")) {
					outputArray = getYahooWithoutNodeJson(jsonObject);
					outputParentArray = getYahooWithNodeJson(jsonObject);
					context.setProperty("Array1", outputArray);
					context.setProperty("Array2", outputParentArray);
					context.setProperty("parentData", parentNameData(jsonObject));
					System.out.println("in yahoo_finance source");
					flag = true;
					break;
				} else if (jsonObject.get("source").equals("others")) {
					outputArray = getOthersJson(jsonObject);
					JSONArray Array2 = new JSONArray();
					context.setProperty("Array1", outputArray);
					context.setProperty("Array2", Array2);
					context.setProperty("parentData", parentNameData(jsonObject));
					System.out.println("in others source");
					flag = true;
					break;
				}

			} else {
				continue;
			}

		}
		if (!flag) {
			System.out.println("No source present in data");
		}

		return true;

	}

	public String getLinkedinMappingJson(JSONObject jsonObject) {
		JSONArray outputJsonArray = new JSONArray();
		JSONObject outputObject = new JSONObject();

		outputObject.put("name", validatekey("name", jsonObject));
		outputObject.put("company_name", validatekey("name", jsonObject));
		outputObject.put("source", "linkedin");
		outputObject.put("industry", validatekey("industry", jsonObject));
		outputObject.put("headquarters", validatekey("headquarters", jsonObject));
		outputObject.put("employees_num", validatekey("no_of_employees", jsonObject));
		outputObject.put("about_us", validatekey("description", jsonObject));
		outputObject.put("social_url", validatekey("social_url", jsonObject));
		outputObject.put("founded", validatekey("founded", jsonObject));
		outputObject.put("type", validatekey("company_type", jsonObject));
		outputObject.put("specialities", getSpecialtiesArray(validatekey("specialities", jsonObject)));
		if (jsonObject.has("custom_properties"))
			outputObject.put("custom_properties", jsonObject.getJSONArray("custom_properties"));
		if (jsonObject.has("employer"))
			outputObject.put("employer", jsonObject.getJSONArray("employer"));

		outputJsonArray.put(outputObject);

		System.out.println("Source Data:" + outputJsonArray.toString());
		return outputJsonArray.toString();
	}

	public JSONArray getSpecialtiesArray(String specialites) {
		String[] specialtiesArray = specialites.split(",");
		JSONArray specialitiesJSONArray = new JSONArray();
		for (int i = 0; i < specialtiesArray.length; i++) {
			JSONObject specialityObject = new JSONObject();
			specialityObject.put("specialities", specialtiesArray[i]);
			specialitiesJSONArray.put(specialityObject);
		}
		return specialitiesJSONArray;
	}

	public String getLinkedinParentMapping(JSONObject jsonObject) {
		JSONObject outputParentObject = new JSONObject();
		JSONArray outputParentArray = new JSONArray();
		String source[] = validatekey("social_url", jsonObject).split("https://www.linkedin.com/company/");

		outputParentObject.put("Comp_Name", source[1]);
		outputParentObject.put("parentName", validatekey("parent_name", jsonObject));
		outputParentObject.put("source", "Linkedin-parent");
		outputParentArray.put(outputParentObject);
		System.out.println("Parent Data" + outputParentArray.toString());
		return outputParentArray.toString();
	}

	public String getDbpediaMapping(JSONObject jsonObject) {
		JSONArray outputJsonArray = new JSONArray();
		JSONObject outputObject = new JSONObject();
		outputObject.put("name", validatekey("name", jsonObject));
		outputObject.put("source", "dbpedia");
		outputObject.put("numberOfEmployees", validatekey("no_of_employees", jsonObject));
		outputObject.put("URI", validatekey("profile_url", jsonObject));
		// if(jsonObject.get("founded").jsonObject.getString("profile_url"));
		outputObject.put("founded", validatekey("founded", jsonObject));
		outputObject.put("company_type", validatekey("company_type", jsonObject));
		outputObject.put("custom_properties", jsonObject.getJSONArray("custom_properties"));
		outputObject.put("foafName", jsonObject.getString("parent_name"));
		JSONArray inputEmployeeArray = new JSONArray();
		inputEmployeeArray = jsonObject.getJSONArray("employer");
		JSONArray outputEmployeeArray = new JSONArray();
		for (int j = 0; j < inputEmployeeArray.length(); j++) {
			JSONObject inputemployeeObject = inputEmployeeArray.getJSONObject(j);
			JSONObject employeeObject = new JSONObject();
			employeeObject.put("employer", validatekey("full_name", inputemployeeObject));
			outputEmployeeArray.put(employeeObject);
		}
		outputObject.put("employer", outputEmployeeArray);
		JSONArray industryArray = new JSONArray();
		JSONObject industryObject = new JSONObject();
		industryObject.put("industry", validatekey("industry", jsonObject));
		industryArray.put(industryObject);
		outputObject.put("industry", industryArray);

		JSONArray headquarterArray = new JSONArray();
		JSONObject headquarterObject = new JSONObject();
		headquarterObject.put("headquarter", validatekey("headquarters", jsonObject));
		headquarterArray.put(headquarterObject);
		outputObject.put("headquarters", headquarterArray);
		outputJsonArray.put(outputObject);
		System.out.println("source " + outputJsonArray.toString());
		return outputJsonArray.toString();
	}

	public String getDbpediaContactMapping(JSONObject jsonObject) {
		JSONArray outputJsonArray = new JSONArray();
		JSONArray contactArray = jsonObject.getJSONArray("employer");
		for (int i = 0; i < contactArray.length(); i++) {
			JSONObject inputemployeeObject = contactArray.getJSONObject(i);
			JSONObject contactObject = new JSONObject();
			contactObject.put("Name", validatekey("full_name", inputemployeeObject));
			contactObject.put("Info", validatekey("description", inputemployeeObject));
			contactObject.put("contact Name", validatekey("full_name", inputemployeeObject));
			contactObject.put("source", "Dbpedia-contact");
			JSONObject occupationObject = new JSONObject();
			JSONArray occupationArray = new JSONArray();
			occupationObject.put("Occupation", validatekey("occupation", inputemployeeObject));
			occupationArray.put(occupationObject);
			contactObject.put("Occupation", occupationArray);
			outputJsonArray.put(contactObject);
		}
		System.out.println("parent " + outputJsonArray.toString());
		return outputJsonArray.toString();
	}

	public String getYahooWithoutNodeJson(JSONObject jsonObject) {
		JSONArray outputJsonArray = new JSONArray();
		JSONObject outputJsonObject = new JSONObject();
		outputJsonObject.put("tickerSymbol", validatekey("ticket_symbol", jsonObject));
		outputJsonObject.put("fullTimeEmployees", validatekey("no_of_employees", jsonObject));
		outputJsonObject.put("website", validatekey("website", jsonObject));
		outputJsonObject.put("industry", validatekey("industry", jsonObject));
		outputJsonObject.put("quarterly_revenue_growth", validatekey("quarterly_revenue_growth", jsonObject));
		outputJsonObject.put("current_year_revenue", validatekey("current_year_revenue", jsonObject));
		outputJsonObject.put("previous_year_revenue", validatekey("previous_year_revenue", jsonObject));
		outputJsonObject.put("TotalAssets", validatekey("total_assets", jsonObject));
		outputJsonObject.put("gross_profit", validatekey("gross_profit", jsonObject));
		outputJsonObject.put("marketCap", validatekey("market_cap", jsonObject));
		outputJsonObject.put("last_quarterly_revenue", validatekey("last_quarterly_revenue", jsonObject));
		outputJsonObject.put("second_last_quarterly_revenue", validatekey("second_last_quarterly_revenue", jsonObject));
		outputJsonObject.put("longBusinessSummary", validatekey("description", jsonObject));
		outputJsonObject.put("parentName", validatekey("parent_name", jsonObject));
		outputJsonObject.put("annual_revenue_growth", getPercentage(validatekey("current_year_revenue", jsonObject),
				validatekey("previous_year_revenue", jsonObject)));
		outputJsonObject.put("source", "yahoo");
		outputJsonObject.put("symbol", validatekey("ticker_symbol", jsonObject));
		outputJsonObject.put("longName", validatekey("name", jsonObject));

		outputJsonArray.put(outputJsonObject);
		System.out.println("Data:" + outputJsonArray.toString());

		return outputJsonArray.toString();
	}

	public String getYahooWithNodeJson(JSONObject jsonObject) {
		JSONArray outputJsonArray = new JSONArray();
		JSONObject outputObject = new JSONObject();
		outputObject.put("source", "yahoo-node");
		outputObject.put("symbol", validatekey("ticker_symbol", jsonObject));
		outputObject.put("gross_profit", getInMillions(validatekey("gross_profit", jsonObject)));
		outputObject.put("TotalAssets", getInMillions(validatekey("total_assets", jsonObject)));
		outputObject.put("current_year_revenue", getInMillions(validatekey("current_year_revenue", jsonObject)));
		outputObject.put("previous_year_revenue", getInMillions(validatekey("previous_year_revenue", jsonObject)));
		outputObject.put("marketCap", getInMillions(validatekey("market_cap", jsonObject)));
		outputObject.put("last_quarterly_revenue", getInMillions(validatekey("last_quarterly_revenue", jsonObject)));
		outputObject.put("second_last_quarterly_revenue",
				getInMillions(validatekey("second_last_quarterly_revenue", jsonObject)));
		outputObject.put("quarterly_revenue_growth",
				String.format("%.2f", (Double.parseDouble(validatekey("quarterly_revenue_growth", jsonObject))) * 100)
						.concat("%"));
		String annualGrowth = getAnnualGrowthPercentage(validatekey("current_year_revenue", jsonObject),
				validatekey("previous_year_revenue", jsonObject));
		outputObject.put("annual_revenue_growth",
				String.format("%.2f", (Double.parseDouble(annualGrowth) * 100)).concat("%"));
		outputObject.put("longName", validatekey("name", jsonObject));
		// if(!validatekey("employer",jsonObject).equals("")) {
		if (jsonObject.has("employer") && jsonObject.getJSONArray("employer").length() > 0) {
			JSONArray employerArray = jsonObject.getJSONArray("employer");
			JSONArray outputEmployeeArray = new JSONArray();
			for (int i = 0; i < employerArray.length(); i++) {
				JSONObject outputEmployeeObject = new JSONObject();
				JSONObject employeeObject = employerArray.getJSONObject(i);
				outputEmployeeObject.put("name", validatekey("full_name", employeeObject));
				outputEmployeeObject.put("title", validatekey("occupation", employeeObject));
				outputEmployeeArray.put(outputEmployeeObject);
			}
			outputObject.put("employer", outputEmployeeArray);
		}
		outputJsonArray.put(outputObject);
		System.out.println("Contact Data:" + outputJsonArray.toString());

		return outputJsonArray.toString();
	}

	public String getOthersJson(JSONObject jsonObject) {
		JSONArray outputJsonArray = new JSONArray();
		JSONObject outputJsonObject = new JSONObject();
		outputJsonObject.put("name", validatekey("name", jsonObject));
		outputJsonObject.put("description", validatekey("description", jsonObject));
		outputJsonObject.put("SIC", validatekey("SIC", jsonObject));
		outputJsonObject.put("NAICS", validatekey("NAICS", jsonObject));
		outputJsonObject.put("headquarters", validatekey("headquarters", jsonObject));
		outputJsonObject.put("operating_years", validatekey("operating_years", jsonObject));
		outputJsonObject.put("no_of_employees", validatekey("no_of_employees", jsonObject));
		outputJsonObject.put("revenue_dollar", validatekey("revenue_dollar", jsonObject));
		outputJsonObject.put("annual_growth", validatekey("annual_growth", jsonObject));
		outputJsonObject.put("quarterly_growth", validatekey("quarterly_growth", jsonObject));
		outputJsonObject.put("parent_name", validatekey("parent_name", jsonObject));
		if (jsonObject.has("custom_properties") && jsonObject.getJSONArray("custom_properties").length() > 0) {
			outputJsonObject.put("custom_properties", jsonObject.getJSONArray("custom_properties"));
		}

		outputJsonObject.put("source", "others");
		outputJsonArray.put(outputJsonObject);
		System.out.println("Others Data:" + outputJsonArray.toString());
		return outputJsonArray.toString();
	}

	public JSONArray parentNameData(JSONObject jsonObject) {
		   JSONArray parent=new JSONArray();
		if (!validatekey("parent_name", jsonObject).equals("")) {
		
			try {
			 parent.put("https://company.org/resource/".concat(getEncodedString(validatekey("parent_name",jsonObject))));
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
	public String getInMillions(String inputValue) {
		double d = Double.parseDouble(inputValue);
		d = (d / 1000000);
		System.out.println(String.format("%.0f", d).concat("M"));
		return String.format("%.2f", d).concat("M");

	}

	public String getPercentage(String firstValue, String secondValue) {
		System.out.println("In Percentage");
		if (firstValue.equals("") || secondValue.equals("")) {
			return "";
		} else {
			Double l1 = Double.parseDouble(firstValue);
			Double l2 = Double.parseDouble(secondValue);
			Double l = (l1 - l2) / l2;
			return String.format("%.3f", l);
		}
	}

	public String getAnnualGrowthPercentage(String firstValue, String secondValue) {
		if (firstValue.equals("") || secondValue.equals("")) {
			return "";
		} else {
			firstValue = firstValue.replace(" M", "");
			secondValue = secondValue.replace(" M", "");
			String currentYearValue = String.valueOf((Double.parseDouble(firstValue)) * 1000000);
			String previousYearValue = String.valueOf((Double.parseDouble(secondValue)) * 1000000);
			return getPercentage(currentYearValue, previousYearValue);

		}
	}

	public JSONObject getJSONObject(String key, String value) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", key);
		jsonObject.put("value", value);
		return jsonObject;
	}

	public static String validatekey(String key, JSONObject jsonObject) {

		String result;
		if (jsonObject.has(key)) {
			if (!jsonObject.isNull(key)) {
				result = jsonObject.getString(key);
			} else {
				result = "";

			}
		} else {
			result = "";

		}
		return result;
	}
	
	
}