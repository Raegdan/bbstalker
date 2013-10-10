package org.raegdan.bbstalker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Blindbag implements Cloneable {
	public List<String> bbids;
	public String waveid;
	public String name;
	public String uniqid;
	public String wikiurl;
	public Integer count;
	public Integer priority;
	public Boolean wanted;

	public Blindbag() {
		bbids = new ArrayList<String>();
	}

	protected Object GetFieldByName(String name) {
		Field f;
	
		try {
			f = this.getClass().getField(name);
			return f.get(this);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Blindbag clone() throws CloneNotSupportedException {
	Blindbag clone = (Blindbag) super.clone();
	return clone;
	}
}
