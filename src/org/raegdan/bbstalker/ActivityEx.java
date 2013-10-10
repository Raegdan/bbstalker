package org.raegdan.bbstalker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

public class ActivityEx extends Activity {
	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Orientation fix for all activities together
		SharedPreferences sp = getSharedPreferences(getPackageName(), MODE_PRIVATE);
		if (sp.getBoolean("portrait_only", true)) {
			setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		//Apply theme if supported
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			setTheme(android.R.style.Theme_Holo_NoActionBar);
		}
	}
}
