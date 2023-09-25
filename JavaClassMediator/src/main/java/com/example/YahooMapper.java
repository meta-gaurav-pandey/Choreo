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

public class YahooMapper extends AbstractMediator { 

	public boolean mediate(MessageContext context) {
		// TODO Implement your mediation logic here
		String jsonPayloadToString = JsonUtil
				.jsonPayloadToString(((Axis2MessageContext) context)
				.getAxis2MessageContext());
		//JSONObject requestBody=XML.toJSONObject(payload);
		String source = (String) context.getProperty("source");
		System.out.println("source");
		String outputArray;
		if(source.equals("yahoo-node")) {
			System.out.println("yahoo-node");
			outputArray=getYahooNodeMappingJson(jsonPayloadToString);
			context.setProperty("Array2", outputArray);
			System.out.println("Yahoo WithNode completed");
		}
		else if(source.equals("yahoo-without-node")) {
			System.out.println("yahoo-without-node");
			outputArray=getYahooWithoutNodeMappingJson(jsonPayloadToString);
			context.setProperty("Array1", outputArray);
			System.out.println("Yahoo WithoutNode completed");

		}
		
		

		return true;
	}
	public String getYahooWithoutNodeMappingJson(String jsonPayloadToString) {
		JSONArray jsonArray = new JSONArray(jsonPayloadToString);
		JSONArray outputJsonArray=new JSONArray();
		for(int i = 0;i<jsonArray.length();i++) {
			JSONObject jsonObject=jsonArray.getJSONObject(i);	
			jsonObject.put("source","yahoo");
			String annualGrowth=getPercentage(jsonObject.getString("current_year_revenue"),jsonObject.getString("previous_year_revenue"));	
			if(!annualGrowth.equals("")) {
			jsonObject.put("annual_revenue_growth",annualGrowth);}
			outputJsonArray.put(jsonObject);
		}
		return outputJsonArray.toString();
	}
	public String getYahooNodeMappingJson(String jsonPayloadToString) {
		JSONArray jsonArray = new JSONArray(jsonPayloadToString);
		JSONArray outputJsonArray=new JSONArray();
		for(int i = 0;i<jsonArray.length();i++) {
			JSONObject jsonObject=jsonArray.getJSONObject(i);
			JSONObject outputObject=new JSONObject();
			System.out.println(validatekey("symbol",jsonObject));
			outputObject.put("source","yahoo-node");
			outputObject.put("symbol", validatekey("symbol",jsonObject));
			outputObject.put("gross_profit", validatekey("gross_profit",jsonObject));	
			outputObject.put("TotalAssets", validatekey("TotalAssets",jsonObject));	
			outputObject.put("current_year_revenue", validatekey("current_year_revenue",jsonObject));		
			outputObject.put("previous_year_revenue",  validatekey("previous_year_revenue",jsonObject));	
			outputObject.put("marketCap", validatekey("marketCap",jsonObject));	
			outputObject.put("last_quarterly_revenue", validatekey("last_quarterly_revenue",jsonObject));		
			outputObject.put("second_last_quarterly_revenue", validatekey("second_last_quarterly_revenue",jsonObject));		
			outputObject.put("quarterly_revenue_growth", validatekey("quarterly_revenue_growth",jsonObject));
			outputObject.put("longName", validatekey("longName",jsonObject));
			
			outputJsonArray.put(outputObject);
			String annualGrowth=getAnnualGrowthPercentage(jsonObject.getString("current_year_revenue"),jsonObject.getString("previous_year_revenue"));	
			if(!annualGrowth.equals("")) {

				outputObject.put("annual_revenue_growth",String.format("%.2f", (Double.parseDouble(annualGrowth)*100)).concat("%"));
			}
		
			Iterator<String> keys=jsonObject.keys();
			JSONArray employeeArray=new JSONArray();
			while(keys.hasNext()) {
				String key=(String)keys.next();
				if(key.contains("employee/")) {
					String[] keywords=key.split("/");
					int index=Integer.parseInt(keywords[1]);
					JSONObject employerObject;
					try {
					 employerObject=employeeArray.getJSONObject(index);}
					catch(JSONException e){
					 employerObject=new JSONObject();					
					}
					employerObject.put(keywords[2], validatekey(key,jsonObject));
					employeeArray.put(index, employerObject);
				}
				outputObject.put("employer", employeeArray);
		}
			outputJsonArray.put(outputObject);

		}
		//System.out.println(outputJsonArray.toString());
		return outputJsonArray.toString();
		
	}
	public String getPercentage(String firstValue, String secondValue) {
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
	
	public String getAnnualGrowthPercentage(String firstValue, String secondValue) {
		if(firstValue.equals("") || secondValue.equals("")) {
			return "";
		}
		else {
			firstValue=firstValue.replace("M", "");
			secondValue=secondValue.replace("M", "");
			String currentYearValue=String.valueOf((Double.parseDouble(firstValue))*1000000);
			String previousYearValue=String.valueOf((Double.parseDouble(secondValue))*1000000);
			return getPercentage(currentYearValue,previousYearValue);

		}
	}
	
public String validatekey(String key,JSONObject jsonObject) {
		
		String result;
			if(jsonObject.has(key)) {
				if(!jsonObject.isNull(key)) {
				result=	jsonObject.getString(key);
				}
				else {
					result="";

				}
			}
			else {
				result="";

			}
			return result;
		}
}
