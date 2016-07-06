package edu.stetson.util;

import java.io.IOException;
import java.util.Properties;

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
