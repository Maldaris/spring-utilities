package edu.stetson.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

public class Util {

	/**
	 * Converts a List<Map<String, Object>> into a List<String>.
	 * Useful with JDBC when querying for a single column with many rows.
	 * @param arg
	 * @param field
	 * @return
	 */
	
	public static List<String> listify(List<Map<String, Object>> arg, String field) {
		List<String> ret = new ArrayList<String>();
		for (Map<String, Object> m : arg)
			ret.add((String) m.get(field));
		return ret;
	}

	/**
	 * Converts a List<Map<String, Object>> into a Map<String, Object>
	 * Useful with JDBC when querying for K,V pairs represented across multiple rows.
	 * @param arg
	 * @param k
	 * @param v
	 * @return
	 */
	
	public static Map<String, Object> delistify(List<Map<String, Object>> arg,
			String k, String v) {
		Map<String, Object> ret = new HashMap<String, Object>();
		for (Map<String, Object> m : arg)
			ret.put((String) m.get(k), m.get(v));
		return ret;
	}

	/**
	 * simple indexOf using Object.equals()
	 * @param l
	 * @param t
	 * @return 0 <= x < l.length or -1 if not found
	 */
	
	public static int indexOf(Object[] l, Object t){
		for(int i = 0; i < l.length; i++){
			if(l[i].equals(t))
				return i;
		}
		return -1;
	}
	
	/**
	 * finds the index of a object using a function specified by comparatorField in the object list
	 * ComparatorField must name a function that returns a boolean value.
	 * @param l List of objects to iterate over
	 * @param t Object to find the index of
	 * @param comparatorField
	 * @return 0 <= x < l.length, or -1 if not found
	 */
	
	public static int indexOf(Object[] l, Object t, String comparatorField) {
		assert l != null;
		assert l.length != 0;
		
		Method m;
		try {
			m = l[0].getClass().getMethod(comparatorField, t.getClass() );
			for(int i = 0; i < l.length; i++){
				Boolean res = (Boolean) m.invoke(l[i], t);
				if(res.booleanValue())
					return i;
			}
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return -1;
		} 

		return -1;
	}
	
	/**
	 * Converts a JSONArray object into a native array of objects.
	 * @param jarr
	 * @return
	 */
	
	public static Object[] translateJSONArray(JSONArray jarr){
		Object[] ret = new Object[jarr.length()];
		for(int i = 0; i < jarr.length(); i++){
			ret[i] = jarr.get(i);
		}
		return ret;
	}
	
	
	/**
	 * Prepends a prefix to a string, and formats using camel casing.
	 * @param prefix
	 * @param field 
	 * @return the combined string
	 */
	public static String prependPrefix(String prefix, String field){
		if(field.length() == 0) return prefix;
		if(field.length() == 1) 
			return prefix + Character.toUpperCase(field.charAt(0));
		return prefix + Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}
}
