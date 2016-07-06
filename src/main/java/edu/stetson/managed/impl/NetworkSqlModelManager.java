package edu.stetson.managed.impl;

import edu.stetson.managed.ManagedResource;

public class NetworkSqlModelManager extends NetworkResourceManagerImpl {
	private SqlModelManager manager;
	
	public NetworkSqlModelManager(){
		super();
		manager = new SqlModelManager(Thread.currentThread().getContextClassLoader());
	}
	
	public NetworkSqlModelManager(ClassLoader cl){
		super();
		manager = new SqlModelManager(cl);
	}
	
	public NetworkSqlModelManager(SqlModelManager manager){
		super();
		this.manager = manager;
	}


	@Override
	public void reload() {
		manager.reload();
	}

	@Override
	public void reload(String s) {
		manager.reload(s);
		
	}

	@Override
	public void reloadResourceByName(String name) {
		manager.reloadResourceByName(name);		
	}

	@Override
	public void reloadResourceByName(String path, String name) {
		manager.reloadResourceByName(path, name);
	}

	@Override
	public ManagedResource get(String name) {
		return manager.get(name);
	}

	@Override
	public void setClassLoader(ClassLoader cl) {
		manager.setClassLoader(cl);		
	}

	public SqlModelManager getManager() {
		return manager;
	}
}
