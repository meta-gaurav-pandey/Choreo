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

public class LinkedInMapper extends AbstractMediator { 

	public boolean mediate(MessageContext context) {
		// TODO Implement your mediation logic here
		String jsonPayloadToString = JsonUtil
				.jsonPayloadToString(((Axis2MessageContext) context)
				.getAxis2MessageContext());
		//JSONObject requestBody=XML.toJSONObject(payload);
		String source = (String) context.getProperty("source");
		System.out.println(source);
		String outputArray;
		if(source.equals("linkedin")) {
			outputArray=getLinkedinMappingJson(jsonPayloadToString);
			context.setProperty("Array1", outputArray);
			System.out.println(outputArray);
		}
		else if(source.equals("linkedin-parent")){
			outputArray=getLinkedinParentMappingJson(jsonPayloadToString);
			context.setProperty("Array2", outputArray);
			System.out.println(outputArray);
		}

		return true;
	}
	public JSONObject getJSONObject(String key,String value) {
		System.out.println("Map Kinkedin JSon");

		JSONObject jsonObject=new JSONObject();
		jsonObject.put("name",key);
		jsonObject.put("value",value);
		return jsonObject;
	}
	public String  getLinkedinParentMappingJson(String jsonPayloadToString) {
		JSONArray jsonArray = new JSONArray(jsonPayloadToString);
		JSONArray outputJsonArray=new JSONArray();
		for(int i = 0;i<jsonArray.length();i++) {
			JSONObject jsonObject=jsonArray.getJSONObject(i);
			if(!jsonObject.getString("parentName").equals("")) {
				String source[]=checkKey("social_url",jsonObject).split("https://www.linkedin.com/company/");
				jsonObject.put("source","Linkedin-parent");
				jsonObject.put("social_url", source[1]);
				outputJsonArray.put(jsonObject);
			}
		}
		return outputJsonArray.toString();
	}
	public String getLinkedinMappingJson(String jsonPayloadToString) {
		JSONArray jsonArray = new JSONArray(jsonPayloadToString);
		String[] standardFields={"names","industry","about_us","headquarters","employees_num","social_url","founded","type"};
		//List standardFieldsList = Arrays.asList(standardFields);
		JSONArray outputJsonArray=new JSONArray();
		for(int i = 0;i<jsonArray.length();i++) {
			JSONObject jsonObject=jsonArray.getJSONObject(i);	
			JSONObject outputObject=new JSONObject();
			JSONArray employeeArray=new JSONArray();
			JSONArray specialtiesArray=new JSONArray();
			JSONArray custompropertiesArray=new JSONArray();
			outputObject.put("company_name",checkKey("company_name",jsonObject));
			outputObject.put("name",checkKey("names",jsonObject));
			outputObject.put("industry", checkKey("industry",jsonObject));
			outputObject.put("source", "linkedin");
			outputObject.put("headquarters", checkKey("headquarters",jsonObject));
			outputObject.put("employees_num", checkKey("employees_num",jsonObject));
			//outputObject.put("operating_years", jsonObject.getString("employees_num"));
			outputObject.put("about_us", checkKey("about_us",jsonObject));
			outputObject.put("social_url", checkKey("social_url",jsonObject));
			outputObject.put("founded", checkKey("founded",jsonObject));
			outputObject.put("type", checkKey("type",jsonObject));
			String specialities="";
	
			Iterator<String> keys=jsonObject.keys();
			while(keys.hasNext()) {
				String key=(String)keys.next();
				if(key.contains("employees/")) {
					String[] keywords=key.split("/");
					int index=Integer.parseInt(keywords[1]);
					JSONObject employerObject;
					try {
					 employerObject=employeeArray.getJSONObject(index);}
					catch(JSONException e){
					 employerObject=new JSONObject();					
					}
					employerObject.put(keywords[2], checkKey(key,jsonObject));
					employeeArray.put(index, employerObject);
				}
				else if(key.contains("specialties/")) {
					if(!jsonObject.getString(key).equals("")) {
						JSONObject specialitiesObject=new JSONObject();
						specialitiesObject.put("specialities", checkKey(key,jsonObject));
							specialtiesArray.put(specialitiesObject);
					}
				}
				else if(key.contains("locations/")) {
					String[] keywords=key.split("/");
					if(keywords[2].equals("address")) {
						if(!jsonObject.getString(key).equals(""))
						custompropertiesArray.put(getJSONObject("locations",checkKey(key,jsonObject)));
					}
				}
				else if(key.contains("affiliated_companies/")) {
					String[] keywords=key.split("/");
					if(keywords[2].equals("company_name")) {
						if(!jsonObject.getString(key).equals(""))
						custompropertiesArray.put(getJSONObject("affiliated_companies",checkKey(key,jsonObject)));
					}
				}
				else if(key.contains("similar_companies/")) {
					String[] keywords=key.split("/");
					if(keywords[2].equals("company_name")) {
						if(!jsonObject.getString(key).equals(""))
						custompropertiesArray.put(getJSONObject("similar_companies",checkKey(key,jsonObject)));
					}
				}
				else if(key.equals("categories/0")) {
					if(!jsonObject.getString(key).equals(""))
					custompropertiesArray.put(getJSONObject("categories",checkKey(key,jsonObject)));
				}
				else if(!Arrays.asList(standardFields).contains(key)) {
					System.out.println(key);
					if(!jsonObject.getString(key).equals(""))
					custompropertiesArray.put(getJSONObject(key,checkKey(key,jsonObject)));
				}
			}
			outputObject.put("employer", employeeArray);
			outputObject.put("specialities", specialtiesArray);
			outputObject.put("custom_properties", custompropertiesArray);
			outputJsonArray.put(outputObject);
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
