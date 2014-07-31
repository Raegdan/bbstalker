package org.raegdan.bbstalker;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import yuku.ambilwarna.*;

public class DetectorActivity extends ActivityEx implements OnClickListener {

	CheckBox cbDETUnicorn, cbDETPegasus, cbDETEarthen, cbDETAlicorn, cbDETMane, cbDETBody, cbDETNonpony;
	Button btnDETManeColor, btnDETBodyColor, btnDETQuery;
	
	// Initial colors
	final static int MANE_DEFAULT = 0xffff00ff;	// magenta
	final static int BODY_DEFAULT = 0xffffff00;	// yellow
	final static int DEALPHA = 0x00ffffff;			// color & DEALPHA == color with stripped alpha value
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detector);
		
		// Controls init
		btnDETManeColor = (Button) findViewById(R.id.btnDETManeColor);
		btnDETBodyColor = (Button) findViewById(R.id.btnDETBodyColor);
		btnDETQuery = (Button) findViewById(R.id.btnDETQuery);
		
		btnDETManeColor.setBackgroundColor(MANE_DEFAULT);
		btnDETManeColor.setTag(MANE_DEFAULT);
		btnDETBodyColor.setBackgroundColor(BODY_DEFAULT);
		btnDETBodyColor.setTag(BODY_DEFAULT);
				
		cbDETUnicorn = (CheckBox) findViewById(R.id.cbDETUnicorn);
		cbDETPegasus = (CheckBox) findViewById(R.id.cbDETPegasus);
		cbDETEarthen = (CheckBox) findViewById(R.id.cbDETEarthen);
		cbDETAlicorn = (CheckBox) findViewById(R.id.cbDETAlicorn);
		cbDETMane = (CheckBox) findViewById(R.id.cbDETMane);
		cbDETBody = (CheckBox) findViewById(R.id.cbDETBody);
		cbDETNonpony = (CheckBox) findViewById(R.id.cbDETNonpony);
	
		btnDETManeColor.setOnClickListener(this);
		btnDETBodyColor.setOnClickListener(this);	
		btnDETQuery.setOnClickListener(this);
	}

	protected void SelectColor(final View v) 	{
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, (Integer) v.getTag(), new  AmbilWarnaDialog.OnAmbilWarnaListener() {
	        @Override
	        public void onOk(AmbilWarnaDialog dialog, int color) {
	            v.setBackgroundColor(color);
	            v.setTag(color);
	        }
	                
	        @Override
	        public void onCancel(AmbilWarnaDialog dialog) {
	        	// selection cancelled, do nothing
	        }
	});

	dialog.show();
	}
	
	protected void Query() {
		JSONObject js = new JSONObject();
		
		try {
			js.put("alicorn", cbDETAlicorn.isChecked());
			js.put("unicorn", cbDETUnicorn.isChecked());
			js.put("pegasus", cbDETPegasus.isChecked());
			js.put("earthen", cbDETEarthen.isChecked());
			js.put("nonpony", cbDETNonpony.isChecked());
			js.put("mane", cbDETMane.isChecked());
			js.put("body", cbDETBody.isChecked());
			js.put("manecolor", ((Integer) btnDETManeColor.getTag()) & DEALPHA);
			js.put("bodycolor", ((Integer) btnDETBodyColor.getTag()) & DEALPHA);
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		
		Intent intent = new Intent(this, DBListActivity.class);
		intent.putExtra("query", js.toString());
		intent.putExtra("mode", DBListActivity.MODE_DETECTOR);
		startActivity(intent);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnDETManeColor:		
			case R.id.btnDETBodyColor: {
				SelectColor(v);
				break;
			}
			
			case R.id.btnDETQuery: {
				Query();
				break;
			}
			
		}
	}
}
