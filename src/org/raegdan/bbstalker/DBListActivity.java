package org.raegdan.bbstalker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DBListActivity extends Activity {
	
	  JSONObject BBDB;
	  ListView lvDBList;
	  TextView tvHeader;
	  
	  	  
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.dblist);
		  
		  ListView lvBDList = (ListView) findViewById(R.id.lvDBList);
		  TextView tvHeader = (TextView) findViewById(R.id.tvHeader);
		  		  
		  InitializeDB();
		  
		  Intent intent = getIntent();
		  String query = intent.getStringExtra("query");
		  
		  if (query.equalsIgnoreCase(""))
		  {
			  tvHeader.setText("All database");
		  } else {
			  tvHeader.setText("Results for «" + query + "»");
		  }
		  
		  JSONObject DBResult = new JSONObject();
		  
		  try {
			DBResult = QueryDB(query);
			Log.d("DBREsult", DBResult.toString(4));
		  } catch (JSONException e) {
			e.printStackTrace();
		  }
		  
		  List<HashMap<String, Object>> DBList = new ArrayList<HashMap<String, Object>>();
		  
		  try {
			  for (int i = 0; i < DBResult.getJSONArray("main").length(); i++)
			  {
				  String bbids = "";
				  for (int j = 0; j < DBResult.getJSONArray("main").getJSONObject(i).getJSONArray("bbids").length(); j++)
				  {
					  bbids += DBResult.getJSONArray("main").getJSONObject(i).getJSONArray("bbids").getString(j);
					  if (j < DBResult.getJSONArray("main").getJSONObject(i).getJSONArray("bbids").length() - 1)
					  {
						  bbids += " / ";
					  }
				  }
				  
				  Resources mRes = this.getResources();
				  Integer wavepic = mRes.getIdentifier("w" + DBResult.getJSONArray("main").getJSONObject(i).getString("waveid"), "drawable", this.getPackageName());

				  HashMap<String, Object> hm = new HashMap<String, Object>();
				  hm.put("name", DBResult.getJSONArray("main").getJSONObject(i).getString("name"));
				  hm.put("misc", "Code: " + bbids);
				  hm.put("img1", wavepic);
				  DBList.add(hm);
				  
			  }
		  } catch (JSONException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  
		  SimpleAdapter adapter = new SimpleAdapter(this, DBList, R.layout.lvdblist, new String[] {"name", "misc", "img1"}, new int[] {R.id.lvdblist_text1, R.id.lvdblist_text2, R.id.lvdblist_img1});
		  lvBDList.setAdapter(adapter);
	  }
	  
	  protected void ShowToast(String text)
	  {
		  Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT); 
		  toast.show(); 
	  }
	  
	  protected Boolean MatchRegexp(String regexp, String s)
	  {
		  return Pattern.compile(regexp).matcher(s).matches();
	  }
	  
	  protected String StreamToString(InputStream is) throws IOException {
		  StringBuilder out = new StringBuilder();
		  BufferedReader br = new BufferedReader(new InputStreamReader(is));
		  for(String line = br.readLine(); line != null; line = br.readLine()) 
		    out.append(line);
		  br.close();
		  return out.toString();
		}
	  
	  protected String ReadDB(Activity activity)
	  {
	    AssetManager am = activity.getAssets();
	    String s = "";
	  	try {
		  	InputStream is = am.open("database.json");
		  	s = StreamToString(is);
		  	is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	  	return s;
	  }
	  
	  protected void InitializeDB()
	  {

			try {
				BBDB = new JSONObject(ReadDB(this));
			} catch (JSONException e) {
				e.printStackTrace();
			}
	  }
	  
	  protected JSONObject QueryDB(String query) throws JSONException
	  {
		JSONObject OutDB = new JSONObject();
		JSONArray MainDB, AddDB;
		MainDB = new JSONArray();
		AddDB = new JSONArray();
		
		Boolean MatchMain, MatchAdd;
		String Priority;
		
		for (int i = 0; i < BBDB.getJSONArray("blindbags").length(); i++)
		{
			MatchMain = MatchAdd = false;
			
			JSONObject record = BBDB.getJSONArray("blindbags").getJSONObject(i);
			
			JSONArray priorities = BBDB.getJSONObject("waves").getJSONObject(record.getString("waveid")).getJSONArray("priority");
			Priority = "false";
			for (int j = 0; j < priorities.length(); j++)
			{
				if (MatchRegexp(priorities.getJSONObject(j).getString("regexp"), query))
				{
					Priority = priorities.getJSONObject(j).getString("field");
				}
			}
				
			JSONArray bbids = record.getJSONArray("bbids");
			for (int j = 0; j < bbids.length(); j++)
			{
				if (bbids.getString(j).contains(query))
				{
					if (Priority.equalsIgnoreCase("bbids"))
					{
						MatchMain = true;
					} else {
						MatchAdd = true;
					}
				}
			}
			if (record.getString("name").toUpperCase(Locale.ENGLISH).contains(query.toUpperCase(Locale.ENGLISH)))
			{
				if (Priority.equalsIgnoreCase("name"))
				{
					MatchMain = true;
				} else {
					MatchAdd = true;
				}
			}
			
			if (MatchMain)
			{
				MainDB.put(record);
				continue;
			} else if (MatchAdd)
			{
				AddDB.put(record);
			}
		}
		  
		OutDB.put("main", MainDB);
		OutDB.put("additional", AddDB);
		
		return OutDB;		  
	  }
	  
	  
}
