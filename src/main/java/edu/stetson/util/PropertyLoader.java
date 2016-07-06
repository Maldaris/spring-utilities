package edu.stetson.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Loads properties from .properties files with little overhead
 * Useful when only one or two properties are needed at class load.
 * @author slfitzge
 *
 */

public class PropertyLoader {
	public static String get(ClassLoader l, String src, String prop){
		Properties p = new Properties();
		try {
			p.load(l.getResourceAsStream(src));
		} catch (IOException e) {
			return "";
		}
		return p.getProperty(prop);
	}
}
