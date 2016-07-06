package edu.stetson.managed.impl;

import edu.stetson.managed.ManagedResourceFactory;

/**
 * Generates SQL/Model pairs. Simple configuration provider; see
 * edu.stetson.managed.ManagedResourceFactory for implementation details.
 * 
 * @author slfitzge
 * 
 */
public class SqlModelFactory extends ManagedResourceFactory {

	public SqlModelFactory(ClassLoader cl) {
		super(new String[] { "Model" }, cl);
	}

}
