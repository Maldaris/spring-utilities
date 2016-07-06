package edu.stetson.managed.impl;

import org.json.JSONObject;

import edu.stetson.managed.ManagedResource;
import edu.stetson.util.Pair;
/**
 * Pair of Resources, usually a SQL statement and the Model that it populates.
 * @author slfitzge
 *
 * @param <L>
 * @param <R>
 */
public class ManagedResourcePair<L extends ManagedResource, R extends ManagedResource>
		extends Pair<L, R> implements ManagedResource {

	public ManagedResourcePair(L left, R right) {
		super(left, right);
	}

	@Override
	public void update(JSONObject jo) {
		super.getLeft().update(jo);
		super.getRight().update(jo);
	}

	@Override
	public Object getUnderlyingObject() {
		return new Pair<L, R>(super.getLeft(), super.getRight());
	}

	@Override
	public Class<? extends Object> getUnderlyingClass() {
		// TODO Needs to be a better way to do this
		return this.getClass().getSuperclass();
	}

	@Override
	public void setClassLoader(ClassLoader cl) {
		super.getLeft().setClassLoader(cl);
		super.getRight().setClassLoader(cl);
	}

}
