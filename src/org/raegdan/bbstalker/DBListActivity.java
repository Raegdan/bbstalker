package org.raegdan.bbstalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
		
		if (!database.LoadDB(this))
		{
			tvDBHeader.setText("Error loading JSON database: I/O error or invalid JSON. If you didn't modify, rename or delete the original «database.json» file, please contact me (Raegdan)");
			return;
		}

		String query = getIntent().getStringExtra("query");
		
		lvDBList = (ListView) findViewById(R.id.lvDBList);
		lvDBList.setOnItemClickListener(this);
		
		if (query.equalsIgnoreCase("$"))
		{
			tvDBHeader.setText("My collection");
			HideMode = HM_BY_COUNT;
		} else
		{
			if (query.equalsIgnoreCase(""))
			{
				tvDBHeader.setText("All database");
			} else {
				tvDBHeader.setText("Results for «" + query + "»");
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
			hmDBList.put("misc", "Code: " + bbids);
			hmDBList.put("count", " (" + database.blindbags.get(i).count.toString() + " in collection)");
			hmDBList.put("img1", wavepic);
			hmDBList.put("uniqid", database.blindbags.get(i).uniqid);
			hmDBList.put("count_int", database.blindbags.get(i).count);
			dl.data.add(hmDBList);
		}
		
		return dl;
	}
	
	protected void CartUncart(String uniqid, Integer value)
	{
		String errmsg = "JSON error occured while saving data.";
		
		Blindbag bb = new Blindbag();;
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
		
		tvPWBBInfoMisc.setText("Pieces in collection: " + database.blindbags.get(i).count);
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
		tvPWBBInfoMisc.setText("Pieces in collection: " + database.GetBlindbagByUniqID(CurrentBBUniqID).count);
		ivPWBBInfoPonyPic.setImageResource(this.getResources().getIdentifier("bb" + CurrentBBUniqID, "drawable", this.getPackageName()));
		ibPWBBWiki.setOnClickListener(this);
		ibPWBBShareVK.setOnClickListener(this);
		ibPWBBShareGPlus.setOnClickListener(this);
		ibPWBBShareTwi.setOnClickListener(this);
		ibPWBBCart.setOnClickListener(this);
		ibPWBBUncart.setOnClickListener(this);
		
		
		PopupWindow pw = new PopupWindow(this);
		
		pw.setOnDismissListener(new PopupWindow.OnDismissListener()
		{
			@Override
			public void onDismiss()
			{
				Log.d("pw", "dismiss");
				RefreshDBList();
			}
		});
		
		pw.setContentView(rlPWBBInfo);
		pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setFocusable(true);
		pw.showAtLocation(vPWBBInfo, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
	}
	
	protected void RefreshDBList()
	{
		// Deleting items if count was set to zero via PW and we're in HM_BY_COUNT mode (collection viewing)
		if (HideMode == HM_BY_COUNT)
		{
			if (((Integer) dblist.data.get(CurrentDBListID).get("count_int")) == 0)
			{
				dblist.data.remove(CurrentDBListID);
				Log.d("pw", "remove");
				saDBList.notifyDataSetChanged();
			}
		}
	}
	
	protected void SocialShare(int SocialNetwork)
	{
		SocialShare ss = new SocialShare(this);
		
		String message = "Hey everypony! Just found W" + database.GetBlindbagByUniqID(CurrentBBUniqID).waveid + " blind bags & " + database.GetBlindbagByUniqID(CurrentBBUniqID).name + " among them @ «" + etPWBBShareShopname.getText().toString() + "» shop, geo: http://TODO/ using #bbstalker";
		
		String text = "Please install official ";
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
		
		text += "client from the Market.";

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
