package edu.stetson.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import edu.stetson.springframework.security.userdetails.StetsonUser;

public class Util {

	public static List<String> listify(List<Map<String, Object>> arg, String field) {
		List<String> ret = new ArrayList<String>();
		for (Map<String, Object> m : arg)
			ret.add((String) m.get(field));
		return ret;
	}

	public static Map<String, Object> delistify(List<Map<String, Object>> arg,
			String k, String v) {
		Map<String, Object> ret = new HashMap<String, Object>();
		for (Map<String, Object> m : arg)
			ret.put((String) m.get(k), m.get(v));
		return ret;
	}

	public static String getIdentifier(StetsonUser user) {
		if(user.getId() == null){
			return user.getUsername();
		}
		return user.getId();
	}

	public static int indexOf(Object[] l, Object t){
		for(int i = 0; i < l.length; i++){
			if(l[i].equals(t))
				return i;
		}
		return -1;
	}
	
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
	
	public static Object[] translateJSONArray(JSONArray jarr){
		Object[] ret = new Object[jarr.length()];
		for(int i = 0; i < jarr.length(); i++){
			ret[i] = jarr.get(i);
		}
		return ret;
	}
	
	public static String prependPrefix(String prefix, String field){
		if(field.length() == 1) 
			return prefix + Character.toUpperCase(field.charAt(0));
		return prefix + Character.toUpperCase(field.charAt(0)) + field.substring(1);
	}
}
