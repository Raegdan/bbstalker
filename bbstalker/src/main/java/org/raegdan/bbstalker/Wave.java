package org.raegdan.bbstalker;

import java.util.ArrayList;
import java.util.List;

public class Wave implements Cloneable {
	public List<RegexpField> priorities;
	public String waveid;
	public String year;
	public String format;
	public String name;

	public Wave() {
		priorities = new ArrayList<RegexpField>();
	}

	@SuppressWarnings("unchecked")
	public Wave clone() throws CloneNotSupportedException {
	Wave clone = (Wave) super.clone();
	clone.priorities = (List<RegexpField>) ((ArrayList<RegexpField>) priorities).clone();
	return clone;
	}
}
