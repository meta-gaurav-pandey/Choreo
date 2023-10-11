package com.example;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.synapse.MessageContext; 
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.commons.json.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import org.apache.synapse.core.axis2.Axis2MessageContext;

public class DBPediaMapper extends AbstractMediator { 
	public boolean mediate(MessageContext context) {
		// TODO Implement your mediation logic here
		String jsonPayloadToString = JsonUtil
				.jsonPayloadToString(((Axis2MessageContext) context)
				.getAxis2MessageContext());
		String isFull = (String) context.getProperty("isFull_Load");
		//JSONObject requestBody=XML.toJSONObject(payload);
		String source = (String) context.getProperty("source");
		//System.out.println(jsonPayloadToString);
		System.out.println("isFull_load :"+isFull);
		String outputArray="";
		if(source.equals("dbpedia")) {
			outputArray=getDBPediaMappingJson(jsonPayloadToString);
			context.setProperty("Array1", outputArray);
			if(isFull.equals("No")) {
			context.setProperty("parentData", parentNameData("foafName",jsonPayloadToString));
			//System.out.println("parentData  "+ parentNameData("foafName",jsonPayloadToString));
			}
			

		}
		
		 else if(source.equals("dbpedia-contact")) { 
			 outputArray=getDBpediaContactMapping(jsonPayloadToString);
     		 context.setProperty("Array2", outputArray);

			 }
		
		//System.out.println(outputArray);

		return true;
	}
	public JSONObject getJSONObject(String key,String value) {
		JSONObject jsonObject=new JSONObject();
		jsonObject.put("name",key);
		jsonObject.put("value",value);
		return jsonObject;
	}
	public String  getDBpediaContactMapping(String jsonPayloadToString) {
		JSONArray jsonArray = new JSONArray(jsonPayloadToString);
		JSONArray outputJsonArray=new JSONArray();
		for(int i = 0;i<jsonArray.length();i++) {
			JSONObject jsonObject=jsonArray.getJSONObject(i);	
			JSONObject outputObject=new JSONObject();
			JSONArray occupationArray=new JSONArray();

			outputObject.put("Name",checkKey("Name",jsonObject));
			outputObject.put("Info", checkKey("Info",jsonObject));
			outputObject.put("source", "Dbpedia-contact");
			outputObject.put("contact Name", checkKey("contact Name",jsonObject));
			
			Iterator<String> keys=jsonObject.keys();
			while(keys.hasNext()) {
				String key=(String)keys.next();
				if(key.contains("Occupation ")) {			
					JSONObject industryObject=new JSONObject();
					 if(!jsonObject.get(key).equals("")) {
				industryObject.put("Occupation", checkKey(key,jsonObject));
					 occupationArray.put(industryObject);
				}
			}
				
			outputObject.put("Occupation", occupationArray);
			
	}
			outputJsonArray.put(outputObject);
		
	}
		return outputJsonArray.toString();
	}
	public String getDBPediaMappingJson(String jsonPayloadToString) {
		JSONArray jsonArray = new JSONArray(jsonPayloadToString);
		JSONArray outputJsonArray=new JSONArray();
		String[] standardFields={"name","numberOfEmployees","URI","description","foafName","company_type","founded"};
		for(int i = 0;i<jsonArray.length();i++) {
			JSONObject jsonObject=jsonArray.getJSONObject(i);	
			JSONObject outputObject=new JSONObject();
			JSONArray employeeArray=new JSONArray();
			JSONArray industryArray=new JSONArray();
			JSONArray headquarterArray=new JSONArray();
			JSONArray custompropertiesArray=new JSONArray();
			outputObject.put("name",checkKey("name",jsonObject));
			outputObject.put("numberOfEmployees", checkKey("numberOfEmployees",jsonObject));
			outputObject.put("source", "dbpedia");
			outputObject.put("URI", checkKey("URI",jsonObject));
			outputObject.put("description", checkKey("description",jsonObject));
			//outputObject.put("operating_years", jsonObject.getString("employees_num"));
			outputObject.put("foafName", checkKey("foafName",jsonObject));
			outputObject.put("company_type", checkKey("company_type",jsonObject));
			outputObject.put("founded", checkKey("founded",jsonObject));
	
			Iterator<String> keys=jsonObject.keys();
			while(keys.hasNext()) {
				String key=(String)keys.next();
				if(key.contains("employer ")) {
					
					JSONObject employerObject=new JSONObject();
					if(!jsonObject.get(key).equals("")) {
					employerObject.put("employer", checkKey(key,jsonObject));
					
					employeeArray.put(employerObject);}
				}
				else if(key.contains("industry ")) {
					//String[] keywords=key.split("/");
					JSONObject industryObject=new JSONObject();
					if(!jsonObject.get(key).equals("")) {
						industryObject.put("industry", checkKey(key,jsonObject));
						industryArray.put(industryObject);
					}						
					
				}
				else if(key.contains("headquarter ")) {
					JSONObject headquarterObject=new JSONObject();
					if(!jsonObject.get(key).equals("")) {
						headquarterObject.put("headquarter", checkKey(key,jsonObject));
											
						headquarterArray.put(headquarterObject);
				}
				}
				
				else if(!Arrays.asList(standardFields).contains(key)) {
					if(!jsonObject.getString(key).equals(""))
					custompropertiesArray.put(getJSONObject(key,checkKey(key,jsonObject)));
				}
			}
			outputObject.put("employer", employeeArray);
			outputObject.put("industry", industryArray);
			outputObject.put("headquarters", headquarterArray);
			outputObject.put("custom_properties", custompropertiesArray);
			outputJsonArray.put(outputObject);
			
		//return "";	}
	}
		return outputJsonArray.toString();
	}
	public static String checkKey(String key,JSONObject jsonObject) {
		if(jsonObject.has(key)) {
			return jsonObject.getString(key);
		}
		else
		return "";
	}
	// Creating the Array for redis sync 
	
	public JSONArray parentNameData(String key,String InputjsonObject) {
		JSONArray jsonArray = new JSONArray(InputjsonObject);
		JSONArray outputJsonArray = new JSONArray();

		for (int i = 0; i < jsonArray.length(); i++) {
			try {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (!validatekey(key, jsonObject).equals("")) {

					outputJsonArray.put("https://company.org/resource/"
							.concat(getEncodedString(validatekey(key, jsonObject))));

				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// System.out.println(outputJsonArray.toString());
		return outputJsonArray;

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

	public static String getEncodedString(String urlText) throws UnsupportedEncodingException {
		if (urlText != null)
			return URLEncoder.encode(urlText, "UTF-8").replace("+", "%20");
		else
			return "";
	}
}