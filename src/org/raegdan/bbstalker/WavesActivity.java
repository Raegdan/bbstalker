package org.raegdan.bbstalker;

import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class WavesActivity extends Activity implements OnItemClickListener {

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
				
				hmDBList.put("name", getString(R.string.wave) + database.waves.get(i).waveid + " (" + database.waves.get(i).year + ")");
				hmDBList.put("misc", getString(R.string.format) + database.waves.get(i).format);
				hmDBList.put("waveid", database.waves.get(i).waveid);			
				hmDBList.put("img1", wavepic);
				dl.data.add(hmDBList);
			}
			
			return dl;
		}
		
		@Override
		protected HashMap<String, Object> doInBackground(Context... arg0) {
			context = (Context) arg0[0];
			
			BlindbagDB database = new BlindbagDB();
			
			HashMap<String, Object> out = new HashMap<String, Object>();

			if (!database.LoadDB(context))
			{
				TitleMsg = context.getString(R.string.json_db_err);
				out.put("error", true);
				out.put("title_msg", TitleMsg);
				return out;
			}

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
}
