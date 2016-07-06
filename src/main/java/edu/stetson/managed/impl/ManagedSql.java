package edu.stetson.managed.impl;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stetson.managed.JSONValidator;
import edu.stetson.managed.ManagedResource;
/**
 * Simple implementation of a SQL Statement container.
 * @author slfitzge
 *
 */
public class ManagedSql implements ManagedResource {

	private static final String[] validFields = { "statementPath" };

	private String statement;

	private ClassLoader cl = Thread.currentThread().getContextClassLoader();

	@Override
	public void update(JSONObject jo) {

		Logger log = LoggerFactory.getLogger(ManagedSql.class);
		log.debug("loading sql...");
		
		assert JSONValidator.validate(jo, ManagedSql.validFields);
		
		log.debug("path: " + jo.getString("statementPath"));

		try {
			this.statement = IOUtils.toString(cl.getResourceAsStream(jo
					.getString("statementPath")));
		} catch (JSONException | IOException e) {
			log.error(e.getLocalizedMessage());
			return;
		}
		log.debug("loaded successfully! Statement:");
		log.debug(statement);
	}

	@Override
	public Object getUnderlyingObject() {
		return statement;
	}

	@Override
	public Class<? extends Object> getUnderlyingClass() {
		return String.class;
	}

	@Override
	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
	}
	
	public String getStatement(){
		return this.statement;
	}

}
