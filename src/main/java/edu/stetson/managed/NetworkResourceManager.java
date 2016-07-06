package edu.stetson.managed;

public interface NetworkResourceManager extends ResourceManager {
	
	public boolean startListener();
	
	public void stopListener();
	
	public boolean setPort(Integer i);
}
