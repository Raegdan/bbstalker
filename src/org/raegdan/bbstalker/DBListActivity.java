package org.raegdan.bbstalker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.raegdan.bbstalker.MyLocation.LocationResult;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class DBListActivity extends Activity implements OnItemClickListener, OnClickListener {
	
	BlindbagDB database;
	DBList dblist;
	String CurrentBBUniqID = "";
	Integer CurrentDBListID = 0;
	Integer HideMode = 0;

	ListView lvDBList;
	SimpleAdapter saDBList;
	TextView tvDBHeader;

	PopupWindow pw;
	View vPWBBInfo;
	RelativeLayout rlPWBBInfo;
	TextView tvPWBBInfoName;
	TextView tvPWBBInfoMisc;
	ImageView ivPWBBInfoPonyPic;
	ImageButton ibPWBBWiki;
	ImageButton ibPWBBShareVK;
	ImageButton ibPWBBShareGPlus;
	ImageButton ibPWBBShareTwi;
	ImageButton ibPWBBCart;
	ImageButton ibPWBBUncart;
	EditText etPWBBShareShopname;
	
	ProgressDialog mDialog;
	MyLocation myLocation;
	String BitlyLink;
	
	final static int HM_BY_COUNT = 1;
	final static int HM_BY_PRIORITY = 2;
	
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
		
		tvDBHeader = (TextView) findViewById(R.id.tvDBHeader);		  
		
		database = new BlindbagDB();
		
		myLocation = new MyLocation();
		
		if (!database.LoadDB(this))
		{
			tvDBHeader.setText(getString(R.string.json_db_err));
			return;
		}

		String query = getIntent().getStringExtra("query");
		
		lvDBList = (ListView) findViewById(R.id.lvDBList);
		lvDBList.setOnItemClickListener(this);
		
		if (query.equalsIgnoreCase("$"))
		{
			tvDBHeader.setText(getString(R.string.my_collection));
			HideMode = HM_BY_COUNT;
		} else
		{
			if (query.equalsIgnoreCase(""))
			{
				tvDBHeader.setText(getString(R.string.all_db));
			} else {
				tvDBHeader.setText(getString(R.string.results_for) + query + "»");
			}
			
			database = database.LookupDB(query);
			HideMode = HM_BY_PRIORITY;
		}		
		
		dblist = PrepareDBList(database, HideMode);
		saDBList = new SimpleAdapter(this, dblist.data, R.layout.lvdblist, dblist.fields, dblist.views);
		lvDBList.setAdapter(saDBList);
	}
	
	protected DBList PrepareDBList (BlindbagDB database, Integer HideMode)
	{
		DBList dl = new DBList();
		dl.fields = new String[] {"name", "misc", "count", "img1"};
		dl.views = new int[] {R.id.tvLVDBListName, R.id.tvLVDBListMisc, R.id.tvLVDBListCollectionCount, R.id.ivVLDBListWavePic};
		
		for (int i = 0; i < database.blindbags.size(); i++)
		{
			if (
					(	(HideMode == HM_BY_PRIORITY) && (database.blindbags.get(i).priority == 0)	) ||
					(	(HideMode == HM_BY_COUNT) && (database.blindbags.get(i).count == 0)	)		)
			{
				continue;
			}
			
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
			hmDBList.put("misc", getString(R.string.code) + bbids);
			hmDBList.put("count", " (" + database.blindbags.get(i).count.toString() + getString(R.string.in_collection));
			hmDBList.put("img1", wavepic);
			hmDBList.put("uniqid", database.blindbags.get(i).uniqid);
			hmDBList.put("count_int", database.blindbags.get(i).count);
			dl.data.add(hmDBList);
		}
		
		return dl;
	}
	
	protected void CartUncart(String uniqid, Integer value)
	{
		String errmsg = getString(R.string.json_saving_err);
		Blindbag bb = new Blindbag();
		
		int i;
		
		for (i = 0; i < database.blindbags.size(); i++)
		{
			bb = database.blindbags.get(i);
			
			if (bb.uniqid.equalsIgnoreCase(uniqid))
			{
				if (bb.count + value < 0)
				{
					return;
				}
				
				bb.count += value;
				break;
			}
		}
		
		database.blindbags.set(i, bb);
		
		if (!database.CommitDB(this))
		{
			Toast.makeText(getApplicationContext(), errmsg, Toast.LENGTH_LONG).show();
			bb.count -= value;
			database.blindbags.set(i, bb);
		}
		
		dblist.data.get(CurrentDBListID).put("count_int", bb.count);
		dblist.data.get(CurrentDBListID).put("count", " (" + bb.count.toString() + getString(R.string.in_collection));
		
		tvPWBBInfoMisc.setText(getString(R.string.pcs_in_collection) + database.blindbags.get(i).count.toString());
		
		if (HideMode == HM_BY_COUNT)
		{
			if (bb.count == 0)
			{
				dblist.data.remove(CurrentDBListID.intValue());
				Log.d("pw", "remove");
				pw.dismiss();
			}
		}
		
		saDBList.notifyDataSetChanged();
	}
	
	protected void lvDBListItemClicked(Integer index)
	{
		CurrentBBUniqID = (String) dblist.data.get(index).get("uniqid");
		CurrentDBListID = index;
		ShowBBInfo();
	}
	
	protected void ShowBBInfo ()
	{
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		vPWBBInfo = inflater.inflate(R.layout.pwbbinfo, null);
		rlPWBBInfo = (RelativeLayout) vPWBBInfo.findViewById(R.id.rlPWBBInfo);
		tvPWBBInfoName = (TextView) rlPWBBInfo.findViewById(R.id.tvPWBBInfoName);
		tvPWBBInfoMisc = (TextView) rlPWBBInfo.findViewById(R.id.tvPWBBInfoMisc);
		ivPWBBInfoPonyPic = (ImageView) rlPWBBInfo.findViewById(R.id.ivPWBBInfoPonyPic);
		ibPWBBWiki = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBWiki);
		ibPWBBShareVK = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBShareVK);
		ibPWBBShareGPlus = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBShareGPlus);
		ibPWBBShareTwi = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBShareTwi);	
		etPWBBShareShopname = (EditText) rlPWBBInfo.findViewById(R.id.etPWBBSocialShareShopname);
		ibPWBBCart = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBCart);
		ibPWBBUncart = (ImageButton) rlPWBBInfo.findViewById(R.id.ibPWBBUncart);
		
		tvPWBBInfoName.setText(database.GetBlindbagByUniqID(CurrentBBUniqID).name);
		tvPWBBInfoMisc.setText(getString(R.string.pcs_in_collection) + database.GetBlindbagByUniqID(CurrentBBUniqID).count);
		ivPWBBInfoPonyPic.setImageResource(this.getResources().getIdentifier("bb" + CurrentBBUniqID, "drawable", this.getPackageName()));
		ibPWBBWiki.setOnClickListener(this);
		ibPWBBShareVK.setOnClickListener(this);
		ibPWBBShareGPlus.setOnClickListener(this);
		ibPWBBShareTwi.setOnClickListener(this);
		ibPWBBCart.setOnClickListener(this);
		ibPWBBUncart.setOnClickListener(this);
		
		final SharedPreferences sp = this.getPreferences(MODE_PRIVATE);
		etPWBBShareShopname.setText(sp.getString("shopname", getString(R.string.shop_field)));
		etPWBBShareShopname.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				sp.edit().putString("shopname", etPWBBShareShopname.getText().toString()).commit();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		
		pw = new PopupWindow(this);
		
		pw.setContentView(rlPWBBInfo);
		pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setFocusable(true);
		pw.showAtLocation(vPWBBInfo, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
	}

	protected class BitlyRequest extends AsyncTask<String, Integer, String>
	{

		@Override
		protected String doInBackground(String... params) {
			String API_KEY = "a6d306ec04569d71baa6459738622fde5d37262f";
			String BITLY_API_URL = "https://api-ssl.bitly.com/v3/shorten?access_token=" + API_KEY + "&longUrl=";
			
			try {
				params[0] = URLEncoder.encode(params[0], "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
			
		    HttpClient hc = new DefaultHttpClient(); 
		    
		    Log.d("Request", BITLY_API_URL + params[0]);
		    
		    HttpGet hg = new HttpGet(BITLY_API_URL + params[0]);
		    HttpResponse hr;
		    
			try {
				hr = hc.execute(hg);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} 
			
		    HttpEntity he = hr.getEntity();  
		    if (he == null)
		    {  
		    	return null;
		    }
		    
		    String response  = "";
		    
			try {
				response = EntityUtils.toString(he);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		    
			Log.d("JSON", response);
		    
		    try {
				return new JSONObject(response).getJSONObject("data").getString("url");
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}		
	}
	protected void SocialShare(final int SocialNetwork)
	{		
		LocationResult locationResult = new LocationResult(){
			@Override
			public void gotLocation(Location location, int ErrCode, boolean cached) {
				switch (ErrCode)
				{
					case MyLocation.EC_NO_PROVIDERS:
					{
						Log.d ("Geodata", "No providers");
						break;
					}
					
					case MyLocation.EC_NO_DATA:
					{
						Log.d ("Geodata", "No data");
						break;
					}
					
					case MyLocation.EC_NO_ERR:
					{
						Log.d ("Geodata", "OK! Lat:" + Double.toString(location.getLatitude()) + " Lon:" + Double.toString(location.getLongitude()));
						
						if (!cached)
						{
							try {
								BitlyLink = new BitlyRequest().execute("http://maps.google.com/maps?q=" + Double.toString(location.getLatitude()) + "," + Double.toString(location.getLongitude())).get().replaceAll("http://", "");
							} catch (InterruptedException e) {
								e.printStackTrace();
								return;
							} catch (ExecutionException e) {
								e.printStackTrace();
								return;
							}
						
							if (BitlyLink == null)
							{
								return;
							}							
						}
						
						Log.d ("Geodata", BitlyLink);

						SocialShareStage2(SocialNetwork, BitlyLink);
						
						break;
					}
				}
			}
		};
		
		mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(false);
        mDialog.show();
        
		myLocation.getLocation(this, locationResult);
	}
	
	protected void SocialShareStage2(int SocialNetwork, String GeoLink)
	{
		mDialog.dismiss();
		
		SocialShare ss = new SocialShare(this);
		
		String message = getString(R.string.social_msg_p1) + database.GetBlindbagByUniqID(CurrentBBUniqID).waveid + getString(R.string.social_msg_p2) + database.GetBlindbagByUniqID(CurrentBBUniqID).name + getString(R.string.social_msg_p3) + etPWBBShareShopname.getText().toString() + getString(R.string.social_msg_p4)  + GeoLink + getString(R.string.social_msg_p5);
		
		String text = getString(R.string.no_social_app_p1);
		switch (SocialNetwork)
		{
			case SocialShare.SN_VK:
				text += "VK.com ";
				break;
				
			case SocialShare.SN_TWITTER:
				text += "Twitter ";
				break;
				
			case SocialShare.SN_GPLUS:
				text += "Google+ ";
				break;
		}
		
		text += getString(R.string.no_social_app_p2);

		if (!ss.Share(message, SocialNetwork))
		{
			Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
		}
	
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

	@Override
	public void onClick(View v) {
		Blindbag CurrentBlindbag = database.GetBlindbagByUniqID(CurrentBBUniqID);
		
		switch (v.getId())
		{
			case R.id.ibPWBBWiki:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CurrentBlindbag.wikiurl)));
				break;
				
			case R.id.ibPWBBShareVK:
				SocialShare(SocialShare.SN_VK);
				break;

			case R.id.ibPWBBShareGPlus:
				SocialShare(SocialShare.SN_GPLUS);
				break;

			case R.id.ibPWBBShareTwi:
				SocialShare(SocialShare.SN_TWITTER);
				break;
				
			case R.id.ibPWBBCart:
				CartUncart(CurrentBBUniqID, +1);
				break;

			case R.id.ibPWBBUncart:
				CartUncart(CurrentBBUniqID, -1);
				break;

		}
	}
}
