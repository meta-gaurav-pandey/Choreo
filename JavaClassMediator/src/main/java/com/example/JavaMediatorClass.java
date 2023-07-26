package com.example;

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
      
       String ObjectType=(String)context.getProperty("sObjectType");
       System.out.println(ObjectType);
         try {
        	 if(ObjectType.equals("Account")) {
        		 System.out.println("Account"+payload);
			json=convertAccount(payload);
			context.setProperty("Array", json.toString());
			}
        	 else if(ObjectType.equals("Lead")) {
        		 System.out.println("Lead"+payload);
        		 json=convertLead(payload);
     			context.setProperty("Array", json.toString()); 
        	 }
        	 else if (ObjectType.equals("AccountScheduler")) {
        		 System.out.println("Scheduler"+payload);
        		 json=convert(payload);
      			context.setProperty("Array", json.toString()); 
        	 }
        	 
			 } catch (org.json.JSONException e) {
				System.out.println("Error occure while converting into json object either records are empty or retrival failed");
				
			}
         
     return true;
 }
	
	
	public static JSONArray convertAccount(String xml) throws org.json.JSONException {
		
  if(xml.length()>0) {
 	 JSONObject jsonObj = XML.toJSONObject(xml);
   //int count=jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getInt("totalSize");
  JSONArray records;
 // if(count==1) {
	  records =new  JSONArray();
	   records.put(jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getJSONObject("sobject"));

//  }
//  else {
  // records = jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getJSONArray("records");
 
 // }
  System.out.println("Hey" + records);
  JSONArray result = new JSONArray();
  for (int i = 0; i < records.length(); i++) {
 	 
      JSONObject record = records.getJSONObject(i);
      JSONObject obj = new JSONObject();
      obj.put("name", record.getString("Name"));
      obj.put("Id", record.getString("Id"));
    //  obj.put("type", record.getJSONObject("attributes").getString("type"));
    //  obj.put("AccountNo", record.getString("AccountNumber"));
    //  obj.put("description", record.getString("Description"));
      obj.put("type", "Account");
      result.put(obj);
      
  }
  return result;
	}
else { JSONArray result = new JSONArray();
	return result;
}

	}
	public static JSONArray convertLead(String xml) throws org.json.JSONException {
		
		  if(xml.length()>0) {
		 	 JSONObject jsonObj = XML.toJSONObject(xml);
		   //int count=jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getInt("totalSize");
		  JSONArray records;
		 // if(count==1) {
			  records =new  JSONArray();
			   records.put(jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getJSONObject("records"));

		//  }
		//  else {
		  // records = jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getJSONArray("records");
		 
		 // }
		  System.out.println("Hey" + records);
		  JSONArray result = new JSONArray();
		  for (int i = 0; i < records.length(); i++) {
		 	 
		      JSONObject record = records.getJSONObject(i);
		      JSONObject obj = new JSONObject();
		      obj.put("name", record.getString("Company"));
		      //obj.put("Id", record.getString("Id"));
		    //  obj.put("type", record.getJSONObject("attributes").getString("type"));
		        obj.put("country", record.getString("Country"));
		        obj.put("description", record.getString("Description"));
		        obj.put("source", "salesforce");
		      
		      result.put(obj);
		      
		  }
		  return result;
			}
		else { JSONArray result = new JSONArray();
			return result;
		}

			}
	public static JSONArray convert(String xml) throws org.json.JSONException {
		
	if(xml.length()>0) {
        JSONObject jsonObj = XML.toJSONObject(xml);
    int count=jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getInt("totalSize");
    JSONArray records;
    if(count==1) {
         records =new  JSONArray();
         records.put(jsonObj.getJSONObject("soapenv:Body").getJSONObject("jsonObject").getJSONObject("records"));

    }
    else {
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
      //  obj.put("AccountNo", record.getString("AccountNumber"));
      //  obj.put("description", record.getString("Description"));
        result.put(obj);
        
    }
    return result;
      }
  else { JSONArray result = new JSONArray();
      return result;
  }

}
}
