package org.raegdan.bbstalker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;

class RegexpField
{
	String regexp;
	String field;
}

class Wave
{
	List<RegexpField> priorities;
	String waveid;
	
	Wave()
	{
		priorities = new ArrayList<RegexpField>();
	}
}

class Blindbag
{
	List<String> bbids;
	String waveid;
	String name;
	String uniqid;
	String wikiurl;
	
	Blindbag()
	{
		bbids = new ArrayList<String>();
	}
}

class BlindbagDB extends Activity {
	List<Wave> waves;
	List<Blindbag> blindbags;
	
	BlindbagDB() {
		super();
		waves = new ArrayList<Wave>();
		blindbags = new ArrayList<Blindbag>();
	}
	
	protected String StreamToString(InputStream is) throws IOException {
		StringBuilder out = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		for(String line = br.readLine(); line != null; line = br.readLine()) 
		{
			out.append(line);
		}
		br.close();
		return out.toString();
	}
	
	protected Boolean MatchRegexp(String regexp, String s)
	{
		return Pattern.compile(regexp).matcher(s).matches();
	}
	  
	protected JSONObject GetDB(String JSONFile, Context context) throws JSONException, IOException
	{
		AssetManager am = context.getAssets();
	  	InputStream is = am.open("database.json");
	  	JSONObject DB = new JSONObject(StreamToString(is));
	  	is.close();
	  	return DB;
	}
	
	protected void ParseDB(JSONObject DB) throws JSONException
	{
		for (int i = 0; i < DB.getJSONArray("waves").length(); i++)
		{
			Wave w = new Wave();
			w.waveid = DB.getJSONArray("waves").getJSONObject(i).getString("waveid");

			RegexpField rf = new RegexpField();
			for (int j = 0; j < DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").length(); j++)
			{
				rf.field =  DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getString("field");
				rf.regexp =  DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getString("regexp");
				w.priorities.add(rf);
			}
			
			waves.add(w);
		}
		
		for (int i = 0; i < DB.getJSONArray("blindbags").length(); i++)
		{
			Blindbag bb = new Blindbag();
			bb.name = DB.getJSONArray("blindbags").getJSONObject(i).getString("name");
			bb.uniqid = DB.getJSONArray("blindbags").getJSONObject(i).getString("uniqid");
			bb.waveid = DB.getJSONArray("blindbags").getJSONObject(i).getString("waveid");
			bb.wikiurl = DB.getJSONArray("blindbags").getJSONObject(i).getString("wikiurl");
			
			for (int j = 0; j < DB.getJSONArray("blindbags").getJSONObject(i).getJSONArray("bbids").length(); j++)
			{
				bb.bbids.add(DB.getJSONArray("blindbags").getJSONObject(i).getJSONArray("bbids").getString(j));
			}
			
			blindbags.add(bb);
		}
	}
	
	Wave GetWaveInfo(String waveid)
	{
		Wave w = new Wave();
		
		for (int i = 0; i < waves.size(); i++)
		{
			if (waves.get(i).waveid.equalsIgnoreCase(waveid))
			{
				w = waves.get(i);
			}
		}
		
		return w;
	}
	
	Boolean LoadDB(String BBStalkerDatabaseJSONFile, Context context)
	{
		Boolean success = true;
		
		try {
			ParseDB(GetDB(BBStalkerDatabaseJSONFile, context));
		} catch (JSONException e) {
			e.printStackTrace();
			success = false;
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		}
		
		return success;
	}
	
	BlindbagDB LookupDB(String query)
	{
		BlindbagDB OutDB = new BlindbagDB();
		BlindbagDB AddDB = new BlindbagDB();
		
		Boolean MatchMain, MatchAdd;
		
		for (int i = 0; i < blindbags.size(); i++)
		{
			MatchMain = MatchAdd = false;
			
			Blindbag bb = blindbags.get(i);
			
			Wave w = new Wave();
			w = GetWaveInfo(bb.waveid);
			
			String PriorityField = "false";
			for (int j = 0; j < w.priorities.size(); j++)
			{
				if (MatchRegexp(w.priorities.get(j).regexp, query))
				{
					PriorityField = w.priorities.get(j).field;
				}
			}
				
			for (int j = 0; j < bb.bbids.size(); j++)
			{
				if (bb.bbids.get(j).contains(query))
				{
					if (PriorityField.equalsIgnoreCase("bbids"))
					{
						MatchMain = true;
					} else {
						MatchAdd = true;
					}
				}
			}
			if (bb.name.toUpperCase(Locale.ENGLISH).contains(query.toUpperCase(Locale.ENGLISH)))
			{
				if (PriorityField.equalsIgnoreCase("name"))
				{
					MatchMain = true;
				} else {
					MatchAdd = true;
				}
			}
			
			if (MatchMain)
			{
				OutDB.blindbags.add(bb);

			} else if (MatchAdd)
			{
				AddDB.blindbags.add(bb);
			}

			if (!OutDB.waves.contains(w))
			{
				OutDB.waves.add(w);
			}
		}
		  
		OutDB.blindbags.addAll(AddDB.blindbags);
		
		return OutDB;
		
	}
}
