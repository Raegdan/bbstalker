package org.raegdan.bbstalker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;

public class MainActivity extends ActivityEx implements OnClickListener, OnEditorActionListener {

	// Controls
	Button btnMAQuery;
	Button btnMAWatchDB;
	Button btnMAWatchCollection;
	Button btnMAHelp;
	Button btnMAConfig;
	Button btnMAWatchWaves;
	Button btnMAWishlist;
	Button btnMADetector;
	
	EditText etMAQuery;
	ProgressDialog mDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		if (((BBStalkerApplication) getApplication()).DBLoaded) {
			ContinueInit();
		} else {
			// Dialog init
			mDialog = new ProgressDialog(this);
			
			// Start async database loading
			new DBLoader().execute(this);
		}
	}
	
	//////////////////////////////////////////////
	// AsyncTask for loading global DB from JSON
	//////////////////////////////////////////////
	protected class DBLoader extends AsyncTask<Activity, Integer, Void> {
		@Override
		protected void onPreExecute () {
			mDialog.setCancelable(false);
			mDialog.setMessage(getString(R.string.loading));
			mDialog.show();
		}
		
		@Override
		protected Void doInBackground(Activity... arg0) {
			((BBStalkerApplication) getApplication()).DBLoaded = ((BBStalkerApplication) arg0[0].getApplication()).LoadDB(arg0[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute (Void arg0) {
			mDialog.dismiss();
			ContinueInit();
		}
	}
	
	////////////////////////////////////////////////////
	// Continues activity init after DBLoader finishes
	////////////////////////////////////////////////////
	protected void ContinueInit() {
		// Don't init controls in case of DB loading failure.
		// Show toast and leave the form dead.
		if (!((BBStalkerApplication) getApplication()).DBLoaded) {
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
		btnMAWishlist = (Button) findViewById(R.id.btnMAWishlist);	
		btnMADetector = (Button) findViewById(R.id.btnMADetector);
		
		etMAQuery = (EditText) findViewById(R.id.etMAQuery);

		btnMAQuery.setOnClickListener(this);
		btnMAWatchDB.setOnClickListener(this);
		btnMAWatchCollection.setOnClickListener(this);
		btnMAHelp.setOnClickListener(this);
		btnMAConfig.setOnClickListener(this);
		btnMAWatchWaves.setOnClickListener(this);
		btnMAWishlist.setOnClickListener(this);
		btnMADetector.setOnClickListener(this);
		
		etMAQuery.setOnClickListener(this);
		etMAQuery.setOnEditorActionListener(this);
		
		ShowWhatsNew();
	}
	
	//////////////////////////
	// Buttons click handler
	//////////////////////////
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnMAQuery: {
				OpenDBListActivity(etMAQuery.getText().toString(), DBListActivity.MODE_LOOKUP);
				HideKB();
				break;
			}
				
			case R.id.btnMAWatchDB: {
				OpenDBListActivity("", DBListActivity.MODE_ALL_DB);
				break;
			}
	
			case R.id.btnMAWatchCollection: {
				OpenDBListActivity("", DBListActivity.MODE_COLLECTION);
				break;
			}
				
			case R.id.btnMAWishlist: {
				OpenDBListActivity("", DBListActivity.MODE_WISHLIST);
				break;
			}
				
			case R.id.btnMAHelp: {
				ShowHelp();
				break;
			}
				
			case R.id.btnMAConfig: {
				OpenConfigActivity();
				break;
			}
				
			case R.id.btnMAWatchWaves: {
				OpenWavesListActivity();
				break;
			}
			
			case R.id.btnMADetector: {
				OpenDetectorActivity();
				break;
			}
		}
	}
	
	
	protected void HideKB() {
		InputMethodManager im = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE); 
		im.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);		
	}
	
	///////////////////////////////
	// Activities calling methods
	///////////////////////////////
	protected void OpenDBListActivity(String query, int mode) {
		if (mode == DBListActivity.MODE_LOOKUP && etMAQuery.getText().toString().equalsIgnoreCase("")) {
			Toast.makeText(getApplicationContext(), getString(R.string.nothing_to_query), Toast.LENGTH_LONG).show();
			return;
		}
		Intent intent = new Intent(this, DBListActivity.class);
		intent.putExtra("query", query);
		intent.putExtra("mode", mode);
		startActivity(intent);
	}
	
	protected void OpenConfigActivity() {
		Intent intent = new Intent(this, ConfigActivity.class);
		startActivity(intent);		
	}
	
	protected void OpenWavesListActivity() {
		Intent intent = new Intent(this, WavesActivity.class);
		startActivity(intent);			
	}
	
	protected void OpenDetectorActivity() {
		Intent intent = new Intent(this, DetectorActivity.class);
		startActivity(intent);			
	}
	
	//////////////////////////////
	// Show help in popup window
	//////////////////////////////
	protected void ShowHelp() {
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
	
	////////////////////////////////////
	// Show what's new in popup window
	////////////////////////////////////
	protected void ShowWhatsNew() {
		Integer VersionCode = 0;
		try {
			VersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		final String VersionCodeString = VersionCode.toString();
		
		final SharedPreferences sp = getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
		if (sp.getBoolean("whatsnew_closed_" + VersionCodeString, false)) {
			return;
		}
		
		final PopupWindow pw;
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vPWHelp = inflater.inflate(R.layout.pwwhatsnew, null);
		RelativeLayout rlPWWhatsNew = (RelativeLayout) vPWHelp.findViewById(R.id.rlPWWhatsNew);

		pw = new PopupWindow(this);
		
		pw.setContentView(rlPWWhatsNew);
		pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setFocusable(true);

		((Button) rlPWWhatsNew.findViewById(R.id.btnPWWhatsNewClose)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pw.dismiss();
			}
		});
		
		pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
			
			@Override
			public void onDismiss() {
				Editor ed = sp.edit();
				ed.putBoolean("whatsnew_closed_" + VersionCodeString, true);
				ed.commit();
			}
		});
	
		pw.showAtLocation(vPWHelp, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
	}

	@Override
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		switch (arg0.getId()) {
			case R.id.etMAQuery: {
				if (arg1 == EditorInfo.IME_ACTION_SEARCH) { 
					OpenDBListActivity(arg0.getText().toString(), DBListActivity.MODE_LOOKUP);
					HideKB();
				}
				   
				return true;
			}
		}
		return false;
	}	
}
