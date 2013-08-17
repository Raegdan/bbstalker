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
	CheckBox cbCAllowGeolocByShop;
	CheckBox cbCSaveShopName;
	SharedPreferences sp;
	Editor ed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		
		sp = getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
		ed = sp.edit();
		
		cbCAllowGeoloc = (CheckBox) findViewById(R.id.cbCAllowGeoloc);
		cbCAllowGeolocByShop = (CheckBox) findViewById(R.id.cbCAllowGeolocByShop);
		cbCSaveShopName = (CheckBox) findViewById(R.id.cbCSaveShopName);
		cbCAllowGeoloc.setChecked(sp.getBoolean("allow_geoloc", true));
		cbCAllowGeolocByShop.setChecked(sp.getBoolean("allow_geoloc_by_shop", true));		
		cbCSaveShopName.setChecked(sp.getBoolean("save_shop_name", true));		
		cbCAllowGeoloc.setOnCheckedChangeListener(this);
		cbCAllowGeolocByShop.setOnCheckedChangeListener(this);
		cbCSaveShopName.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId())
		{
			case R.id.cbCAllowGeoloc:
			{
				ed.putBoolean("allow_geoloc", isChecked).commit();
			}
			case R.id.cbCSaveShopName:
			{
				ed.putBoolean("save_shop_name", isChecked).commit();
				if (!isChecked)
				{
					ed.putString("shopname", "");
				}
			}
			case R.id.cbCAllowGeolocByShop:
			{
				ed.putBoolean("allow_geoloc_by_shop", isChecked).commit();
			}			
		}
	}
}
