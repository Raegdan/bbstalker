package org.raegdan.bbstalker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

public class MainActivity extends ActivityEx implements OnClickListener {

	// Controls
    Button btnMAQuery;
    Button btnMAWatchDB;
    Button btnMAWatchCollection;
    Button btnMAHelp;
    Button btnMAConfig;
    Button btnMAWatchWaves;
    EditText etMAQuery;
    ProgressDialog mDialog;
    
    // Success DB load flag
    Boolean DBLoadedSuccessfully = true;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Dialog init
		mDialog = new ProgressDialog(this);
        
		// Start async database loading
        new DBLoader().execute(this);
	}
	
	//////////////////////////////////////////////
	// AsyncTask for loading global DB from JSON
	//////////////////////////////////////////////
	protected class DBLoader extends AsyncTask<Activity, Integer, Void>
	{
		@Override
		protected void onPreExecute ()
		{
	        mDialog.setCancelable(false);
	        mDialog.setMessage(getString(R.string.loading));
	        mDialog.show();
		}
		
		@Override
		protected Void doInBackground(Activity... arg0) {
			DBLoadedSuccessfully = ((BBStalkerApplication) arg0[0].getApplication()).LoadDB(arg0[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute (Void arg0)
		{
			mDialog.dismiss();
			ContinueInit();
		}
	}
	
	////////////////////////////////////////////////////
	// Continues activity init after DBLoader finishes
	////////////////////////////////////////////////////
	protected void ContinueInit()
	{
		// Don't init controls in case of DB loading failure.
		// Show toast and leave the form dead.
		if (!DBLoadedSuccessfully)
		{
			Toast.makeText(getApplicationContext(), getString(R.string.json_db_err), Toast.LENGTH_LONG).show();
			return;
		}
		
        // Controls init
        btnMAQuery = (Button) findViewById(R.id.btnMAQuery);
        btnMAWatchDB = (Button) findViewById(R.id.btnMAWatchDB);
        btnMAWatchCollection = (Button) findViewById(R.id.btnMAWatchCollection);
        btnMAHelp = (Button) findViewById(R.id.btnMAHelp);
        btnMAConfig = (Button) findViewById(R.id.btnMAConfig);
        btnMAWatchWaves = (Button) findViewById(R.id.btnMAWatchWaves);
        etMAQuery = (EditText) findViewById(R.id.etMAQuery);

        btnMAQuery.setOnClickListener(this);
        btnMAWatchDB.setOnClickListener(this);
        btnMAWatchCollection.setOnClickListener(this);
        btnMAHelp.setOnClickListener(this);
        btnMAConfig.setOnClickListener(this);
        btnMAWatchWaves.setOnClickListener(this);
        etMAQuery.setOnClickListener(this);
	}
	
	//////////////////////////
	// Buttons click handler
	//////////////////////////
	@Override
	public void onClick(View v) {
	    switch (v.getId())
	    {
	    case R.id.btnMAQuery:
    		OpenDBListActivity(etMAQuery.getText().toString(), DBListActivity.MODE_LOOKUP);
    		InputMethodManager im = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE); 
    		im.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
	    }
	}
	
	///////////////////////////////
	// Activities calling methods
	///////////////////////////////
	protected void OpenDBListActivity(String query, int mode)
	{
		if (mode == DBListActivity.MODE_LOOKUP && etMAQuery.getText().toString().equalsIgnoreCase(""))
		{
			Toast.makeText(getApplicationContext(), getString(R.string.nothing_to_query), Toast.LENGTH_LONG).show();
			return;
		}
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
	
	//////////////////////////////
	// Show help in popup window
	//////////////////////////////
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
