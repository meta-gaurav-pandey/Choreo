package com.example;
import com.bordercloud.sparql.*;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.synapse.MessageContext; 
import org.apache.synapse.mediators.AbstractMediator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
public class ZeniaClass extends AbstractMediator { 

	public boolean mediate(MessageContext cont) { 
		try{
			String type=(String) cont.getProperty("Type");
		
		System.out.println("In the class");
		if(type.equals("linkedIn")) {
			linkedin(cont);
		}
		else if(type.equals("dbpedia")) {
			dbpedia(cont);
		}
		else {
			System.out.println("You have entered wrong company name");
			throw new Exception("Please enter valid company");
		}
	}
    catch (Exception e) {
	System.out.println(
			"Error occure while converting into json object either records are empty or retrival failed");

}
		return true;
	}
	
		// TODO Implement your mediation logic here 
	public static void linkedin(MessageContext context) {	
		try {
	            // URL of the API endpoint
		
			    String company=(String) context.getProperty("CompanyName");
			    System.out.println(company);
	            String apiUrl = "https://linkedin-companies-data.p.rapidapi.com/?vanity_name="+company;

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

	            // Get the response code
	           // int responseCode = connection.getResponseCode();
	           // System.out.println("Response Code: " + responseCode);

	            // Read and print the response content
	            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	            String line;
	            StringBuilder response = new StringBuilder();
	            while ((line = reader.readLine()) != null) {
	                response.append(line);
	            }
	            reader.close();

	          //  System.out.println("Response Body: " + response.toString());
	            context.setProperty("Records", response.toString());

	            // Close the connection
	            connection.disconnect();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

		
	}
	
	public static void dbpedia(MessageContext context) {
		  try {
			  String company=(String) context.getProperty("CompanyName");
			    System.out.println(company);
	            URI endpoint = new URI("https://dbpedia.org/sparql/");
	            String querySelect  =
	                    "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
	                    + "        PREFIX dbp: <http://dbpedia.org/property/>\n"
	                    + "        PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
	                    + "\n"
	                    + "        SELECT ?founded ?industry ?headquarter ?locationCity ?numberOfEmployees ?companyType ?rdfsLabel ?foafName ?dbpName ?abstract ?employer\n"
	                    + "        WHERE {\n"
	                    + "        OPTIONAL{ <http://dbpedia.org/resource/"+company+"> dbo:abstract ?abstract. FILTER (LANGMATCHES(LANG(?abstract), \"en\"))}\n"
	                    + "        OPTIONAL{ <http://dbpedia.org/resource/"+company+"> dbo:foundingDate ?founded.}\n"
	                    + "        OPTIONAL{ <http://dbpedia.org/resource/"+company+"> dbo:industry ?industry.}\n"
	                    + "        OPTIONAL{ <http://dbpedia.org/resource/"+company+"> dbo:headquarter ?headquarter.}\n"
	                    + "        OPTIONAL{ <http://dbpedia.org/resource/"+company+"> dbo:type ?companyType.}\n"
	                    + "        OPTIONAL{ <http://dbpedia.org/resource/"+company+"> dbo:locationCity ?locationCity.}\n"
	                    + "        OPTIONAL{ <http://dbpedia.org/resource/"+company+"> dbo:numberOfEmployees ?numberOfEmployees.}\n"
	                    + "        OPTIONAL{ ?employer dbo:employer <http://dbpedia.org/resource/"+company+">.}\n"
	                    + "        OPTIONAL{ <http://dbpedia.org/resource/"+company+"> rdfs:label ?rdfsLabel. FILTER (LANGMATCHES(LANG(?rdfsLabel), \"en\"))}\n"
	                    + "        OPTIONAL{ <http://dbpedia.org/resource/"+company+"> foaf:name ?foafName. FILTER (LANGMATCHES(LANG(?foafName), \"en\"))}\n"
	                    + "        OPTIONAL{ <http://dbpedia.org/resource/"+company+"> dbp:name ?dbpName. FILTER (LANGMATCHES(LANG(?dbpName), \"en\"))}\n"
	                    + "        }";
	           // System.out.println(querySelect);
	            SparqlClient sc = new SparqlClient(false);
	            sc.setEndpointRead(endpoint);
	            SparqlResult sr = sc.query(querySelect);
	          //  sc.printLastQueryAndResult();

	            SparqlResultModel rows_queryPopulationInFrance = sr.getModel();
	
	            if (rows_queryPopulationInFrance.getRowCount() > 0) {
	          
	            	String result= printResult(sr.getModel(),rows_queryPopulationInFrance.getRowCount());   
	                context.setProperty("Result", result.toString());
	            }
	        } catch (URISyntaxException | SparqlClientException e) {
	            System.out.println(e);
	            e.printStackTrace();
	        }
	}
	public static String printResult(SparqlResultModel rs , int size) {
        StringBuilder br=new StringBuilder();
       for (String variable : rs.getVariables()) {
          br.append(String.format("%-"+size+"."+size+"s", variable ) + " | ");
      }
    br.append("\n");
       for (HashMap<String, Object> row : rs.getRows()) {
           for (String variable : rs.getVariables()) {
             br.append(String.format("%-"+size+"."+size+"s", row.get(variable)) + " | ");
           }
           br.append("\n");
       }
       return br.toString();
   }

}
