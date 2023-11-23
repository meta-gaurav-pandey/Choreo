package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONObject;

public class InsertParentData extends AbstractMediator{
	public boolean mediate(MessageContext context) {

	 try {
		 Object parentArray=null;
			parentArray =  context.getProperty("parentData");
			if(parentArray!=null) {
				
			JSONArray parent=(JSONArray) parentArray;
		     if(parent.length()>0) {
		
			
		 //JSONArray parentArray=new JSONArray(parentData);
		 
    	 String line, url;
         url = "https://api.zeniagraph.ai/graphql";
    	//  url = "https://localhost:8089/graphql";
         CloseableHttpClient client = null;
         CloseableHttpResponse response = null;

         client = HttpClientBuilder.create().build();
         HttpPost httpPost = new HttpPost(url);
         httpPost.addHeader("Content-Type", "application/json");
         httpPost.addHeader("Accept", "");
         

        String query = "mutation insertParentData($comParent_url: [String],$isCrawl : Boolean!){\r\n"
        		+ "    insertParentData(comParent_url: $comParent_url,isCrawl:$isCrawl){\r\n"
        		+ "        record\r\n"
        		+ "        error\r\n"
        		+ "    }\r\n"
        		+ "}";

        Map<String, Object> variables = new HashMap<String, Object>();
       // Map<String, Object> dataMap = new HashMap<String, Object>();
       
        
        variables.put("isCrawl", true);
        variables.put("comParent_url", parentArray);
       

        // Create an ObjectMapper instance to convert variables to JSON
//        ObjectMapper objectMapper = new ObjectMapper();
//        String variablesJson = objectMapper.writeValueAsString(variables);

        JSONObject jsonobj = new JSONObject();
        jsonobj.put("query", query);
        jsonobj.put("variables", variables);
         System.out.println("Request for Parent data sync  :"+jsonobj);
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
      //  JSONObject SyncRedisObject = new JSONObject(builder.toString());
       
       
		context.setProperty("insertParentDataObject", builder.toString());
			}
			else {
				String result="Either no data present for parent or the file is Missing";
			//	JSONObject SyncRedisObject = new JSONObject(result.toString());
				context.setProperty("insertParentDataObject",result.toString());
			}
		     
			}
			else {
				String result="Either no data present for parent or the file is Missing";
			//	JSONObject SyncRedisObject = new JSONObject(result.toString());
				context.setProperty("insertParentDataObject",result.toString());
			}
    } catch (IOException e) {
        e.printStackTrace();
        context.setProperty("insertParentDataObject",e.getMessage());
    }
	return true;
	 }
}
