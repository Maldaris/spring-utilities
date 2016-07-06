package edu.stetson.managed;

import org.springframework.ui.Model;

public interface ModelMapper<T extends ManagedResource> {
	public Model mapObjectToModel(Model model, String name, T res);
	
	public Model unwrapObjectToModel(Model model, T res);
}
