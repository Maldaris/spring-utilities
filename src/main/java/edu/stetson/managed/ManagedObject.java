package edu.stetson.managed;

import java.lang.reflect.Method;

public interface ManagedObject extends ManagedResource {
	public String[] getFields();
	
	public Object getField(String s);
	
	public Object callMethod(String s, Class<?>[] types, Object...args);

	public Method[] getMethods();
}
