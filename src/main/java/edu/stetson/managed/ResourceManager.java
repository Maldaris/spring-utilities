package edu.stetson.managed;

public interface ResourceManager {
	public void reload();
	
	public void reload(String s);
	
	public void reloadResourceByName(String name);
	
	public void reloadResourceByName(String path, String name);
	
	public ManagedResource get(String name);
	
	public void setClassLoader(ClassLoader cl);
}
