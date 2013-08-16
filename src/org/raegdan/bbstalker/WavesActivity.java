package org.raegdan.bbstalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class WavesActivity extends Activity implements OnItemClickListener {
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

	TextView tvWavesHeader;
	ListView lvWavesList;
	DBList dl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.waves);
		
		tvWavesHeader = (TextView) findViewById(R.id.tvWavesHeader);
		lvWavesList = (ListView) findViewById(R.id.lvWavesList);
		
		BlindbagDB database = new BlindbagDB();
		if (!database.LoadDB(this))
		{
			tvWavesHeader.setText(getString(R.string.json_db_err));
			return;
		}
		
		DBList dblist = PrepareDBList(database);
		SimpleAdapter saDBList = new SimpleAdapter(this, dblist.data, R.layout.lvwaves, dblist.fields, dblist.views);
		lvWavesList.setOnItemClickListener(this);
		lvWavesList.setAdapter(saDBList);
		
	}

	protected DBList PrepareDBList (BlindbagDB database)
	{
		dl = new DBList();
		dl.fields = new String[] {"name", "misc", "img1"};
		dl.views = new int[] {R.id.tvLVWavesName, R.id.tvLVWavesMisc, R.id.ivVLWavesWavePic};
		
		for (int i = 0; i < database.waves.size(); i++)
		{			
			Integer wavepic = this.getResources().getIdentifier("w" + database.waves.get(i).waveid, "drawable", this.getPackageName());
			
			HashMap<String, Object> hmDBList = new HashMap<String, Object>();
			
			hmDBList.put("name", getString(R.string.wave) + database.waves.get(i).waveid + " (" + database.waves.get(i).year + ")");
			hmDBList.put("misc", getString(R.string.format) + database.waves.get(i).format);
			hmDBList.put("waveid", database.waves.get(i).waveid);			
			hmDBList.put("img1", wavepic);
			dl.data.add(hmDBList);
		}
		
		return dl;
	}

	protected void QueryWave(String waveid)
	{
    	Intent intent = new Intent(this, DBListActivity.class);
    	intent.putExtra("query", waveid);
    	intent.putExtra("mode", DBListActivity.MODE_WAVE);    	
    	startActivity(intent);		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		QueryWave((String) dl.data.get(arg2).get("waveid"));
	}
}
