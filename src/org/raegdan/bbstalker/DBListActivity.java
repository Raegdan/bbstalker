package org.raegdan.bbstalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DBListActivity extends Activity implements OnItemClickListener {
	
	  ListView lvDBList;
	  TextView tvHeader;
	  BlindbagDB database = new BlindbagDB();
	  BlindbagDB QueryResult;
	  
	  protected class DBList
	  {
		  List<HashMap<String, Object>> data;
		  String[] fields;
		  int[] views;
		  
		  DBList()
		  {
			  data = new ArrayList<HashMap<String, Object>>();
		  }
	  }
	    	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.dblist);
		  
		  lvDBList = (ListView) findViewById(R.id.lvDBList);
		  tvHeader = (TextView) findViewById(R.id.tvHeader);
		  
		  
		  if (!database.LoadDB("database.json", this))
		  {
			  tvHeader.setText("Error loading JSON database: I/O error or invalid JSON. If you didn't modify, rename or delete the original «database.json» file, please contact me (Raegdan)");
			  return;
		  }
		  		  
		  String query = getIntent().getStringExtra("query");
		  
		  if (query.equalsIgnoreCase(""))
		  {
			  tvHeader.setText("All database");
		  } else {
			  tvHeader.setText("Results for «" + query + "»");
		  }
		  
		  QueryResult = database.LookupDB(query);
		  DBList dblist = PrepareDBList(QueryResult);

		  lvDBList.setOnItemClickListener(this);
		  SimpleAdapter adapter = new SimpleAdapter(this, dblist.data, R.layout.lvdblist, dblist.fields, dblist.views);
		  lvDBList.setAdapter(adapter);
		  
	  }
	  
	  protected DBList PrepareDBList (BlindbagDB database)
	  {
		  DBList dblist = new DBList();
		  dblist.fields = new String[] {"name", "misc", "img1"};
		  dblist.views = new int[] {R.id.lvdblist_text1, R.id.lvdblist_text2, R.id.lvdblist_img1};
		  
		  for (int i = 0; i < database.blindbags.size(); i++)
		  {
			  String bbids = "";
			  for (int j = 0; j < database.blindbags.get(i).bbids.size(); j++)
			  {
				  bbids += database.blindbags.get(i).bbids.get(j);
				  if (j < database.blindbags.get(i).bbids.size() - 1)
				  {
					  bbids += " / ";
				  }
			  }
			  
			  Resources mRes = this.getResources();
			  Integer wavepic = mRes.getIdentifier("w" + database.blindbags.get(i).waveid, "drawable", this.getPackageName());
			  HashMap<String, Object> hmDBList = new HashMap<String, Object>();
			  hmDBList.put("name", database.blindbags.get(i).name);
			  hmDBList.put("misc", "Code: " + bbids);
			  hmDBList.put("img1", wavepic);
			  hmDBList.put("uniqid", database.blindbags.get(i).uniqid);
			  dblist.data.add(hmDBList);
		  }
		  
		  return dblist;
	  }
	  
	  protected void lvDBListItemClicked(Integer index)
	  {
		  Log.d("db list click", QueryResult.blindbags.get(index).uniqid + " - " + QueryResult.blindbags.get(index).name);
	  }
	  
	  protected void ShowBBInfo (BlindbagDB source, Integer index)
	  {
		  /*Log.d("sbbi", "linking textview");
		  TextView pwbbinfo_header = new TextView(this);
		  pwbbinfo_header = (TextView) findViewById(R.id.pwbbinfo_header);
		  Log.d("sbbi", "textview settext");
		  pwbbinfo_header.setText(source.blindbags.get(index).name);
		  */
		  Log.d("sbbi", "creating inflater");
		  LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		  Log.d("sbbi", "creating pw");
		  PopupWindow pw = new PopupWindow(inflater.inflate(R.layout.pwbbinfo, null, false),100,100, true);
		  Log.d("sbbi", "show pw");
		  pw.showAtLocation(this.findViewById(R.id.lvDBList), Gravity.CENTER, 0, 0);
	  }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		switch (arg0.getId())
		{
			case R.id.lvDBList:
				lvDBListItemClicked(arg2);
				break;
		}
	}
}
