package org.raegdan.bbstalker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Blindbag implements Cloneable {
	public final static int RACE_NONPONY = 0;
	public final static int RACE_UNICORN = 1;
	public final static int RACE_PEGASUS = 2;
	public final static int RACE_ALICORN = 3;
	public final static int RACE_EARTHEN = 4;
	
	public List<String> bbids;
	public String waveid;
	public String name;
	public String uniqid;
	public String wikiurl;
	public Integer count;
	public Integer priority;
	public Boolean wanted;
	public int race;
	public int manecolor;
	public int bodycolor;

	public Blindbag() {
		bbids = new ArrayList<String>();
	}

	protected Object GetFieldByName(String name) {
		Field f;
	
		try {
			f = ((Object) this).getClass().getField(name);
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
        return (Blindbag) super.clone();
	}
}
