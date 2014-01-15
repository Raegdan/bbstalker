package org.raegdan.bbstalker;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import yuku.ambilwarna.*;

public class DetectorActivity extends ActivityEx implements OnClickListener {

	CheckBox cbDETUnicorn, cbDETPegasus, cbDETEarthen, cbDETAlicorn, cbDETMane, cbDETBody;
	Button btnDETManeColor, btnDETBodyColor, btnDETQuery;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detector);
		
		// Controls init
		btnDETManeColor = (Button) findViewById(R.id.btnDETManeColor);
		btnDETBodyColor = (Button) findViewById(R.id.btnDETBodyColor);
		btnDETQuery = (Button) findViewById(R.id.btnDETQuery);
		
		btnDETManeColor.setBackgroundColor(0xffff00ff);
		btnDETManeColor.setTag(Integer.valueOf(0xffff00ff));
		btnDETBodyColor.setBackgroundColor(0xffffff00);
		btnDETBodyColor.setTag(Integer.valueOf(0xffffff00));
				
		cbDETUnicorn = (CheckBox) findViewById(R.id.cbDETUnicorn);
		cbDETPegasus = (CheckBox) findViewById(R.id.cbDETPegasus);
		cbDETEarthen = (CheckBox) findViewById(R.id.cbDETEarthen);
		cbDETAlicorn = (CheckBox) findViewById(R.id.cbDETAlicorn);
		cbDETMane = (CheckBox) findViewById(R.id.cbDETMane);
		cbDETBody = (CheckBox) findViewById(R.id.cbDETBody);
	
		btnDETManeColor.setOnClickListener(this);
		btnDETBodyColor.setOnClickListener(this);	
		btnDETQuery.setOnClickListener(this);
	}

	/*@SuppressLint("NewApi")
	protected int SafeGetBGColor(View v) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return ((ColorDrawable) v.getBackground()).getColor();
		} else {
			return 0xff000000;
		}
	}*/
	
	protected void SelectColor(final View v) 	{
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, ((Integer) v.getTag()).intValue(), new  AmbilWarnaDialog.OnAmbilWarnaListener() {
	        @Override
	        public void onOk(AmbilWarnaDialog dialog, int color) {
	            v.setBackgroundColor(color);
	            v.setTag(Integer.valueOf(color));
	        }
	                
	        @Override
	        public void onCancel(AmbilWarnaDialog dialog) {
	        	// selection cancelled
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
			js.put("mane", cbDETMane.isChecked());
			js.put("body", cbDETBody.isChecked());
			js.put("mane_clr", ((Integer) btnDETManeColor.getTag()).intValue());
			js.put("body_clr", ((Integer) btnDETBodyColor.getTag()).intValue());
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
