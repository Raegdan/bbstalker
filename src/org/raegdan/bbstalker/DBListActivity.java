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
import android.app.DownloadManager.Query;
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
	
	  ListView lvDBList;
	  TextView tvDBHeader;
	  BlindbagDB database = new BlindbagDB();
	  //BlindbagDB QueryResult;
	  //Blindbag CurrentBlindbag;
	  Integer EntryIndex = 0;
	  
	  View vPWBBInfo;
	  RelativeLayout rlPWBBInfo;
	  TextView tvPWBBInfoName;
	  TextView tvPWBBInfoMisc;
	  ImageView ivPWBBInfoPonyPic;
	  ImageButton ibPWBBWiki;
	  ImageButton ibPWBBShareVK;
	  ImageButton ibPWBBShareGPlus;
	  ImageButton ibPWBBShareTwi;
	  EditText etPWBBShareShopname;
	  
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
		  
		  Log.d("x", "Init controls OK");
		  
		  if (!database.LoadDB(this))
		  {
			  tvDBHeader.setText("Error loading JSON database: I/O error or invalid JSON. If you didn't modify, rename or delete the original «database.json» file, please contact me (Raegdan)");
			  return;
		  }

		  Log.d("x", "Load DB OK");

		  String query = getIntent().getStringExtra("query");
		  
		  if (query.equalsIgnoreCase(""))
		  {
			  tvDBHeader.setText("All database");
		  } else {
			  tvDBHeader.setText("Results for «" + query + "»");
		  }
		  
		  database = database.LookupDB(query);
		  DBList dblist = PrepareDBList(database);

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
			  if (database.blindbags.get(i).priority == 0)
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
			  hmDBList.put("img1", wavepic);
			  hmDBList.put("uniqid", database.blindbags.get(i).uniqid);
			  dblist.data.add(hmDBList);
		  }
		  
		  return dblist;
	  }
	  
	  protected void CartUncart(String uniqid, Integer value)
	  {
		  String errmsg = "JSON error occured while saving data.";
		  
		  for (int i = 0; i < database.blindbags.size(); i++)
		  {
			  if (database.blindbags.get(i).uniqid.equalsIgnoreCase(uniqid))
			  {
				  database.blindbags.get(i).count += value;
				  break;
			  }
		  }
		  
		  if (!database.CommitDB())
		  {
			  Toast.makeText(getApplicationContext(), errmsg, Toast.LENGTH_LONG).show();
		  }
		  
		  
	  }
	  
	  protected void lvDBListItemClicked(Integer index)
	  {
		  EntryIndex = index;
		  ShowBBInfo();
	  }
	  
	  protected void ShowBBInfo ()
	  {
		  Blindbag CurrentBlindbag = database.blindbags.get(EntryIndex);

		  tvPWBBInfoName.setText(CurrentBlindbag.name);
		  tvPWBBInfoMisc.setText("Pieces in collection: " + CurrentBlindbag.count);
		  ivPWBBInfoPonyPic.setImageResource(this.getResources().getIdentifier("bb" + CurrentBlindbag.uniqid, "drawable", this.getPackageName()));
		  ibPWBBWiki.setOnClickListener(this);
		  ibPWBBShareVK.setOnClickListener(this);
		  ibPWBBShareGPlus.setOnClickListener(this);
		  ibPWBBShareTwi.setOnClickListener(this);
		  
		  PopupWindow pw = new PopupWindow(this);
		  pw.setContentView(rlPWBBInfo);
	      pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
	      pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
	      pw.setFocusable(true);
		  pw.showAtLocation(vPWBBInfo, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
	  }
	  
	  protected void SocialShare(int SocialNetwork)
	  {
		  Blindbag CurrentBlindbag = database.blindbags.get(EntryIndex);
		  
		  SocialShare ss = new SocialShare(this);
		  
		  String message = "Hey everypony! Just found W" + CurrentBlindbag.waveid + " blind bags & " + CurrentBlindbag.name + " among them @ «" + etPWBBShareShopname.getText().toString() + "» shop, geo: http://TODO/ using #bbstalker";
		  
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
		Blindbag CurrentBlindbag = database.blindbags.get(EntryIndex);
		
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
}
	}
}
