package edu.stetson.managed.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.ui.Model;

import edu.stetson.managed.JSONValidator;
import edu.stetson.managed.ManagedObject;
import edu.stetson.util.Util;

/**
 * Generates Java Classes as described in JSON input using Javassist, and
 * provides basic interfaces for accessing the contained object's fields and
 * methods.
 * 
 * @author slfitzge
 * 
 * TODO: Better version management & PermGen awareness in update();
 * 
 */
public class ManagedModel extends ManagedModelMapper implements ManagedObject {

	private Logger log = LoggerFactory.getLogger(ManagedModel.class);

	private Class<? extends GeneratedClassAccessor> clazz;

	private ClassLoader cl = Thread.currentThread().getContextClassLoader();

	private Object gen;

	private static final String[] jsonValidFields = { "type", "name", "fields",
			"additionalDependencies" };
	private static final String jsonValidType = "Model";
	private static final String[] fieldAttributes = { "access", "type", "name",
			"generateAccessors", "resultSetExtractorName" };
	public static String[] dependencies = { "java.util.List",
			"java.sql.SQLException", "java.sql.ResultSet",
			"org.springframework.jdbc.core.ResultSetExtractor",
			"org.springframework.dao.DataAccessException",
			"org.springframework.jdbc.core.ResultSetExtractor" };

	@Deprecated
	@Override
	public String[] getFields() {
		Field[] f = clazz.getDeclaredFields();
		String[] ret = new String[f.length];

		for (int i = 0; i < f.length; i++)
			ret[i] = f[i].getName();

		return ret;
	}

	@Deprecated
	@Override
	public Object getField(String s) {
		String[] f = this.getFields();
		int idx = Util.indexOf(f, s);
		return idx > -1 ? f[idx] : null;
	}

	public Object callMethod(String name, Class<?>[] params, Object... args) {
		try {
			return gen.getClass().getDeclaredMethod(name, params)
					.invoke(gen, args);
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			return e;
		}
	}

	public Object callMethod(String name) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		return gen.getClass().getDeclaredMethod(name, new Class<?>[] {})
				.invoke(gen, new Object[] {});
	}

	public ResultSetExtractor<GeneratedClassAccessor> getResultSetExtractor() {
		return new ResultSetExtractor<GeneratedClassAccessor>() {

			@Override
			public GeneratedClassAccessor extractData(ResultSet rs)
					throws SQLException, DataAccessException {
				GeneratedClassAccessor ret = (GeneratedClassAccessor) gen;
				rs.next();
				for (int i = 0; i < ret.getIndexFieldCount(); i++) {

					Object o = rs.getObject(ret.getResultSetName(i));

					log.debug("field " + i + "/" + ret.getResultSetName(i)
							+ ": " + o.toString());

					ret.setFieldByIndex(i, o);

					log.debug("set field " + i + " to "
							+ ret.getFieldByIndex(i));
				}
				rs.close();
				return ret;
			}

		};
	}

	private int getLatestVersionNumberFromPool(String className, ClassPool pool) {
		return getLatestVersionNumberFromPool(className, pool, 0);
	}

	private int getLatestVersionNumberFromPool(String className,
			ClassPool pool, int idx) {

		CtClass cc = pool.getOrNull(className + "Version0");
		log.debug("LF> Latest Class\nstarting at Version0...");
		while (cc != null) {
			idx++;
			cc = pool.getOrNull(className + "Version" + idx);
		}
		log.debug("Latest Version of " + className + ": " + (idx));

		if (pool.getOrNull(className + "Version" + (idx + 1)) != null)
			return getLatestVersionNumberFromPool(className, pool, idx + 1);

		return idx;
	}

	@Override
	public void update(JSONObject jo) {

		log.debug("creating class...");

		if (!JSONValidator.validate(jo, ManagedModel.jsonValidFields)) {
			log.error("invalid json structure!");
			return;
		}

		if (!jo.getString("type").equals(ManagedModel.jsonValidType)) {
			log.error("invalid json type!");
			return;
		}

		clazz = null;
		gen = null;

		ClassPool pool = new ClassPool();

		pool.appendClassPath(new LoaderClassPath(cl));

		List<String> modDep = Arrays.asList(ManagedModel.dependencies);

		JSONArray nDepArr = jo.getJSONArray("additionalDependencies");
		for (int i = 0; i < nDepArr.length(); i++) {
			modDep.add(nDepArr.getString(i));
		}
		modDep.toArray(ManagedModel.dependencies);

		for (String dep : ManagedModel.dependencies) {
			pool.importPackage(dep);
		}

		CtClass cc = pool
				.makeClass(jo.getString("name")
						+ "Version"
						+ (getLatestVersionNumberFromPool(jo.getString("name"),
								pool) + 1));

		CtMethod rig;
		String rigSrc = "public Object getFieldByIndex(int i){ \n switch(i) {\n";

		CtMethod ris;
		String risSrc = "public void setFieldByIndex(int i, Object o){ \n switch(i){\n";

		CtMethod rsi;
		String rsiSrc = "public String getResultSetName(int i){\n switch(i){\n";

		CtMethod rin;
		String rinSrc = "public String getFieldNameByIndex(int i){ \n switch(i) {\n";

		JSONArray fields = jo.getJSONArray("fields");
		for (int i = 0; i < fields.length(); i++) {
			JSONObject field = fields.getJSONObject(i);
			JSONValidator.validate(field, ManagedModel.fieldAttributes);

			CtField cf;
			String src = "";

			final String name = field.getString("name");
			final String access = field.getString("access");
			final String type = field.getString("type");
			final String resultSet = field.getString("resultSetExtractorName");

			if (field.has("decorators")) {
				JSONArray decorators = field.getJSONArray("decorators");
				for (int j = 0; j < decorators.length(); j++) {
					src += decorators.getString(j) + "\n";
				}
			}

			src += access;
			src += " " + type;
			src += " " + name + ";";

			try {
				cf = CtField.make(src, cc);
				cc.addField(cf);
			} catch (CannotCompileException e) {
				log.error(e.getMessage());
				return;
			}

			CtMethod cmGet, cmSet;
			String getSrc = "public ", setSrc = "public void ";
			getSrc += field.getString("type");
			getSrc += " " + Util.prependPrefix("get", name);
			getSrc += "(){ return " + field.getString("name") + "; }";
			setSrc += Util.prependPrefix("set", name);
			setSrc += "(" + type + " arg){ this." + name + " = arg; }";

			try {
				cmGet = CtMethod.make(getSrc, cc);
				cmSet = CtMethod.make(setSrc, cc);

				cc.addMethod(cmSet);
				cc.addMethod(cmGet);
			} catch (CannotCompileException e) {
				log.error(e.getMessage());
				return;
			}

			rigSrc += "case " + i + ":\nreturn this." + name + ";\n";
			risSrc += "case " + i + ": this." + name + " = (" + type
					+ ") o; break;\n";
			rsiSrc += "case " + i + ":\nreturn \"" + resultSet + "\";\n";
			rinSrc += "case " + i + ":\nreturn \"" + name + "\";\n";
		}

		rigSrc += "} return null; }";
		risSrc += "} }";
		rsiSrc += "} return \"\"; }";
		rinSrc += "} return \"\"; }";

		CtMethod ric;
		String ricSrc = "public int getIndexFieldCount() { return "
				+ fields.length() + "; }";

		try {
			rig = CtMethod.make(rigSrc, cc);
			ris = CtMethod.make(risSrc, cc);
			ric = CtMethod.make(ricSrc, cc);
			rsi = CtMethod.make(rsiSrc, cc);
			rin = CtMethod.make(rinSrc, cc);
			cc.addMethod(ris);
			cc.addMethod(rig);
			cc.addMethod(ric);
			cc.addMethod(rsi);
			cc.addMethod(rin);
		} catch (CannotCompileException e) {
			log.error(e.getReason());
			log.debug(rigSrc);
			log.debug(risSrc);
			log.debug(ricSrc);
			log.debug(rsiSrc);
			log.debug(rinSrc);
			return;
		}

		pool.importPackage("edu.stetson.managed.impl.GeneratedClassAccessor");
		try {
			cc.addInterface(pool
					.get("edu.stetson.managed.impl.GeneratedClassAccessor"));
		} catch (NotFoundException e1) {
			log.error(e1.getLocalizedMessage());
			return;
		}

		try {
			clazz = cc.toClass();
		} catch (CannotCompileException e) {
			log.error(e.getReason());
			return;
		}

		log.debug("Classname: " + clazz.getCanonicalName() + "\nFields...");
		for (Field f : clazz.getDeclaredFields()) {
			log.debug(f.getName());
		}

		if (jo.has("_skipinstance") && jo.getBoolean("_skipinstance"))
			return; // skip creating an object for use with extensions of
					// managedmodel
		try {
			gen = clazz.getConstructor(new Class<?>[] {}).newInstance(
					new Object[] {});
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			log.error(e.getMessage());
			return;
		}
	}

	@Override
	public Object getUnderlyingObject() {
		return gen;
	}

	@Override
	public Class<? extends Object> getUnderlyingClass() {
		return clazz;
	}

	public Model mapObjectToModel(Model model, String name) {
		super.mapObjectToModel(model, name, this);
		return model;
	}

	public Model unwrapObjectToModel(Model model) {
		super.unwrapObjectToModel(model, this);
		return model;
	}

	@Override
	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
	}

	@Override
	public Method[] getMethods() {
		return clazz.getDeclaredMethods();
	}
}
