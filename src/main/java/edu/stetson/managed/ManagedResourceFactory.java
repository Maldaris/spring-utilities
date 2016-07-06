package edu.stetson.managed;

import java.lang.reflect.InvocationTargetException;

import org.json.JSONObject;

import edu.stetson.util.Util;

public class ManagedResourceFactory {
	
	private String[] acceptedTypes;
	
	private ClassLoader cl = Thread.currentThread().getContextClassLoader();
	
	public ManagedResourceFactory(String[] acceptedTypes){
		this.acceptedTypes = acceptedTypes;
	}
	
	public ManagedResourceFactory(String[] acceptedTypes, ClassLoader cl){
		this.acceptedTypes = acceptedTypes;
		this.cl = cl;
	}
	
	public ManagedResource generateResourceFromJSON(Class<? extends ManagedResource> impl,
			JSONObject jo) {

		assert Util.indexOf(acceptedTypes, jo.get("types")) > -1;
		
		ManagedResource ret;
		try {
			ret = (ManagedResource) impl.getConstructors()[0]
					.newInstance(new Object[] {});
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| SecurityException e) {
			e.printStackTrace();
			return null;
		}

		ret.setClassLoader(cl);
		
		ret.update(jo);

		return ret;

	}
}
