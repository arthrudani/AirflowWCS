package com.daifukuamerica.wrxj.web.core.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
/**
 * Global access  Google GSON utility 
 * 
 * Author: dystout
 * Created : Jun 13, 2018
 *
 */
public class GsonUtil
{
	public static final Gson PRETTY_PRINT_JSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
	/**
	    * Get the gson column name descriptors to provide the datatables
	    * api with column names and an easy way to name/rename hibernate datatables
	    * via the model objects. 
	    * 
	    * @param o - json object
	    * @return String[] of the column names
	    */
	   public static String[] getObjectColumns(Object o){
		   List<String> columns = new ArrayList<String>(); 

		   String json = PRETTY_PRINT_JSON.toJson(o); 
		   JsonParser parser = new JsonParser(); 
		   JsonElement element = parser.parse(json); 
		   JsonObject obj = element.getAsJsonObject(); 
		   Set<Map.Entry<String, JsonElement>> entries = obj.entrySet(); 
		   for(Map.Entry<String, JsonElement> entry : entries)
		   {
			   columns.add(entry.getKey());
		   }
		   return columns.stream().toArray(String[]::new); //fancy java 8 eh?
	   }
	   

}
