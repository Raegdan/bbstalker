package org.raegdan.bbstalker;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ConfigActivity extends Activity implements OnCheckedChangeListener{

	CheckBox cbCAllowGeoloc;
	SharedPreferences sp;
	Editor ed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		
		sp = getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
		ed = sp.edit();
		
		cbCAllowGeoloc = (CheckBox) findViewById(R.id.cbCAllowGeoloc);
		cbCAllowGeoloc.setChecked(sp.getBoolean("allow_geoloc", true));
		cbCAllowGeoloc.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId())
		{
			case R.id.cbCAllowGeoloc:
			{
				ed.putBoolean("allow_geoloc", isChecked).commit();
			}
		}
	}
}
