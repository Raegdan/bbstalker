package org.raegdan.bbstalker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

//////////////////////////////////////////////////
// BlindbagDB class - handling the main database
//////////////////////////////////////////////////
public class BlindbagDB implements Cloneable {

	/////////////////
	// P U B L I C //
	/////////////////
	public List<Wave> waves;
	public List<Blindbag> blindbags;

	public BlindbagDB() {
		super();
		waves = new ArrayList<Wave>();
		blindbags = new ArrayList<Blindbag>();
	}
	
	///////////////////////////////////////////////////////////////////
	// Loads blind bags database and collection from JSON into class.
	///////////////////////////////////////////////////////////////////
	public Boolean LoadDB(Context context) 	{
		try {
			ParseDB(GetDB(context));
			ParseCollection(_GetCollection(context));
			ParseWishlist(_GetWishlist(context));
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
	public BlindbagDB LookupDB(String query, Boolean SmartSearch) {
		if (SmartSearch) {
			PerformSmartSearch(query);
		} else {
			PerformFastSearch(query);
		}
		
		return this;
	}

	/////////////////////////////////////////////////
	// Performs reverse lookup by race and colors.
	/////////////////////////////////////////////////
	public BlindbagDB ReverseLookup(String query) {
		JSONObject js = new JSONObject();
		
		try {
			js = new JSONObject(query);

			for (int i = 0; i < blindbags.size(); i++) 	{
				Blindbag bb = blindbags.get(i);
				
				if ( !(js.getBoolean("alicorn") && bb.race == Blindbag.RACE_ALICORN) &&
						!(js.getBoolean("unicorn") && bb.race == Blindbag.RACE_UNICORN) &&
						!(js.getBoolean("pegasus") && bb.race == Blindbag.RACE_PEGASUS) &&
						!(js.getBoolean("earthen") && bb.race == Blindbag.RACE_EARTHEN) &&
						!(js.getBoolean("nonpony") && bb.race == Blindbag.RACE_NONPONY)) {
					bb.priority = 0;
				} else { 
					if (js.getBoolean("mane") || js.getBoolean("body")) {					
						int priority = 0;
						
						if (js.getBoolean("mane") && js.getBoolean("body")) {
							final double DEVQ = 0.01;
							Double md = ColorDiff(bb.manecolor, js.getInt("manecolor"));
							Double bd = ColorDiff(bb.bodycolor, js.getInt("bodycolor"));
							Double avg = (md + bd) / 2.0;
							//Double dev = (Math.sqrt(0.5 * ((md - avg) * (md - avg)) + ((bd - avg) * (bd - avg))) * DEVQ);
							Double dev = Math.abs(md - bd) * DEVQ;
							priority = 1000 - (int) (avg + avg * dev);
							
							
//							Log.d("color_diff", "mc="+ md.toString() + ", bc="+ bd.toString() + ", dd=" + dd.toString() + ", prio=" + Integer.valueOf(priority).toString());
						} else {
							if (js.getBoolean("mane")) {
								priority = 1000 - ColorDiff(bb.manecolor, js.getInt("manecolor")).intValue();
							}
							
							if (js.getBoolean("body")) {
								priority = 1000 - ColorDiff(bb.bodycolor, js.getInt("bodycolor")).intValue();
							}
						}
						
						bb.priority = priority;
					} else {
						bb.priority = 1;
					}
				}
				
				blindbags.set(i, bb);
			}
			
			PrioritySort();
			
			return this;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	///////////////////////////////////////////////////////////////////////
	// Commits changes in collection and wishlist into SharedPreferences.
	///////////////////////////////////////////////////////////////////////
	public Boolean CommitDB (Context context) {
		JSONArray coll = new JSONArray();
		JSONArray wl   = new JSONArray();	
		
		for (int i = 0; i < blindbags.size(); i++) 	{
			Integer count  = blindbags.get(i).count;
			Boolean wanted = blindbags.get(i).wanted;
			
			try {
				if (count > 0) {	
					coll.put(new JSONObject().put("uniqid", blindbags.get(i).uniqid).put("count", count));
				}
				
				if (wanted) {
					wl.put(blindbags.get(i).uniqid);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		Editor ed = sp.edit();
		ed.putString(COLLECTION_PREF_ID, coll.toString());
		ed.putString(WISHLIST_PREF_ID, wl.toString());
		ed.commit();

		return true;
	}
	
	//////////////////////////////////////////
	// Returns the blind bad data by its ID.
	//////////////////////////////////////////
	public Blindbag GetBlindbagByUniqID(String uniqid) 	{
		for (int i = 0; i < blindbags.size(); i++) 	{
			if (blindbags.get(i).uniqid.equalsIgnoreCase(uniqid)) {
				return blindbags.get(i);
			}
		}
		
		return null;
	}
	
	/////////////////////////////////////
	// Returns the wave data by its ID.
	/////////////////////////////////////	
	public Wave GetWaveByWaveID(String waveid) {
		Wave w = new Wave();
		
		for (int i = 0; i < waves.size(); i++) 	{
			if (waves.get(i).waveid.equalsIgnoreCase(waveid)) {
				w = waves.get(i);
			}
		}
		
		return w;
	}
	
	/////////////////////////////////////////////////
	// Returns all blind bags of a wave by wave id.
	/////////////////////////////////////////////////
	public BlindbagDB GetWaveBBs(String waveid) {
		BlindbagDB OutDB = this;
		
		for (int i = 0; i < OutDB.blindbags.size(); i++) {
			if (!OutDB.blindbags.get(i).waveid.equalsIgnoreCase(waveid)) {
				OutDB.blindbags.get(i).priority = 0;
			}
		}
		
		return OutDB;
	}
	
	///////////////////////////////////
	// Gets user collection database.
	///////////////////////////////////
	public BlindbagDB GetCollection(Context context) {
		try {
			ParseCollection(_GetCollection(context));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		BlindbagDB OutDB = this;
		
		for (int i = 0; i < OutDB.blindbags.size(); i++) {
			if (OutDB.blindbags.get(i).count < 1) {
				OutDB.blindbags.get(i).priority = 0;
			}
		}
		
		return OutDB;		
	}
	
	/////////////////////////////////
	// Gets user wishlist database.
	/////////////////////////////////
	public BlindbagDB GetWishlist(Context context) 	{
		try {
			ParseWishlist(_GetWishlist(context));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		BlindbagDB OutDB = this;
		
		for (int i = 0; i < OutDB.blindbags.size(); i++) {
			if (!OutDB.blindbags.get(i).wanted) {
				OutDB.blindbags.get(i).priority = 0;
			}
		}
		
		return OutDB;		
	}
	
	///////////////////////
	// Clones the object.
	///////////////////////
	public BlindbagDB clone(Context context) throws CloneNotSupportedException {
		BlindbagDB clone = (BlindbagDB) super.clone();
		clone.waves = new ArrayList<Wave>();
		clone.blindbags = new ArrayList<Blindbag>();
		
		for (int i = 0; i < waves.size(); i++) {
			clone.waves.add(waves.get(i).clone());
		}
		
		for (int i = 0; i < blindbags.size(); i++) {
			clone.blindbags.add(blindbags.get(i).clone());
		}
		
		try {
			clone.ParseCollection(_GetCollection(context));
			clone.ParseWishlist(_GetWishlist(context));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return clone;
	}
	
	/////////////////////////////
	// Dumps DB into JSON code.
	/////////////////////////////
	public String DumpDB(Context context)
	{
		JSONObject dump = new JSONObject();
		
		try {
			dump.put("collection", _GetCollection(context));
			dump.put("wishlist", _GetWishlist(context));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return dump.toString();
	}
	
	/////////////////////////////////////////////////////////
	// Restores DB from JSON dump.
	// Returns true on success;
	// false on error (database remains untouched on error)
	/////////////////////////////////////////////////////////
	public Boolean RestoreDB(String dump, Context context) {
		JSONObject jsondump;
		
		try {
			jsondump = new JSONObject(dump);
		} catch (JSONException e) {
			return false;
		}
		
		try {
			ParseCollection(jsondump.getJSONArray("collection"));
		} catch (JSONException e) {
			try {
				ParseCollection(_GetCollection(context));
				return false;
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			ParseWishlist(jsondump.getJSONArray("wishlist"));
		} catch (JSONException e) {
			try {
				ParseCollection(_GetCollection(context));
				ParseWishlist(_GetWishlist(context));
				return false;
			} catch (JSONException e1) {
				e1.printStackTrace();
			}		
		}
		
		CommitDB(context);
		return true;
	}
	
	///////////////////
	// P R I V A T E //
	///////////////////
	
	protected final static String DB_ASSET = "database.json";
	protected final static String COLLECTION_PREF_ID = "bbcollection";
	protected final static String WISHLIST_PREF_ID = "bbwishlist";
	
	///////////////////////////////////////////////////////////////////////////////////
	// Smart Search: recognizes the query type by regexps from database
	// and sorts the results by priority.
	// About 10 times slower than Fast Search, but is smarter than Anatoly Wasserman.
	///////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected void PerformSmartSearch(String query) {
		String QueryRegexp = QueryToRegexp(query);
		BlindbagDB source = this;
		
		for (int i = 0; i < blindbags.size(); i++) 	{
			Blindbag bb = blindbags.get(i);
			
			Wave w = new Wave();
			w = source.GetWaveByWaveID(bb.waveid);
			
			Integer Priority = 0;
			for (int j = 0; j < w.priorities.size(); j++) {				
				if (MatchRegexp(w.priorities.get(j).regexp, query.toUpperCase(Locale.ENGLISH)) && (w.priorities.get(j).priority > Priority)) {
					String PriorityField = w.priorities.get(j).field;
					Object field = bb.GetFieldByName(PriorityField);
					
					// Exception in GFBN
					if (field == null) 	{
						continue;
					}
					
					// Field is string (e.g. name)
					else if (field instanceof String) {
						if (MatchRegexp(QueryRegexp.toUpperCase(Locale.ENGLISH), ((String) field).toUpperCase(Locale.ENGLISH))) {
							Priority = w.priorities.get(j).priority;
						}							
					}
					
					// Field is list (e.g. bbids)
					else if (field instanceof List) {
						for (int k = 0; k < bb.bbids.size(); k++) {
							if (MatchRegexp(QueryRegexp.toUpperCase(Locale.ENGLISH), ((List<String>) field).get(k))) {
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
	protected void PerformFastSearch(String query) {
		String QueryRegexp = QueryToRegexp(query);
		
		for (int i = 0; i < blindbags.size(); i++) 	{
			Blindbag bb = blindbags.get(i);
			
			bb.priority = 0;
			if (MatchRegexp(QueryRegexp.toUpperCase(Locale.ENGLISH), bb.name.toUpperCase(Locale.ENGLISH)) || bb.waveid.equals(query)) {
				bb.priority = 1;
				continue;
			}
			
			for (int j = 0; j < bb.bbids.size(); j++) {
				if (MatchRegexp(QueryRegexp.toUpperCase(Locale.ENGLISH), bb.bbids.get(j).toUpperCase(Locale.ENGLISH))) {
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
	protected void PrioritySort () {
		Boolean f = true;
		
		while (f) {
			f = false;
			for (int i = 0; i < blindbags.size() - 1; i++) 	{
				Blindbag buf = new Blindbag();
				
				if (blindbags.get(i).priority < blindbags.get(i + 1).priority) {
					buf = blindbags.get(i);
					blindbags.set(i, blindbags.get(i + 1));
					blindbags.set(i+1, buf);
					f = true;
				};
			}
		}
	}
	
	/////////////////////
	// Storage fetching
	/////////////////////
	protected JSONObject GetDB(Context context) throws JSONException, IOException {
		AssetManager am = context.getAssets();
		InputStream is = am.open(DB_ASSET);
		JSONObject db = new JSONObject(StreamToString(is));
		is.close();
		return db;
	}
	
	protected JSONArray _GetCollection (Context context) throws JSONException {
		SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return new JSONArray(sp.getString(COLLECTION_PREF_ID, "[{\"uniqid\": \"\", \"count\": 0}]"));		
	}
	
	protected JSONArray _GetWishlist (Context context) throws JSONException {
		SharedPreferences sp = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		return new JSONArray(sp.getString(WISHLIST_PREF_ID, "[]"));		
	}
	
	////////////////////
	// Storage parsing
	////////////////////
	protected void ParseDB(JSONObject DB) throws JSONException {
		for (int i = 0; i < DB.getJSONArray("waves").length(); i++) {
			Wave w = new Wave();
			w.waveid = DB.getJSONArray("waves").getJSONObject(i).getString("waveid");
			w.year = DB.getJSONArray("waves").getJSONObject(i).getString("year");
			w.format = DB.getJSONArray("waves").getJSONObject(i).getString("format");
			w.name = DB.getJSONArray("waves").getJSONObject(i).getString("name"); 

			for (int j = 0; j < DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").length(); j++) {
				RegexpField rf = new RegexpField();
				rf.field =  DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getString("field");
				rf.regexp =  DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getString("regexp");
				rf.priority = DB.getJSONArray("waves").getJSONObject(i).getJSONArray("priorities").getJSONObject(j).getInt("priority");
				w.priorities.add(rf);
			}
			
			waves.add(w);
		}
		
		for (int i = 0; i < DB.getJSONArray("blindbags").length(); i++) {
			Blindbag bb = new Blindbag();
			bb.name = DB.getJSONArray("blindbags").getJSONObject(i).getString("name");
			bb.uniqid = DB.getJSONArray("blindbags").getJSONObject(i).getString("uniqid");
			bb.waveid = DB.getJSONArray("blindbags").getJSONObject(i).getString("waveid");
			bb.wikiurl = DB.getJSONArray("blindbags").getJSONObject(i).getString("wikiurl");
			
			String rawrace = DB.getJSONArray("blindbags").getJSONObject(i).getString("race");
			bb.race = Blindbag.RACE_NONPONY;
			if (rawrace.equalsIgnoreCase("unicorn")) {
				bb.race = Blindbag.RACE_UNICORN;
			} else if (rawrace.equalsIgnoreCase("alicorn")) {
				bb.race = Blindbag.RACE_ALICORN;
			} else if (rawrace.equalsIgnoreCase("pegasus")) {
				bb.race = Blindbag.RACE_PEGASUS;
			} else if (rawrace.equalsIgnoreCase("earthen")) {
				bb.race = Blindbag.RACE_EARTHEN;
			}
			
			bb.manecolor = Integer.valueOf(DB.getJSONArray("blindbags").getJSONObject(i).getString("mane"), 16);
			bb.bodycolor = Integer.valueOf(DB.getJSONArray("blindbags").getJSONObject(i).getString("body"), 16);
			
			bb.priority = 1;
			
			for (int j = 0; j < DB.getJSONArray("blindbags").getJSONObject(i).getJSONArray("bbids").length(); j++) 	{
				bb.bbids.add(DB.getJSONArray("blindbags").getJSONObject(i).getJSONArray("bbids").getString(j));
			}
			
			blindbags.add(bb);
		}
	}
	
	protected void ParseCollection (JSONArray collection) throws JSONException {
		for (int i = 0; i < blindbags.size(); i++) 	{
			Blindbag bb = new Blindbag();
			bb = blindbags.get(i);
			Boolean found = false;
			
			for (int j = 0; j < collection.length(); j++) {
				JSONObject jo = collection.getJSONObject(j);
				
				if (jo.getString("uniqid").equalsIgnoreCase(bb.uniqid)) {
					bb.count = jo.getInt("count");
					found = true;
					break;
				}
			}
			
			if (!found) {
				bb.count = 0;
			}
			
			blindbags.set(i, bb);
			
			found = false;
		}
	}
	
	protected void ParseWishlist (JSONArray wishlist) throws JSONException
	{
		for (int i = 0; i < blindbags.size(); i++) {
			Blindbag bb = new Blindbag();
			bb = blindbags.get(i);
			bb.wanted = false;
			
			for (int j = 0; j < wishlist.length(); j++) {
				String wanted_bb = wishlist.getString(j);
				
				if ((wanted_bb.equalsIgnoreCase(bb.uniqid)) && (bb.count < 1)) {
					bb.wanted = true;
					break;
				}
			}
			
			blindbags.set(i, bb);
		}
	}
	
	//////////////////
	// Misc routines
	//////////////////
	protected String QueryToRegexp(String query) {
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
	
	protected Boolean MatchRegexp(String regexp, String s) {
		return Pattern.compile(regexp).matcher(s).matches();
	}
	
	protected Double ColorDiff(int color1, int color2) {
		int R1, G1, B1, R2, G2, B2;
		int[] lab1 = {0,0,0};
		int[] lab2 = {0,0,0};
		
		R1 = (color1 & 0xff0000) >> 16;
		G1 = (color1 & 0x00ff00) >> 8;
		B1 = (color1 & 0x0000ff);
		R2 = (color2 & 0xff0000) >> 16;
		G2 = (color2 & 0x00ff00) >> 8;
		B2 = (color2 & 0x0000ff);
		
		rgb2lab(R1, G1, B1, lab1);
		rgb2lab(R2, G2, B2, lab2);
		
		return Math.sqrt((lab1[0] - lab2[0]) * (lab1[0] - lab2[0]) + (lab1[1] - lab2[1]) * (lab1[1] - lab2[1]) + (lab1[2] - lab2[2]) * (lab1[2] - lab2[2]));
	}
	
	// Function taken at http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector/HTMLHelp/farbraumJava.htm
	public void rgb2lab(int R, int G, int B, int []lab) {
		//http://www.brucelindbloom.com
		  
		float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
		float Ls, as, bs;
		float eps = 216.f/24389.f;
		float k = 24389.f/27.f;
		   
		float Xr = 0.964221f;  // reference white D50
		float Yr = 1.0f;
		float Zr = 0.825211f;
		
		// RGB to XYZ
		r = R/255.f; //R 0..1
		g = G/255.f; //G 0..1
		b = B/255.f; //B 0..1
		
		// assuming sRGB (D65)
		if (r <= 0.04045)
			r = r/12;
		else
			r = (float) Math.pow((r+0.055)/1.055,2.4);
		
		if (g <= 0.04045)
			g = g/12;
		else
			g = (float) Math.pow((g+0.055)/1.055,2.4);
		
		if (b <= 0.04045)
			b = b/12;
		else
			b = (float) Math.pow((b+0.055)/1.055,2.4);
		
		
		X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
		Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
		Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;
		
		// XYZ to Lab
		xr = X/Xr;
		yr = Y/Yr;
		zr = Z/Zr;
				
		if ( xr > eps )
			fx =  (float) Math.pow(xr, 1/3.);
		else
			fx = (float) ((k * xr + 16.) / 116.);
		 
		if ( yr > eps )
			fy =  (float) Math.pow(yr, 1/3.);
		else
		fy = (float) ((k * yr + 16.) / 116.);
		
		if ( zr > eps )
			fz =  (float) Math.pow(zr, 1/3.);
		else
			fz = (float) ((k * zr + 16.) / 116);
		
		Ls = ( 116 * fy ) - 16;
		as = 500*(fx-fy);
		bs = 200*(fy-fz);
		
		lab[0] = (int) (2.55*Ls + .5);
		lab[1] = (int) (as + .5); 
		lab[2] = (int) (bs + .5);       
	} 
}
