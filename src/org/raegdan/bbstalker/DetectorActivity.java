package org.raegdan.bbstalker;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import yuku.ambilwarna.*;
import yuku.ambilwarna.widget.*;

public class DetectorActivity extends ActivityEx implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detector);
		
		// Controls init
		Button btnDETManeColor = (Button) findViewById(R.id.btnDETManeColor);
		Button btnDETBodyColor = (Button) findViewById(R.id.btnDETBodyColor);
		
		btnDETManeColor.setOnClickListener(this);
		btnDETBodyColor.setOnClickListener(this);		
	}

	protected void SelectColor(final View v)
	{
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this,((ColorDrawable) v.getBackground()).getColor(), new  AmbilWarnaDialog.OnAmbilWarnaListener() {
	        @Override
	        public void onOk(AmbilWarnaDialog dialog, int color) {
	            v.setBackgroundColor(color);
	        }
	                
	        @Override
	        public void onCancel(AmbilWarnaDialog dialog) {
	        	// selection cancelled
	        }
	});

	dialog.show();
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btnDETManeColor:		
			case R.id.btnDETBodyColor: {
				SelectColor(v);
				break;
			}
		}
	}
}
