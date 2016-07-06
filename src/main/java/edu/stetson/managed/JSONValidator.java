package edu.stetson.managed;

import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.stetson.util.Util;

public class JSONValidator {
	
	/**
	 * Ensures fields exist within JSON Object, ignoring extra fields.
	 * @param jo
	 * @param fields
	 */
	
	public static boolean validate(JSONObject jo, String[] fields){
		for(String s : fields){
			if(!jo.has(s))
				return false;
		}
		
		return true;
	}
	/**
	 * Ensures only the fields passed in exist as fields within the JSON Object.
	 * @param jo
	 * @param fields
	 * @return
	 */
	
	public static boolean validateStrict(JSONObject jo, String[] fields){
		List<String> jof = Arrays.asList(JSONObject.getNames(jo));
		for(String s : fields){
			if(!jof.remove(s)) return false;
		}
		
		return jof.size() == 0;
	}
	
	
	/**
	 * Ensures the array denoted by name contains all of the objects in question, ignoring extraneous objects.
	 * @param jo
	 * @param name
	 * @param objects
	 * @return
	 */
	public static boolean validateArray(JSONObject jo, String name, Object...objects){
		JSONArray arr = jo.getJSONArray(name);
		if(arr.length() == 0 && objects.length > 0) return false;
		
		for(int i = 0; i < arr.length(); i++){
			if(Util.indexOf(objects, arr.get(i)) == -1)
					return false;
		}
		
		return true;
	}
	/**
	 * Essentially ensures the array denoted by name is equal to the list of Objects.
	 * @param jo
	 * @param name
	 * @param objects
	 * @return
	 */
	public static boolean validateArrayStrict(JSONObject jo, String name, Object...objects){
		List<Object> arr = Arrays.asList(Util.translateJSONArray(jo.getJSONArray(name)));
		if(arr.size() != objects.length) return false;
		for(Object o : objects)
			if(!arr.remove(o)) return false;
		
		return arr.size() == 0;
		
	}
}
