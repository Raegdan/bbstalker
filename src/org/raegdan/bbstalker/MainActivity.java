package org.raegdan.bbstalker;

import android.os.Bundle;
import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.content.Context;
import android.content.Intent;

public class MainActivity extends Activity implements OnClickListener {

    Button btnMAQuery;
    Button btnMAWatchDB;
    Button btnMAWatchCollection;
    Button btnMAHelp;
    Button btnMAConfig;
    Button btnMAWatchWaves;
    EditText etMAQuery;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnMAQuery = (Button) findViewById(R.id.btnMAQuery);
        btnMAQuery.setOnClickListener(this);
        btnMAWatchDB = (Button) findViewById(R.id.btnMAWatchDB);
        btnMAWatchDB.setOnClickListener(this);
        btnMAWatchCollection = (Button) findViewById(R.id.btnMAWatchCollection);
        btnMAWatchCollection.setOnClickListener(this);
        btnMAHelp = (Button) findViewById(R.id.btnMAHelp);
        btnMAHelp.setOnClickListener(this);
        btnMAConfig = (Button) findViewById(R.id.btnMAConfig);
        btnMAConfig.setOnClickListener(this);
        btnMAWatchWaves = (Button) findViewById(R.id.btnMAWatchWaves);
        btnMAWatchWaves.setOnClickListener(this);
        etMAQuery = (EditText) findViewById(R.id.etMAQuery);
        etMAQuery.setOnClickListener(this);
	}
	
	protected void OpenDBListActivity(String query, int mode) {
    	Intent intent = new Intent(this, DBListActivity.class);
    	intent.putExtra("query", query);
    	intent.putExtra("mode", mode);    	
    	startActivity(intent);
	}
	
	protected void OpenConfigActivity()
	{
    	Intent intent = new Intent(this, ConfigActivity.class);
    	startActivity(intent);		
	}
	
	protected void OpenWavesListActivity()
	{
    	Intent intent = new Intent(this, WavesActivity.class);
    	startActivity(intent);			
	}
	
	@Override
	public void onClick(View v) {
	    switch (v.getId()) {
	    case R.id.btnMAQuery:
	    	if (etMAQuery.getText().toString().equalsIgnoreCase(""))
	    	{
	    		Toast.makeText(getApplicationContext(), getString(R.string.nothing_to_query), Toast.LENGTH_LONG).show();
	    	} else {
	    		OpenDBListActivity(etMAQuery.getText().toString(), DBListActivity.MODE_LOOKUP);
	    	}
	    	break;
	    	
	    case R.id.btnMAWatchDB:
	    	OpenDBListActivity("", DBListActivity.MODE_ALL_DB);
	    	break;

	    case R.id.btnMAWatchCollection:
	    	OpenDBListActivity("", DBListActivity.MODE_COLLECTION);
	    	break;
	    	
	    case R.id.btnMAHelp:
	    	ShowHelp();
	    	break;
	    	
	    case R.id.btnMAConfig:
	    	OpenConfigActivity();
	    	break;
	    	
	    case R.id.btnMAWatchWaves:
	    	OpenWavesListActivity();
	    	break;
	    	
	    default:
	    	break;
	  }
	}
	
	protected void ShowHelp()
	{
		PopupWindow pw;
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vPWHelp = inflater.inflate(R.layout.pwhelp, null);
		RelativeLayout rlPWHelp = (RelativeLayout) vPWHelp.findViewById(R.id.rlPWHelp);
		
		pw = new PopupWindow(this);
		
		pw.setContentView(rlPWHelp);
		pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setFocusable(true);
		pw.showAtLocation(vPWHelp, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
	}    
}
