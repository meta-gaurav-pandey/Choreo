package com.example;

import java.util.Arrays;
import java.util.Iterator;

import javax.swing.plaf.synth.SynthOptionPaneUI;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

public class JavaMediatorClass extends AbstractMediator {

	public boolean mediate(MessageContext context) {
		// TODO Implement your mediation logic here

		JSONArray json;
		String payload = context.getEnvelope().getBody().toString();

		String ObjectType = (String) context.getProperty("sObjectType");
		System.out.println(ObjectType);
		try {
			if (ObjectType.equals("Account")) {
				System.out.println("Account" + payload);
				json = convertAccount(payload);
				context.setProperty("Array", json.toString());
			} else if (ObjectType.equals("Lead")) {
				System.out.println("Lead" + payload);
				json = convertLead(payload);
				context.setProperty("Array1", json.toString());
				System.out.println(json.toString());

			} else if (ObjectType.equals("AccountScheduler")) {
				System.out.println("Scheduler" + payload);
				json = convertSchedule(payload);
				context.setProperty("Array", json.toString());
			} else if (ObjectType.equals("DataEvent")) {
				System.out.println("DataEvent" + payload);
				json = convertEventData(payload);
				context.setProperty("Array", json.toString());
			} else if (ObjectType.equals("ApiData")) {
				System.out.println("ApiData" + payload);
				json = convertApiData(payload);
				context.setProperty("Array", json.toString());
			} else {
				throw new Exception("The typeObject is null");
			}

		} catch (Exception e) {
			System.out.println(
					"Error occure while converting into json object either records are empty or retrival failed");

		}

		return true;
	}

	public static JSONArray convertAccount(String xml) throws org.json.JSONException {

		if (xml.length() > 0) {
			JSONObject jsonObj = XML.toJSONObject(xml);

			JSONArray records;

			records = new JSONArray();
			records.put(jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getJSONObject("sobject"));

			System.out.println("Hey" + records);
			JSONArray result = new JSONArray();
			for (int i = 0; i < records.length(); i++) {

				JSONObject record = records.getJSONObject(i);
				JSONObject obj = new JSONObject();
				obj.put("name", record.getString("Name"));
				obj.put("Id", record.getString("Id"));
				// obj.put("type", record.getJSONObject("attributes").getString("type"));
				// obj.put("AccountNo", record.getString("AccountNumber"));
				// obj.put("description", record.getString("Description"));
				obj.put("type", "Account");
				result.put(obj);

			}
			return result;
		} else {
			JSONArray result = new JSONArray();
			return result;
		}

	}

	public static JSONArray convertLead(String xml) throws org.json.JSONException {

		if (xml.length() > 0) {
			JSONObject jsonObj = XML.toJSONObject(xml);

			JSONArray records;

			records = new JSONArray();
			records.put(jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getJSONObject("records"));

			// System.out.println("Hey" + records);
			JSONArray result = new JSONArray();
			// String[]
			// standardFields={"zeniadev__SICCode__c","zeniadev__Quarterly_Growth__c","zeniadev__Operating_Years__c","Description","zeniadev__No_Of_Employees__c","zeniadev__Annual_Growth__c","Status","Industry","zeniadev__Headquarters__c","zeniadev__Niacs_Code__c","Company","Name"};
			for (int i = 0; i < records.length(); i++) {
				JSONArray employeeArray = new JSONArray();
				JSONObject record = records.getJSONObject(i);

				JSONObject obj = new JSONObject();
				obj.put("Name", checkKeySales("Name", record));
				obj.put("Description", checkKeySales("Description", record));
				obj.put("source", "salesforce");
			//	obj.put("zeniadev__SICCode__c", checkKeySales("zeniadev__SICCode__c", record));

				obj.put("zeniadev__Quarterly_Growth__c", checkKeyPercent("zeniadev__Quarterly_Growth__c", record));
				obj.put("zeniadev__Operating_Years__c", checkKeySales("zeniadev__Operating_Years__c", record));
				obj.put("zeniadev__No_Of_Employees__c", checkKeySales("zeniadev__No_Of_Employees__c", record));
				obj.put("zeniadev__Annual_Growth__c", checkKeyPercent("zeniadev__Annual_Growth__c", record));

				obj.put("Status", checkKeySales("Status", record));
				obj.put("Industry", checkKeySales("Industry", record));
				obj.put("zeniadev__Headquarters__c", checkKeySales("zeniadev__Headquarters__c", record));
			//	obj.put("zeniadev__Niacs_Code__c", checkKeySales("zeniadev__Niacs_Code__c", record));
				obj.put("Company", checkKeySales("Company", record));
//				obj.put("latitude", record.get("Latitude"));
//				obj.put("longitude", record.get("Longitude"));
//				obj.put("geocodeAccuracy", record.get("GeocodeAccuracy"));
//				obj.put("phone", record.get("Phone"));
//				obj.put("mobilePhone", record.get("MobilePhone"));
//				obj.put("fax", record.get("Fax"));
//				obj.put("email", record.get("Email"));
//				obj.put("website", record.get("Website"));
//				obj.put("status", record.get("Status"));
//				obj.put("industry", record.get("Industry"));
//				obj.put("rating", record.get("Rating"));
//				obj.put("annualRevenue", record.get("AnnualRevenue"));
//				obj.put("no_of_employees", record.get("NumberOfEmployees"));
//				obj.put("SIC", record.get("SICCode__c"));
//				obj.put("type", record.get("Company_type__c"));
//				obj.put("annual_growth", record.get("Annual_Growth__c"));
//				obj.put("NAICS", record.get("NAICS__c"));
//				obj.put("second_last_quarterly_revenue", record.get("second_last_quarterly_revenue__c"));
//				obj.put("last_quarterly_revenue", record.get("SICCode__c"));
//				obj.put("last_quarterly_revenue", record.get("last_quarterly_revenue__c"));
//				obj.put("yearly_revenue_2021", record.get("yearly_revenue_2021__c"));
//				obj.put("yearly_revenue_2022", record.get("yearly_revenue_2022__c"));
//				obj.put("Categories", record.get("Categories__c"));
//				obj.put("Specialties", record.get("Specialties__c"));
//				obj.put("headquarters", record.get("headquarters__c"));
				Iterator<String> keys = record.keys();
				while (keys.hasNext()) {
					String key = (String) keys.next();
					if (key.contains("zeniadev__Contacts__r")) {

						if (!(record.get(key) == null) || !record.get(key).equals("")) {
							System.out.println("condition");
							int size = record.getJSONObject(key).getInt("totalSize");

							JSONArray emp = new JSONArray();
							if (size == 1) {
								emp.put(record.getJSONObject(key).getJSONObject("records"));
							} else {

								emp = record.getJSONObject(key).getJSONArray("records");
							}
							System.out.println(emp.toString());
							for (int r = 0; r < emp.length(); r++) {
								JSONObject empRecord = emp.getJSONObject(r);
								JSONObject empObject = new JSONObject();
								empObject.put("zeniadev__Designation__c",
										checkKeySales("zeniadev__Designation__c", empRecord));
								empObject.put("Name", checkKeySales("Name", empRecord));
								empObject.put("zeniadev__Lead__c", checkKeySales("zeniadev__Lead__c", empRecord));
								employeeArray.put(empObject);
							}
							obj.put("Contact", employeeArray);

							// employeeArray.put(employerObject);
						}
					} else if (key.contains("zeniadev__Niacs_Code__c")) {
						JSONArray NaicArray = new JSONArray();
						if (!checkKeySales(key, record).equals("")) {

							String[] code = checkKeySales(key, record).split(",");

							for (int j = 0; j < code.length; j++) {
								JSONObject Naic = new JSONObject();
								Naic.put("zeniadev__Niacs_Code__c", code[j].trim());
								NaicArray.put(Naic);
							}
						}
						obj.put("naics", NaicArray);

					}
					else if (key.contains("zeniadev__SICCode__c")) {
						JSONArray sicArray = new JSONArray();
						if (!checkKeySales(key, record).equals("")) {

							String[] code = checkKeySales(key, record).split(",");

							for (int j = 0; j < code.length; j++) {
								JSONObject sic = new JSONObject();
								sic.put("zeniadev__SICCode__c", code[j].trim());
								sicArray.put(sic);
							}
						}
						obj.put("sic", sicArray);

					}
				}
				result.put(obj);

			}
			return result;
		} else {
			JSONArray result = new JSONArray();
			return result;
		}

	}

	public static JSONObject getJSONObject(String key, String value) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", key);
		jsonObject.put("value", value);
		return jsonObject;
	}

	public static String checkKeySales(String key, JSONObject jsonObject) {
		if (jsonObject.has(key)) {
			return (String) jsonObject.get(key).toString();
		} else
			return "";
	}

	public static String checkKeyPercent(String key, JSONObject jsonObject) {
		if (jsonObject.has(key)) {
			return (String) jsonObject.get(key).toString().concat("%");
		} else
			return "";
	}

	public static JSONArray convertSchedule(String xml) throws org.json.JSONException {

		if (xml.length() > 0) {
			JSONObject jsonObj = XML.toJSONObject(xml);
			int count = jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getInt("totalSize");
			JSONArray records;
			if (count == 1) {
				records = new JSONArray();
				records.put(jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getJSONObject("records"));

			} else {
				records = jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getJSONArray("records");

			}
			System.out.println("Hey" + records);
			JSONArray result = new JSONArray();
			for (int i = 0; i < records.length(); i++) {

				JSONObject record = records.getJSONObject(i);
				JSONObject obj = new JSONObject();
				obj.put("name", record.getString("Name"));
				obj.put("Id", record.getString("Id"));
				obj.put("type", record.getJSONObject("attributes").getString("type"));
				result.put(obj);

			}
			return result;
		} else {
			JSONArray result = new JSONArray();
			return result;
		}

	}

	public static JSONArray convertEventData(String xml) throws org.json.JSONException {

		if (xml.length() > 0) {
			JSONObject jsonObj = XML.toJSONObject(xml);
			// System.out.println(jsonObj);
			JSONArray records;

			records = new JSONArray();
			records.put(jsonObj.getJSONObject("soapenv:Body").getJSONObject("Record"));

			JSONArray result = new JSONArray();
			for (int i = 0; i < records.length(); i++) {

				JSONObject record = records.getJSONObject(i);
				JSONObject obj = new JSONObject();
				obj.put("name", record.get("name") instanceof JSONObject ? "" : record.get("name"));

				obj.put("website", record.get("website") instanceof JSONObject ? "" : record.get("website"));
				obj.put("source", record.get("source") instanceof JSONObject ? "" : record.get("source"));
				obj.put("industry", record.get("industry") instanceof JSONObject ? "" : record.get("industry"));

				obj.put("headquarters",
						record.get("headquarters") instanceof JSONObject ? "" : record.get("headquarters"));
				obj.put("country", record.get("country") instanceof JSONObject ? "" : record.get("country"));
				obj.put("market_value",
						record.get("market_value") instanceof JSONObject ? "" : record.get("market_value"));
				obj.put("annual_growth",
						record.get("annual_growth") instanceof JSONObject ? "" : record.get("annual_growth"));
				// obj.put("company", record.get("Company"));
				obj.put("quarterly_growth",
						record.get("quarterly_growth") instanceof JSONObject ? "" : record.get("quarterly_growth"));
				obj.put("no_of_employees",
						record.get("no_of_employees") instanceof JSONObject ? "" : record.get("no_of_employees"));
				obj.put("type", record.get("type") instanceof JSONObject ? "" : record.get("type"));
				obj.put("founded", record.get("founded") instanceof JSONObject ? "" : record.get("founded"));
				obj.put("assets", record.get("assets") instanceof JSONObject ? "" : record.get("assets"));
				obj.put("sales", record.get("sales") instanceof JSONObject ? "" : record.get("sales"));
				obj.put("profit", record.get("profit") instanceof JSONObject ? "" : record.get("profit"));
				obj.put("yearly_revenue_2022", record.get("yearly_revenue_2022") instanceof JSONObject ? ""
						: record.get("yearly_revenue_2022"));
				obj.put("yearly_revenue_2021", record.get("yearly_revenue_2021") instanceof JSONObject ? ""
						: record.get("yearly_revenue_2021"));
				obj.put("last_quarterly_revenue", record.get("last_quarterly_revenue") instanceof JSONObject ? ""
						: record.get("last_quarterly_revenue"));
				obj.put("second_last_quarterly_revenue",
						record.get("second_last_quarterly_revenue") instanceof JSONObject ? ""
								: record.get("second_last_quarterly_revenue"));
				obj.put("article", record.get("article") instanceof JSONObject ? "" : record.get("article"));
				obj.put("SIC", record.get("SIC") instanceof JSONObject ? "" : record.get("SIC"));
				obj.put("NAICS", record.get("NAICS") instanceof JSONObject ? "" : record.get("NAICS"));
				obj.put("company_size",
						record.get("company_size") instanceof JSONObject ? "" : record.get("company_size"));
				obj.put("Specialties",
						record.get("Specialties") instanceof JSONObject ? "" : record.get("Specialties"));

				result.put(obj);

			}
			return result;
		} else {
			JSONArray result = new JSONArray();
			return result;
		}

	}

	public static JSONArray convertApiData(String xml) throws org.json.JSONException {

		if (xml.length() > 0) {
			JSONObject jsonObj = XML.toJSONObject(xml);
			// int
			// count=jsonObj.getJSONJSONObject("soapenv:Body").getJSONJSONObject("jsonJSONObject").getInt("totalSize");
			JSONArray records;

			records = new JSONArray();
			if (jsonObj.getJSONObject("soapenv:Body").getJSONObject("SourceData").get("Data") instanceof JSONObject) {
				records.put(jsonObj.getJSONObject("soapenv:Body").getJSONObject("SourceData").get("Data"));
			} else if (jsonObj.getJSONObject("soapenv:Body").getJSONObject("SourceData")
					.get("Data") instanceof JSONArray) {
				records = jsonObj.getJSONObject("soapenv:Body").getJSONObject("SourceData").getJSONArray("Data");
			} else {
				System.out.println("No data is comming of error occured");
			}

			JSONArray result = new JSONArray();
			for (int i = 0; i < records.length(); i++) {

				JSONObject record = records.getJSONObject(i);
				JSONObject obj = new JSONObject();
				obj.put("name", record.get("name") instanceof JSONObject ? "" : record.get("name"));

				obj.put("website", record.get("website") instanceof JSONObject ? "" : record.get("website"));
				obj.put("source", record.get("source") instanceof JSONObject ? "" : record.get("source"));
				obj.put("industry", record.get("industry") instanceof JSONObject ? "" : record.get("industry"));

				obj.put("headquarters",
						record.get("headquarters") instanceof JSONObject ? "" : record.get("headquarters"));
				obj.put("country", record.get("country") instanceof JSONObject ? "" : record.get("country"));
				obj.put("market_value",
						record.get("market_value") instanceof JSONObject ? "" : record.get("market_value"));
				obj.put("annual_growth",
						record.get("annual_growth") instanceof JSONObject ? "" : record.get("annual_growth"));
				// obj.put("company", record.get("Company"));
				obj.put("quarterly_growth",
						record.get("quarterly_growth") instanceof JSONObject ? "" : record.get("quarterly_growth"));
				obj.put("no_of_employees",
						record.get("no_of_employees") instanceof JSONObject ? "" : record.get("no_of_employees"));
				obj.put("type", record.get("type") instanceof JSONObject ? "" : record.get("type"));
				obj.put("founded", record.get("founded") instanceof JSONObject ? "" : record.get("founded"));
				obj.put("assets", record.get("assets") instanceof JSONObject ? "" : record.get("assets"));
				obj.put("sales", record.get("sales") instanceof JSONObject ? "" : record.get("sales"));
				obj.put("profit", record.get("profit") instanceof JSONObject ? "" : record.get("profit"));
				obj.put("yearly_revenue_2022", record.get("yearly_revenue_2022") instanceof JSONObject ? ""
						: record.get("yearly_revenue_2022"));
				obj.put("yearly_revenue_2021", record.get("yearly_revenue_2021") instanceof JSONObject ? ""
						: record.get("yearly_revenue_2021"));
				obj.put("last_quarterly_revenue", record.get("last_quarterly_revenue") instanceof JSONObject ? ""
						: record.get("last_quarterly_revenue"));
				obj.put("second_last_quarterly_revenue",
						record.get("second_last_quarterly_revenue") instanceof JSONObject ? ""
								: record.get("second_last_quarterly_revenue"));
				obj.put("article", record.get("article") instanceof JSONObject ? "" : record.get("article"));
				obj.put("SIC", record.get("SIC") instanceof JSONObject ? "" : record.get("SIC"));
				obj.put("NAICS", record.get("NAICS") instanceof JSONObject ? "" : record.get("NAICS"));
				obj.put("company_size",
						record.get("company_size") instanceof JSONObject ? "" : record.get("company_size"));
				obj.put("Specialties",
						record.get("Specialties") instanceof JSONObject ? "" : record.get("Specialties"));

				result.put(obj);

			}
			return result;
		} else {
			JSONArray result = new JSONArray();
			return result;
		}

	}
}
