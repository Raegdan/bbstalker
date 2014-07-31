package org.raegdan.bbstalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/////////////////////////////////////////////////
// DBList - structure supplied to list adapters
/////////////////////////////////////////////////
public class DBList {
	List<HashMap<String, Object>> data;
	String[] fields;
	int[] views;
	int total_count;

	public DBList() {
		data = new ArrayList<HashMap<String, Object>>();
	}
}
