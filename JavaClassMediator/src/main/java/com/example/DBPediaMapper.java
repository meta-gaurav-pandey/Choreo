package com.example;

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
		//JSONObject requestBody=XML.toJSONObject(payload);
		String source = (String) context.getProperty("source");
		//System.out.println(jsonPayloadToString);
		String outputArray="";
		if(source.equals("dbpedia")) {
			outputArray=getDBPediaMappingJson(jsonPayloadToString);
			context.setProperty("Array", outputArray);

		}
		
		 else if(source.equals("dbpedia-contact")) { 
			 outputArray=getDBpediaContactMapping(jsonPayloadToString);
     		 context.setProperty("Array2", outputArray);

			 }
		
		System.out.println(outputArray);

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
		String[] standardFields={"name","numberOfEmployees","profile_url","description","foafName","company_type","founded"};
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
}
