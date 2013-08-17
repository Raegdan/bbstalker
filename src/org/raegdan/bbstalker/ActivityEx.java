package org.raegdan.bbstalker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class ActivityEx extends Activity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	SharedPreferences sp = getSharedPreferences(getPackageName(), MODE_PRIVATE);
    	if (sp.getBoolean("portrait_only", true))
    	{
    		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    	}
    }
}
