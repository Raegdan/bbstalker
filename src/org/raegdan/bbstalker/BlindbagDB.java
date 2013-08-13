package org.raegdan.bbstalker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.util.Log;

class RegexpField
{
	String regexp;
	String field;
	Integer priority;
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
	public List<String> bbids;
	public String waveid;
	public String name;
	public String uniqid;
	public String wikiurl;
	public Integer count;
	public Integer priority;
	
	Blindbag()
	{
		bbids = new ArrayList<String>();
	}
	
	protected Object GetFieldByName(String name)
	{
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
}

class BlindbagDB extends Activity {
	
	/////////////////
	// P U B L I C //
	/////////////////

	List<Wave> waves;
	List<Blindbag> blindbags;

	BlindbagDB() {
		super();
		waves = new ArrayList<Wave>();
		blindbags = new ArrayList<Blindbag>();
	}
	
	Boolean LoadDB(Context context)
	{
		Boolean success = true;
		
		try {
			ParseDB(GetDB(context));
			ParseCollection(GetCollection(context));

		} catch (JSONException e) {
			e.printStackTrace();
			success = false;
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		}
		
		
		
		return success;
	}
	
	@SuppressWarnings("unchecked")
	BlindbagDB LookupDB(String query)
	{
		BlindbagDB OutDB = this;
		
//		Log.d("Lookup", "Query: '" + query + "'");
		
		for (int i = 0; i < OutDB.blindbags.size(); i++)
		{
			Blindbag bb = OutDB.blindbags.get(i);
			
			Wave w = new Wave();
			w = GetWaveInfo(bb.waveid, OutDB);
			
			Integer Priority = 0;
			for (int j = 0; j < w.priorities.size(); j++)
			{
//				Log.d("Lookup", "j = " + Integer.toString(j) + ", Regexp: '" + w.priorities.get(j).regexp + ", " + w.priorities.get(j).priority.toString() + "'");
				
				if (MatchRegexp(w.priorities.get(j).regexp, query.toUpperCase(Locale.ENGLISH)) && (w.priorities.get(j).priority > Priority))
				{
//					Log.d("Lookup", "Regexp Match! Field = '" + w.priorities.get(j).field + "'");
					String PriorityField = w.priorities.get(j).field;
					
					Object field = bb.GetFieldByName(PriorityField);
					// Exception in GFBN
					if (field == null)
					{
//						Log.d("Lookup", "Field is null.");
						continue;
					}
					
					// Field is string (e.g. name)
					else if (field instanceof String)
					{
//						Log.d("Lookup", "Field is string.");
						if (((String) field).toUpperCase(Locale.ENGLISH).contains(query.toUpperCase(Locale.ENGLISH)))
						{
//							Log.d("Lookup", "Match! Priority = " + w.priorities.get(j).priority.toString());
							Priority = w.priorities.get(j).priority;
						}							
					}
					
					// Field is list (e.g. bbids)
					else if (field instanceof List)
					{
//						Log.d("Lookup", "Field is list.");
						for (int k = 0; k < bb.bbids.size(); k++)
						{
							if (((List<String>) field).get(k).toUpperCase(Locale.ENGLISH).contains(query.toUpperCase(Locale.ENGLISH)))
							{
//								Log.d("Lookup", "Match! Priority = " + w.priorities.get(j).priority.toString());
								Priority = w.priorities.get(j).priority;
							}
						}							
					}
				}
			}
			
			bb.priority = Priority;
			
//			Log.d("Out", bb.name + " --- " + bb.waveid + " --- " + Integer.toString(bb.priority));
			OutDB.blindbags.set(i, bb);
		}
		
		OutDB = PrioritySort(OutDB);
		
		return OutDB;
		
	}
	
	Boolean CommitDB (Context context)
	{
		JSONArray ja = new JSONArray();
		
		for (int i = 0; i < blindbags.size(); i++)
		{
			Integer count = blindbags.get(i).count;
			
			if (count > 0)
			{
				try {
					ja.put(new JSONObject().put("uniqid", blindbags.get(i).uniqid).put("count", count));
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		
		SharedPreferences sp = ((Activity) context).getPreferences(MODE_PRIVATE);
	Editor ed = sp.edit();
	ed.putString(COLLECTION_PREF_ID, ja.toString());
	ed.commit();
	
	return true;
	}
	
	Blindbag GetBlindbagByUniqID(String uniqid)
	{
		for (int i = 0; i < blindbags.size(); i++)
		{
			if (blindbags.get(i).uniqid.equalsIgnoreCase(uniqid))
			{
				return blindbags.get(i);
			}
		}
		
		return null;
	}
	
	///////////////////
	// P R I V A T E //
	///////////////////
	
	protected final static String DB_ASSET = "database.json";
	protected final static String COLLECTION_PREF_ID = "bbcollection";
	
	protected BlindbagDB PrioritySort (BlindbagDB database)
	{
		Boolean f = true;
		while (f)
		{
			f = false;
			for (int i = 0; i < database.blindbags.size() - 1; i++)
			{
				Blindbag buf = new Blindbag();
				
				if (database.blindbags.get(i).priority < database.blindbags.get(i + 1).priority)
				{
					buf = database.blindbags.get(i);
					database.blindbags.set(i, database.blindbags.get(i + 1));
					database.blindbags.set(i+1, buf);
					f = true;
				};

			}
		}
		
		return database;
	}
	
	protected JSONArray GetCollection (Context context) throws JSONException
	{
		SharedPreferences sp = ((Activity) context).getPreferences(MODE_PRIVATE);
		Log.d("x", "GetCollection OK");
		return new JSONArray(sp.getString(COLLECTION_PREF_ID, "[{\"uniqid\": \"\", \"count\": 0}]"));		
	}
	
	protected void ParseCollection (JSONArray collection) throws JSONException
	{

		for (int i = 0; i < blindbags.size(); i++)
		{
			Blindbag bb = new Blindbag();
			bb = blindbags.get(i);
			Boolean found = false;
			
			for (int j = 0; j < collection.length(); j++)
			{
				JSONObject jo = collection.getJSONObject(j);
				
				if (jo.getString("uniqid").equalsIgnoreCase(blindbags.get(i).uniqid))
				{
					bb.count = jo.getInt("count");
					found = true;
				}
			}
			
			if (!found)
			{
				bb.count = 0;
			}
			
			blindbags.set(i, bb);
			
			found = false;
		}
		Log.d("x", "ParseCollection OK");
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
	
	protected JSONObject GetDB(Context context) throws JSONException, IOException
	{
		AssetManager am = context.getAssets();
		InputStream is = am.open(DB_ASSET);
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

			for (int j = 0; j < DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").length(); j++)
			{
				RegexpField rf = new RegexpField();
				rf.field =  DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getString("field");
				rf.regexp =  DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getString("regexp");
				rf.priority = DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getInt("priority");
				Log.d("ParseDB", rf.field + " -- " + rf.regexp + " -- "  + Integer.toString(rf.priority));
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
			bb.priority = 1;
			
			for (int j = 0; j < DB.getJSONArray("blindbags").getJSONObject(i).getJSONArray("bbids").length(); j++)
			{
				bb.bbids.add(DB.getJSONArray("blindbags").getJSONObject(i).getJSONArray("bbids").getString(j));
			}
			
			blindbags.add(bb);
		}
	}
	
	Wave GetWaveInfo(String waveid, BlindbagDB database)
	{
		Wave w = new Wave();
		
		for (int i = 0; i < database.waves.size(); i++)
		{
			if (database.waves.get(i).waveid.equalsIgnoreCase(waveid))
			{
				w = database.waves.get(i);
			}
		}
		
		return w;
	}
}
