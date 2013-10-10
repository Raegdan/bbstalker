package org.raegdan.bbstalker;

public class RegexpField implements Cloneable {
	public String regexp;
	public String field;
	public Integer priority;

	public RegexpField clone() throws CloneNotSupportedException {
		RegexpField clone = (RegexpField) super.clone();
	return clone;
	}
}
