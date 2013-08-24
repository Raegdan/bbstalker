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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;

/////////////////////////////////
// BlindbagDB structure classes
/////////////////////////////////
class RegexpField implements Cloneable
{
	String regexp;
	String field;
	Integer priority;
	
    public RegexpField clone() throws CloneNotSupportedException {
    	RegexpField clone = (RegexpField) super.clone();
        return clone;
    }
}

class Wave implements Cloneable
{
	List<RegexpField> priorities;
	String waveid;
	String year;
	String format;
	
	Wave()
	{
		priorities = new ArrayList<RegexpField>();
	}
	
    @SuppressWarnings("unchecked")
	public Wave clone() throws CloneNotSupportedException {
        Wave clone = (Wave) super.clone();
        clone.priorities = (List<RegexpField>) ((ArrayList<RegexpField>) priorities).clone();
        return clone;
    }
}

class Blindbag implements Cloneable
{
	public List<String> bbids;
	public String waveid;
	public String barcode;
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
	
    public Blindbag clone() throws CloneNotSupportedException {
        Blindbag clone = (Blindbag) super.clone();
        return clone;
    }
}

//////////////////////////////////////////////////
// BlindbagDB class - handling the main database
//////////////////////////////////////////////////
class BlindbagDB implements Cloneable
{
	
	/////////////////
	// P U B L I C //
	/////////////////

	List<Wave> waves;
	List<Blindbag> blindbags;

	public BlindbagDB() {
		super();
		waves = new ArrayList<Wave>();
		blindbags = new ArrayList<Blindbag>();
	}
	
	///////////////////////////////////////////////////////////////////
	// Loads blind bags database and collection from JSON into class.
	///////////////////////////////////////////////////////////////////
	public Boolean LoadDB(Context context)
	{
		try {
			ParseDB(GetDB(context));
			ParseCollection(_GetCollection(context));
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	///////////////////////////////////////
	// Performs lookup through database
	// using Smart or Fast search methods.
	///////////////////////////////////////
	public BlindbagDB LookupDB(String query, Boolean SmartSearch)
	{
		if (SmartSearch)
		{
			PerformSmartSearch(query);
		} else {
			PerformFastSearch(query);
		}
		
		return this;
	}
	
	//////////////////////////////////////////////////////////
	// Commits changes in collection into SharedPreferences.
	//////////////////////////////////////////////////////////
	public Boolean CommitDB (Context context)
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
		
		SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		Editor ed = sp.edit();
		ed.putString(COLLECTION_PREF_ID, ja.toString());
		ed.commit();
	
		return true;
	}
	
	//////////////////////////////////////////
	// Returns the blind bad data by its ID.
	//////////////////////////////////////////
	public Blindbag GetBlindbagByUniqID(String uniqid)
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
	
	/////////////////////////////////////////////////
	// Returns all blind bags of a wave by wave id.
	/////////////////////////////////////////////////
	public BlindbagDB GetWaveBBs(String waveid)
	{
		BlindbagDB OutDB = this;
		
		for (int i = 0; i < OutDB.blindbags.size(); i++)
		{
			if (!OutDB.blindbags.get(i).waveid.equalsIgnoreCase(waveid))
			{
				OutDB.blindbags.get(i).priority = 0;
			}
		}
		
		return OutDB;
	}
	
	///////////////////////////////////
	// Gets user collection database.
	///////////////////////////////////
	public BlindbagDB GetCollection(Context context)
	{
		try {
			ParseCollection(_GetCollection(context));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		BlindbagDB OutDB = this;
		
		for (int i = 0; i < OutDB.blindbags.size(); i++)
		{
			if (OutDB.blindbags.get(i).count < 1)
			{
				OutDB.blindbags.get(i).priority = 0;
			}
		}
		
		return OutDB;		
	}
	
	///////////////////////
	// Clones the object.
	///////////////////////
    public BlindbagDB clone() throws CloneNotSupportedException {
        BlindbagDB clone = (BlindbagDB) super.clone();
        clone.waves = new ArrayList<Wave>();
        clone.blindbags = new ArrayList<Blindbag>();
        
        for (int i = 0; i < waves.size(); i++)
        {
        	clone.waves.add(waves.get(i).clone());
        }
        
        for (int i = 0; i < blindbags.size(); i++)
        {
        	clone.blindbags.add(blindbags.get(i).clone());
        }
        
        return clone;
    }
	
	///////////////////
	// P R I V A T E //
	///////////////////
	
	protected final static String DB_ASSET = "database.json";
	protected final static String COLLECTION_PREF_ID = "bbcollection";
	
	///////////////////////////////////////////////////////////////////////////////////
	// Smart Search: recognizes the query type by regexps from database
	// and sorts the results by priority.
	// About 10 times slower than Fast Search, but is smarter than Anatoly Wasserman.
	///////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected void PerformSmartSearch(String query)
	{
		String QueryRegexp = QueryToRegexp(query);
		BlindbagDB source = this;
		
		for (int i = 0; i < blindbags.size(); i++)
		{
			Blindbag bb = blindbags.get(i);
			
			Wave w = new Wave();
			w = GetWaveByWaveID(bb.waveid, source);
			
			Integer Priority = 0;
			for (int j = 0; j < w.priorities.size(); j++)
			{				
				if (MatchRegexp(w.priorities.get(j).regexp, query.toUpperCase(Locale.ENGLISH)) && (w.priorities.get(j).priority > Priority))
				{
					String PriorityField = w.priorities.get(j).field;
					Object field = bb.GetFieldByName(PriorityField);
					
					// Exception in GFBN
					if (field == null)
					{
						continue;
					}
					
					// Field is string (e.g. name)
					else if (field instanceof String)
					{
						if (MatchRegexp(QueryRegexp.toUpperCase(Locale.ENGLISH), ((String) field).toUpperCase(Locale.ENGLISH)))
						{
							Priority = w.priorities.get(j).priority;
						}							
					}
					
					// Field is list (e.g. bbids)
					else if (field instanceof List)
					{
						for (int k = 0; k < bb.bbids.size(); k++)
						{
							if (MatchRegexp(QueryRegexp.toUpperCase(Locale.ENGLISH), ((List<String>) field).get(k)))
							{
								Priority = w.priorities.get(j).priority;
							}
						}							
					}
				}
			}
			
			bb.priority = Priority;
			
			blindbags.set(i, bb);
		}
		
		PrioritySort();		
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	// Fast search: no query recognition, no sorting.
	// Just answers the question if the blind bag contains query in any of its fields.
	// About 10 times faster than Smart Search, but is as stupid as a soldier boot.
	////////////////////////////////////////////////////////////////////////////////////
	protected void PerformFastSearch(String query)
	{
		String QueryRegexp = QueryToRegexp(query);
		
		for (int i = 0; i < blindbags.size(); i++)
		{
			Blindbag bb = blindbags.get(i);
			
			bb.priority = 0;
			if (MatchRegexp(QueryRegexp.toUpperCase(Locale.ENGLISH), bb.name.toUpperCase(Locale.ENGLISH))	||
				bb.waveid.equals(query) ||
				bb.barcode.equals(query))
			{
				bb.priority = 1;
				continue;
			}
			
			for (int j = 0; j < bb.bbids.size(); j++)
			{
				if (MatchRegexp(QueryRegexp.toUpperCase(Locale.ENGLISH), bb.bbids.get(j).toUpperCase(Locale.ENGLISH)))
				{
					bb.priority = 1;
					break;
				}
			}
			
			blindbags.set(i, bb);
		}	
	}
	
	///////////////////////////////
	// Sorts database by priority
	///////////////////////////////
	protected void PrioritySort ()
	{
		Boolean f = true;
		
		while (f)
		{
			f = false;
			for (int i = 0; i < blindbags.size() - 1; i++)
			{
				Blindbag buf = new Blindbag();
				
				if (blindbags.get(i).priority < blindbags.get(i + 1).priority)
				{
					buf = blindbags.get(i);
					blindbags.set(i, blindbags.get(i + 1));
					blindbags.set(i+1, buf);
					f = true;
				};

			}
		}
	}
	
	/////////////////////////////////////
	// Returns the wave data by its ID.
	/////////////////////////////////////	
	protected Wave GetWaveByWaveID(String waveid, BlindbagDB database)
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
	
	/////////////////////
	// Storage fetching
	/////////////////////
	protected JSONObject GetDB(Context context) throws JSONException, IOException
	{
		AssetManager am = context.getAssets();
		InputStream is = am.open(DB_ASSET);
		JSONObject db = new JSONObject(StreamToString(is));
		is.close();
		return db;
	}
	
	protected JSONArray _GetCollection (Context context) throws JSONException
	{
		SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return new JSONArray(sp.getString(COLLECTION_PREF_ID, "[{\"uniqid\": \"\", \"count\": 0}]"));		
	}
	
	////////////////////
	// Storage parsing
	////////////////////
	protected void ParseDB(JSONObject DB) throws JSONException
	{
		for (int i = 0; i < DB.getJSONArray("waves").length(); i++)
		{
			Wave w = new Wave();
			w.waveid = DB.getJSONArray("waves").getJSONObject(i).getString("waveid");
			w.year = DB.getJSONArray("waves").getJSONObject(i).getString("year");
			w.format = DB.getJSONArray("waves").getJSONObject(i).getString("format");
			

			for (int j = 0; j < DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").length(); j++)
			{
				RegexpField rf = new RegexpField();
				rf.field =  DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getString("field");
				rf.regexp =  DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getString("regexp");
				rf.priority = DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getInt("priority");
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
			bb.barcode = DB.getJSONArray("blindbags").getJSONObject(i).getString("barcode");
			bb.wikiurl = DB.getJSONArray("blindbags").getJSONObject(i).getString("wikiurl");
			bb.priority = 1;
			
			for (int j = 0; j < DB.getJSONArray("blindbags").getJSONObject(i).getJSONArray("bbids").length(); j++)
			{
				bb.bbids.add(DB.getJSONArray("blindbags").getJSONObject(i).getJSONArray("bbids").getString(j));
			}
			
			blindbags.add(bb);
		}
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
	}
	
	//////////////////
	// Misc routines
	//////////////////
	protected String QueryToRegexp(String query)
	{
		query = query.replaceAll("([^A-Za-z0-9\\.\\*])", "\\\\$1");
		query = query.replaceAll("\\*", ".{1,}?");
		query = ".*?" + query + ".*?";
		
		return query;
	}
	
	protected String StreamToString(InputStream is) throws IOException {
		String s = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		for(String line = br.readLine(); line != null; line = br.readLine()) 
		{
			s += line;
		}
		br.close();
		return s;
	}
	
	protected Boolean MatchRegexp(String regexp, String s)
	{
		return Pattern.compile(regexp).matcher(s).matches();
	}
}
