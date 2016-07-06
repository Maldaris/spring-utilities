package edu.stetson.managed.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ui.Model;

import edu.stetson.managed.ManagedResource;
import edu.stetson.managed.ManagedResourceFactory;
import edu.stetson.managed.ResourceManager;

/**
 * Loads, manages, and provides an interface to SQL/Model pairs defined in JSON.
 * Allows for invocation of the pairs and mapping to Spring Models without the
 * need for extensive boilerplate.
 * 
 * @author slfitzge
 * 
 */
public class SqlModelManager implements ResourceManager {

	private Logger log = LoggerFactory.getLogger(SqlModelManager.class);

	private Map<String, ManagedResourcePair<ManagedSql, ManagedModel>> resources;

	private PathMatchingResourcePatternResolver resolver;

	private ManagedResourceFactory factory;

	private final String defaultPath;

	private ClassLoader cl;

	public SqlModelManager(ClassLoader cl) {
		this.setClassLoader(cl);
		this.factory = new SqlModelFactory(cl);
		defaultPath = "";
		resources = new HashMap<String, ManagedResourcePair<ManagedSql, ManagedModel>>();

	}

	public SqlModelManager(ClassLoader cl, String defaultPath) {
		this.setClassLoader(cl);
		this.factory = new SqlModelFactory(cl);
		this.defaultPath = defaultPath;
		resources = new HashMap<String, ManagedResourcePair<ManagedSql, ManagedModel>>();
	}

	public Model mapTo(String resource, Model model) {
		return mapTo(resource, "", model);
	}

	public Model mapTo(String resource, String namePrefix, Model model) {

		ManagedModel res = resources.get(resource).getRight();
		GeneratedClassAccessor accessor = ((GeneratedClassAccessor) res
				.getUnderlyingObject());
		for (int i = 0; i < accessor.getIndexFieldCount(); i++) {
			String s = accessor.getFieldNameByIndex(i);
			String n = namePrefix
					+ (namePrefix.isEmpty() ? s.charAt(0) : Character
							.toUpperCase(s.charAt(0))) + s.substring(1);
			String v = accessor.getFieldByIndex(i).toString();
			log.debug("mapping " + s + " as " + n + " with value " + v);
			model.addAttribute(n, v);
		}

		return model;
	}

	/**
	 * Calls the SQL associated with the resource name, and loads it into the
	 * associated model, generating the class if necessary.
	 * 
	 * @param name
	 * @param jdbcTemplate
	 * @return the generated object from the model's ResultSetExtractor.
	 */
	public Object invoke(String name, JdbcTemplate jdbcTemplate) {
		ManagedResourcePair<ManagedSql, ManagedModel> pair = this.resources
				.get(name);
		if (pair == null)
			return null;

		return jdbcTemplate.query(pair.getLeft().getStatement(), pair
				.getRight().getResultSetExtractor());
	}

	/**
	 * Calls the SQL associated with the resource name, and loads it into the
	 * associated model, generating the class if necessary.
	 * 
	 * @param name
	 * @param args
	 * @param jdbcTemplate
	 * @return the generated object from the model's ResultSetExtractor.
	 */
	public Object invoke(String name, Object[] args, JdbcTemplate jdbcTemplate) {
		ManagedResourcePair<ManagedSql, ManagedModel> pair = this.resources
				.get(name);
		if (pair == null)
			return null;

		return jdbcTemplate.query(pair.getLeft().getStatement(), args, pair
				.getRight().getResultSetExtractor());
	}

	/**
	 * Clears existing loaded resources, then grabs resources defined by the
	 * path, and loads them in memory, calling their subsequent update()
	 * functions.
	 * 
	 * @param path
	 *            Path to search for resources. Relative to the classloader
	 *            specified by the constructor.
	 */
	@Override
	public void reload(String path) {

		Resource[] res;
		try {
			res = this.resolver.getResources(path + "*.json");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		for (Resource r : res) {
			JSONObject jo;
			try {
				jo = new JSONObject(IOUtils.toString(r.getInputStream()));
			} catch (JSONException | IOException e) {
				e.printStackTrace();
				return;
			}

			assert jo.has("name");

			log.debug("Loading resource: " + jo.getString("name"));

			ManagedSql sql = (ManagedSql) factory.generateResourceFromJSON(
					ManagedSql.class, jo);
			ManagedModel model = (ManagedModel) factory
					.generateResourceFromJSON(ManagedModel.class, jo);

			ManagedResourcePair<ManagedSql, ManagedModel> p = new ManagedResourcePair<ManagedSql, ManagedModel>(
					sql, model);

			this.resources.put(jo.getString("name"), p);

		}
	}

	/**
	 * Calls .reload(path) using the default path defined by the constructor, or
	 * by using "./"
	 */
	public void reload() {
		reload(this.defaultPath);
	}

	@Override
	public void reloadResourceByName(String name) {
		reloadResourceByName(defaultPath, name);
	}

	@Override
	public ManagedResource get(String name) {
		return this.resources.get(name);
	}

	@Override
	public void setClassLoader(ClassLoader cl) {
		this.cl = cl;
		this.resolver = new PathMatchingResourcePatternResolver(cl);
	}

	/**
	 * Explicitly reloads the resource on the path, by its name. Note: Omit the
	 * extension as this is appended automatically, to ensure file type safety.
	 */
	@Override
	public void reloadResourceByName(String path, String name) {
		Resource res;

		try {
			String prefix = "";
			if (!path.equals(""))
				prefix = this.defaultPath + File.separator;
			res = this.resolver.getResources(prefix + name + ".json")[0];
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		JSONObject jo;
		try {
			jo = new JSONObject(IOUtils.toString(res.getInputStream()));
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			return;
		}

		assert jo.has("name");

		log.debug("Loading resource: " + jo.getString("name"));

		ManagedSql sql = (ManagedSql) factory.generateResourceFromJSON(
				ManagedSql.class, jo);
		ManagedModel model = (ManagedModel) factory.generateResourceFromJSON(
				ManagedModel.class, jo);

		ManagedResourcePair<ManagedSql, ManagedModel> p = new ManagedResourcePair<ManagedSql, ManagedModel>(
				sql, model);

		this.resources.put(jo.getString("name"), p);
	}

	public List<String> getResources() {
		List<String> ret = new ArrayList<String>();

		for (String s : this.resources.keySet())
			ret.add(s);

		return ret;
	}

}
