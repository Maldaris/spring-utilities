package edu.stetson.managed.impl;

/**
 * Defines the interface implemented by generated models, for getting fields
 * without the need for user-defined reflection.
 * 
 * @author slfitzge
 * 
 */
public interface GeneratedClassAccessor {
	public void setFieldByIndex(int i, Object o);

	public Object getFieldByIndex(int i);

	public int getIndexFieldCount();

	public String getResultSetName(int idx);

	public String getFieldNameByIndex(int i);
}
