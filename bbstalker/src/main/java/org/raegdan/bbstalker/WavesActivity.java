package org.raegdan.bbstalker;

import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class WavesActivity extends ActivityEx implements OnItemClickListener, OnItemLongClickListener {
	TextView tvWavesHeader;
	ListView lvWavesList;
	DBList dblist;
	BlindbagDB database;
	ProgressDialog mDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.waves);
		
		tvWavesHeader = (TextView) findViewById(R.id.tvWavesHeader);
		lvWavesList = (ListView) findViewById(R.id.lvWavesList);
		
		try {
			database = ((BBStalkerApplication) this.getApplication()).GetDB(this);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		mDialog = new ProgressDialog(this);
		mDialog.setMessage(getString(R.string.loading));
		mDialog.setCancelable(false);
		mDialog.show();
		
		new QueryDatabase().execute(this);
	}

	protected void DBQueryFinished(BlindbagDB db, DBList dl, String TitleMsg)
	{
		mDialog.dismiss();
		
		if (db == null || dl == null)
		{
			tvWavesHeader.setText(TitleMsg);
			return;
		}
		
		dblist = dl;
		database = db;
		
		SimpleAdapter saDBList = new SimpleAdapter(this, dblist.data, R.layout.lvwaves, dblist.fields, dblist.views);
		lvWavesList.setOnItemClickListener(this);
		lvWavesList.setOnItemLongClickListener(this);
		lvWavesList.setAdapter(saDBList);
	}
	
	protected class QueryDatabase extends AsyncTask<Context, Integer, HashMap<String, Object>>
	{
		Context context;
		String TitleMsg;
		
		protected DBList PrepareDBList (BlindbagDB database, Context context)
		{
			DBList dl = new DBList();
			dl.fields = new String[] {"name", "misc", "img1"};
			dl.views = new int[] {R.id.tvLVWavesName, R.id.tvLVWavesMisc, R.id.ivVLWavesWavePic};
			
			for (int i = 0; i < database.waves.size(); i++)
			{			
				Integer wavepic = context.getResources().getIdentifier("w" + database.waves.get(i).waveid, "drawable", context.getPackageName());
				
				HashMap<String, Object> hmDBList = new HashMap<String, Object>();
				
				if (Integer.parseInt(database.waves.get(i).waveid) <= 100)
				{
					hmDBList.put("name", getString(R.string.wave) + database.waves.get(i).waveid + " (" + database.waves.get(i).year + ")");
					hmDBList.put("misc", getString(R.string.format) + database.waves.get(i).format);
				} else {
					hmDBList.put("name", database.waves.get(i).name);
					hmDBList.put("misc", getString(R.string.collection_set));					
				}
				hmDBList.put("waveid", database.waves.get(i).waveid);			
				hmDBList.put("img1", wavepic);
				dl.data.add(hmDBList);
			}
			
			return dl;
		}
		
		@Override
		protected HashMap<String, Object> doInBackground(Context... arg0) {
			context = arg0[0];
			
			HashMap<String, Object> out = new HashMap<String, Object>();

			out.put("error", false);
			out.put("title_msg", TitleMsg);
			out.put("database", database);
			out.put("dblist", PrepareDBList(database, context));			
			return out;
		}
		
		@Override
		protected void onPostExecute (HashMap<String, Object> result)
		{
			if (!((Boolean) result.get("error")))
			{
				DBQueryFinished((BlindbagDB) result.get("database"), (DBList) result.get("dblist"), TitleMsg);				
			} else {
				DBQueryFinished(null, null, TitleMsg);				
			}
		}
	}
	
	protected void ShowWaveImage (int index)
	{
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View vPWWaveImage = inflater.inflate(R.layout.pwwaveimage, null);
		
		TextView tvPWWaveImageHeader = (TextView) vPWWaveImage.findViewById(R.id.tvPWWaveImageHeader);
		ImageView ivPWWaveImage = (ImageView) vPWWaveImage.findViewById(R.id.ivPWWaveImage);
		RelativeLayout rlPWWaveImage = (RelativeLayout) vPWWaveImage.findViewById(R.id.rlPWWaveImage);
		
		ivPWWaveImage.setImageResource(this.getResources().getIdentifier("wi" + dblist.data.get(index).get("waveid"), "drawable", this.getPackageName()));
		tvPWWaveImageHeader.setText((String) dblist.data.get(index).get("name"));
		
		PopupWindow pw = new PopupWindow(this);
		
		pw.setContentView(rlPWWaveImage);
		pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setFocusable(true);
		
		pw.showAtLocation(vPWWaveImage, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
	}
	
	//////////////////////////////

	protected void QueryWave(String waveid)
	{
		Intent intent = new Intent(this, DBListActivity.class);
		intent.putExtra("query", waveid);
		intent.putExtra("mode", DBListActivity.MODE_WAVE);	
		startActivity(intent);		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		QueryWave((String) dblist.data.get(arg2).get("waveid"));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		switch (arg0.getId()) {
		case R.id.lvWavesList:
			ShowWaveImage(arg2);
			return true;
		}
		
		return false;
	}
}
