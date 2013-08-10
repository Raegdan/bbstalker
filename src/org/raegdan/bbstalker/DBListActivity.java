package org.raegdan.bbstalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DBListActivity extends Activity {
	
	  ListView lvDBList;
	  TextView tvHeader;
	    	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.dblist);
		  
		  ListView lvDBList = (ListView) findViewById(R.id.lvDBList);
		  TextView tvHeader = (TextView) findViewById(R.id.tvHeader);
		  
		  BlindbagDB database = new BlindbagDB();
		  if (!database.LoadDB("database.json", this))
		  {
			  tvHeader.setText("Error loading JSON database: I/O error or invalid JSON. If you didn't modify, rename or delete the original «database.json» file, please contact me (Raegdan)");
			  return;
		  }
		  		  
		  Intent intent = getIntent();
		  String query = intent.getStringExtra("query");
		  
		  if (query.equalsIgnoreCase(""))
		  {
			  tvHeader.setText("All database");
		  } else {
			  tvHeader.setText("Results for «" + query + "»");
		  }
		  
		  BlindbagDB QueryResult = database.LookupDB(query);
		  
		  final List<HashMap<String, Object>> DBList = new ArrayList<HashMap<String, Object>>();
		  for (int i = 0; i < QueryResult.blindbags.size(); i++)
		  {
			  String bbids = "";
			  for (int j = 0; j < QueryResult.blindbags.get(i).bbids.size(); j++)
			  {
				  bbids += QueryResult.blindbags.get(i).bbids.get(j);
				  if (j < QueryResult.blindbags.get(i).bbids.size() - 1)
				  {
					  bbids += " / ";
				  }
			  }
			  
			  Resources mRes = this.getResources();
			  Integer wavepic = mRes.getIdentifier("w" + QueryResult.blindbags.get(i).waveid, "drawable", this.getPackageName());
			  HashMap<String, Object> hmDBList = new HashMap<String, Object>();
			  hmDBList.put("name", QueryResult.blindbags.get(i).name);
			  hmDBList.put("misc", "Code: " + bbids);
			  hmDBList.put("img1", wavepic);
			  hmDBList.put("uniqid", QueryResult.blindbags.get(i).uniqid);
			  DBList.add(hmDBList);
		  }


		  lvDBList.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			    {
					  DBList.get(position).get("uniqid").toString();					  
			    }
		  });
		  
		  SimpleAdapter adapter = new SimpleAdapter(this, DBList, R.layout.lvdblist, new String[] {"name", "misc", "img1"}, new int[] {R.id.lvdblist_text1, R.id.lvdblist_text2, R.id.lvdblist_img1});
		  lvDBList.setAdapter(adapter);
		  
	  }
}
