package edu.stetson.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * 
 * SQL Statement Loader
 * 
 * Loads the .sql files in the provided path, 
 * using the provided classloader, by matching to a path.
 * 
 * @version 0.5.0
 * 
 * @author slfitzge
 *
 */


public class SqlStatementLoader {

	public Map<String, String> resources;

	private PathMatchingResourcePatternResolver resolver;

	private ClassLoader cl;

	private Logger logger;

	public SqlStatementLoader(ClassLoader contextCL) {
		this.cl = contextCL;
		this.resolver = new PathMatchingResourcePatternResolver(this.cl);
		this.resources = new HashMap<String, String>();
	}

	public SqlStatementLoader(ClassLoader contextCL, Logger log) {
		this.cl = contextCL;
		this.resolver = new PathMatchingResourcePatternResolver(this.cl);
		this.resources = new HashMap<String, String>();
		this.logger = log;
	}

	public boolean hasResource(String resourceName) {
		return this.resources.containsKey(resourceName);
	}

	public String get(String resourceName) {
		final String ret;

		if (this.resources.containsKey(resourceName)) {
			ret = this.resources.get(resourceName);
		} else {
			ret = null;
		}

		return ret;
	}

	public int loadStatementsByPattern(String path) {
		int ret = 0;
		Resource[] res;
		String pattern = path + "*.sql";
		try {
			res = resolver.getResources(pattern);
			if(res.length == 0){
				throw new Exception("No such resources matching pattern.");
			}
		} catch (IOException e) {
			if (logger != null)
				logger.error(e.getMessage());
			return ret;
		} catch (Exception e) {
			if (logger != null)
				logger.error(e.getMessage());
			return ret;
		}
		for (Resource r : res) {
			String filename = r.getFilename();
			filename = filename.split("\\.")[0];
			try {
				this.resources.put(filename,
						IOUtils.toString(r.getInputStream()));
			} catch (IOException e) {
				if (logger != null)
					logger.error(e.getMessage());
			}
			ret++;
		}

		return ret;
	}

}
