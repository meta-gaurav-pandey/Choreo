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
			System.out.println("Yahoo WithNode:"+outputArray);
		}
		else if(source.equals("yahoo-without-node")) {
			System.out.println("yahoo-without-node");
			outputArray=getYahooWithputNodeMappingJson(jsonPayloadToString);
			context.setProperty("Array1", outputArray);
			System.out.println("Yahoo WithoutNode:"+outputArray);

		}
		
		

		return true;
	}
	public String getYahooWithputNodeMappingJson(String jsonPayloadToString) {
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
			jsonObject.put("source","yahoo-node");
			String annualGrowth=getAnnualGrowthPercentage(jsonObject.getString("current_year_revenue"),jsonObject.getString("previous_year_revenue"));	
			if(!annualGrowth.equals("")) {

			jsonObject.put("annual_revenue_growth",String.format("%.2f", (Double.parseDouble(annualGrowth)*100)).concat("%"));
			}
			outputJsonArray.put(jsonObject);
		}
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
			firstValue=firstValue.replace(" M", "");
			secondValue=secondValue.replace(" M", "");
			String currentYearValue=String.valueOf((Double.parseDouble(firstValue))*1000000);
			String previousYearValue=String.valueOf((Double.parseDouble(secondValue))*1000000);
			return getPercentage(currentYearValue,previousYearValue);

		}
	}
}
