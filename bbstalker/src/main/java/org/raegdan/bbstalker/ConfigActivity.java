package org.raegdan.bbstalker;

import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

public class ConfigActivity extends ActivityEx implements OnCheckedChangeListener, OnClickListener {

	// Controls
	CheckBox cbCAllowGeoloc;
	CheckBox cbCAllowGeolocByShop;
	CheckBox cbCSaveShopName;
	CheckBox cbCPortraitOnly;
	CheckBox cbCSmartSearch;
	Button btnCImportExport;
	
	// PWIE controls
	RelativeLayout rlPWIE;
	Button btnPWIEImport;
	Button btnPWIEExport;
	Button btnPWIECopy;
	Button btnPWIEPaste;		
	EditText etPWIECode;
	
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
		btnCImportExport = (Button) findViewById(R.id.btnCImportExport);
		
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
		btnCImportExport.setOnClickListener(this);
	}
	
	protected void ShowPWIE() {
		PopupWindow pw;
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vPWIE = inflater.inflate(R.layout.import_export, null);

		rlPWIE = (RelativeLayout) vPWIE.findViewById(R.id.rlPWIE);
		btnPWIEImport = (Button) vPWIE.findViewById(R.id.btnPWIEImport);
		btnPWIEExport = (Button) vPWIE.findViewById(R.id.btnPWIEExport);
		btnPWIECopy = (Button) vPWIE.findViewById(R.id.btnPWIECopy);
		btnPWIEPaste = (Button) vPWIE.findViewById(R.id.btnPWIEPaste);		
		etPWIECode = (EditText) vPWIE.findViewById(R.id.etPWIECode);
		
		btnPWIEImport.setOnClickListener(this);
		btnPWIEExport.setOnClickListener(this);
		btnPWIECopy.setOnClickListener(this);
		btnPWIEPaste.setOnClickListener(this);
		
		pw = new PopupWindow(this);
		
		pw.setContentView(rlPWIE);
		pw.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
		pw.setFocusable(true);
		pw.showAtLocation(vPWIE, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
	}
	
	// Click handlers
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		
		switch (buttonView.getId()) {
			case R.id.cbCAllowGeoloc: {
				ed.putBoolean("allow_geoloc", isChecked).commit();
				cbCAllowGeolocByShop.setEnabled(isChecked);
				break;
			}
			
			case R.id.cbCSaveShopName:	{
				ed.putBoolean("save_shop_name", isChecked).commit();
				if (!isChecked) {
					ed.putString("shopname", "").commit();
				}
				break;
			}
			
			case R.id.cbCAllowGeolocByShop: {
				ed.putBoolean("allow_geoloc_by_shop", isChecked).commit();
				break;
			}			
			
			case R.id.cbCPortraitOnly: {
				ed.putBoolean("portrait_only", isChecked).commit();
				break;
			}	
			
			case R.id.cbCSmartSearch: {
				ed.putBoolean("smart_search", isChecked).commit();
				break;
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnCImportExport: {
				ShowPWIE();
				break;
			}
			
			case R.id.btnPWIEImport: {
				try {
					if (((BBStalkerApplication) getApplication()).GetDB(this).RestoreDB(etPWIECode.getText().toString(), this)) {
						Toast.makeText(getApplicationContext(), getString(R.string.import_success), Toast.LENGTH_LONG).show();					
					} else {
						Toast.makeText(getApplicationContext(), getString(R.string.import_fault), Toast.LENGTH_LONG).show();
					}
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				
				break;
			}
			
			case R.id.btnPWIEExport: {
				try {
					etPWIECode.setText(((BBStalkerApplication) getApplication()).GetDB(this).DumpDB(this));
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}

				Toast.makeText(getApplicationContext(), getString(R.string.export_success), Toast.LENGTH_LONG).show();
				break;
			}
			
			case R.id.btnPWIECopy: {
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				{
				    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				    clipboard.setText(etPWIECode.getText());
				} else {
				    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
				    android.content.ClipData clip = android.content.ClipData.newPlainText("", etPWIECode.getText());
				    clipboard.setPrimaryClip(clip);
				}
				
				Toast.makeText(getApplicationContext(), getString(R.string.copied), Toast.LENGTH_LONG).show();

				break;
			}
			
			case R.id.btnPWIEPaste: {
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
				{
				    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				    etPWIECode.setText(clipboard.getText());
				} else {
				    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
				    if (clipboard.getPrimaryClip() != null) {
				    	etPWIECode.setText(clipboard.getPrimaryClip().getItemAt(0).coerceToText(this));
				    }
				}

				Toast.makeText(getApplicationContext(), getString(R.string.pasted), Toast.LENGTH_LONG).show();

				break;
			}
		}
	}
}
