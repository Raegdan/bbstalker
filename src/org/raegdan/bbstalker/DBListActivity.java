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
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DBListActivity extends Activity implements OnItemClickListener {
	
	  ListView lvDBList;
	  TextView tvDBHeader;
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
		  tvDBHeader = (TextView) findViewById(R.id.tvDBHeader);
		  
		  
		  if (!database.LoadDB("database.json", this))
		  {
			  tvDBHeader.setText("Error loading JSON database: I/O error or invalid JSON. If you didn't modify, rename or delete the original «database.json» file, please contact me (Raegdan)");
			  return;
		  }
		  		  
		  String query = getIntent().getStringExtra("query");
		  
		  if (query.equalsIgnoreCase(""))
		  {
			  tvDBHeader.setText("All database");
		  } else {
			  tvDBHeader.setText("Results for «" + query + "»");
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
		  dblist.views = new int[] {R.id.tvLVDBListName, R.id.tvLVDBListMisc, R.id.ivVLDBListWavePic};
		  
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
			  
			  Integer wavepic = this.getResources().getIdentifier("w" + database.blindbags.get(i).waveid, "drawable", this.getPackageName());
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
		  ShowBBInfo(index);
	  }
	  
	  protected void ShowBBInfo (Integer index)
	  {
		  LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		  View view = inflater.inflate(R.layout.pwbbinfo, null);
		  RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.rlPWBBInfo);
		  Blindbag bb = QueryResult.blindbags.get(index);
		  
		  TextView tvPWBBInfoName = (TextView) layout.findViewById(R.id.tvPWBBInfoName);
		  //Log.d("x", layout.findViewById(R.id.tvPWBBInfoName).getClass().toString());
		  //ImageView ivPWBBInfoWavePic = (ImageView) layout.findViewById(R.id.ivPWBBInfoWavePic);
		  ImageView ivPWBBInfoPonyPic = (ImageView) layout.findViewById(R.id.ivPWBBInfoPonyPic);
		  
		  tvPWBBInfoName.setText(bb.name);
		  //ivPWBBInfoWavePic.setImageResource(this.getResources().getIdentifier("w" + bb.waveid, "drawable", this.getPackageName()));
		  ivPWBBInfoPonyPic.setImageResource(this.getResources().getIdentifier("bb" + bb.uniqid, "drawable", this.getPackageName()));
		  
		  PopupWindow pw = new PopupWindow(this);
		  pw.setContentView(layout);
	      pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
	      pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
	      pw.setFocusable(true);
		  pw.showAtLocation(view, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
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
