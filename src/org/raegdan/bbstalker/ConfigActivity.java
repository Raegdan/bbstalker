package org.raegdan.bbstalker;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ConfigActivity extends ActivityEx implements OnCheckedChangeListener{

	// Controls
	CheckBox cbCAllowGeoloc;
	CheckBox cbCAllowGeolocByShop;
	CheckBox cbCSaveShopName;
	CheckBox cbCPortraitOnly;
	CheckBox cbCSmartSearch;
	
	// Config handlers
	SharedPreferences sp;
	Editor ed;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		
		// Init config handlers
		sp = getSharedPreferences(this.getPackageName(), MODE_PRIVATE);
		ed = sp.edit();
		
		// Init controls
		cbCAllowGeoloc = (CheckBox) findViewById(R.id.cbCAllowGeoloc);
		cbCAllowGeolocByShop = (CheckBox) findViewById(R.id.cbCAllowGeolocByShop);
		cbCSaveShopName = (CheckBox) findViewById(R.id.cbCSaveShopName);
		cbCPortraitOnly = (CheckBox) findViewById(R.id.cbCPortraitOnly);
		cbCSmartSearch = (CheckBox) findViewById(R.id.cbCSmartSearch);
		
		cbCAllowGeoloc.setChecked(sp.getBoolean("allow_geoloc", true));
		cbCAllowGeolocByShop.setChecked(sp.getBoolean("allow_geoloc_by_shop", true));
		cbCAllowGeolocByShop.setEnabled(sp.getBoolean("allow_geoloc", true));
		cbCSaveShopName.setChecked(sp.getBoolean("save_shop_name", true));
		cbCPortraitOnly.setChecked(sp.getBoolean("portrait_only", true));
		cbCSmartSearch.setChecked(sp.getBoolean("smart_search", true));
		
		cbCAllowGeoloc.setOnCheckedChangeListener(this);
		cbCAllowGeolocByShop.setOnCheckedChangeListener(this);
		cbCSaveShopName.setOnCheckedChangeListener(this);
		cbCPortraitOnly.setOnCheckedChangeListener(this);
		cbCSmartSearch.setOnCheckedChangeListener(this);
	}

	// Click handlers
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		switch (buttonView.getId())
		{
			case R.id.cbCAllowGeoloc:
			{
				ed.putBoolean("allow_geoloc", isChecked).commit();
				cbCAllowGeolocByShop.setEnabled(isChecked);
				break;
			}
			
			case R.id.cbCSaveShopName:
			{
				ed.putBoolean("save_shop_name", isChecked).commit();
				if (!isChecked)
				{
					ed.putString("shopname", "").commit();
				}
				break;
			}
			
			case R.id.cbCAllowGeolocByShop:
			{
				ed.putBoolean("allow_geoloc_by_shop", isChecked).commit();
				break;
			}			
			
			case R.id.cbCPortraitOnly:
			{
				ed.putBoolean("portrait_only", isChecked).commit();
				break;
			}	
			
			case R.id.cbCSmartSearch:
				ed.putBoolean("smart_search", isChecked).commit();
				break;
		}
	}
}
