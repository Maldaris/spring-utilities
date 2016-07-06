package edu.stetson.managed.impl;

import edu.stetson.managed.ManagedResourceFactory;

public class SqlModelFactory extends ManagedResourceFactory {

	public SqlModelFactory(ClassLoader cl) {
		super(new String[] { "Model" }, cl);
	}

}
