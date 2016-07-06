package edu.stetson.managed;

import org.json.JSONObject;

public interface ManagedResource {
	public void setClassLoader(ClassLoader cl);
	
	public void update(JSONObject jo);
	
	public Object getUnderlyingObject();
	
	public Class<? extends Object> getUnderlyingClass();
}
