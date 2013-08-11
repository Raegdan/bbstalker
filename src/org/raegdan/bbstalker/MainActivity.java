package org.raegdan.bbstalker;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.view.Menu;

public class MainActivity extends Activity implements OnClickListener {

    Button btnMAQuery;
    Button btnMAWatchDB;
    Button btnMAWatchCollection;
    EditText etMAQuery;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        btnMAQuery = (Button) findViewById(R.id.btnMAQuery);
        btnMAQuery.setOnClickListener(this);
        btnMAWatchDB = (Button) findViewById(R.id.btnMAWatchDB);
        btnMAWatchDB.setOnClickListener(this);
        btnMAWatchCollection = (Button) findViewById(R.id.btnMAWatchCollection);
        btnMAWatchCollection.setOnClickListener(this);
        etMAQuery = (EditText) findViewById(R.id.etMAQuery);
        etMAQuery.setOnClickListener(this);
	}
	
	  protected void ShowToast(String text)
	  {
		  Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT); 
		  toast.show(); 
	  }

	// Queries blind bag DB and shows DB activity
	protected void QueryDatabase(String query) {
    	Intent intent = new Intent(this, DBListActivity.class);
    	intent.putExtra("query", query);
    	startActivity(intent);		
	}
	
	@Override
	public void onClick(View v) {
	    switch (v.getId()) {
	    case R.id.btnMAQuery:
	    	if (etMAQuery.getText().toString().equalsIgnoreCase(""))
	    	{
	    		ShowToast("Nothing to query :)");
	    	} else {
	    		QueryDatabase(etMAQuery.getText().toString());
	    	}
	    	break;
	    	
	    case R.id.btnMAWatchDB:
	    	QueryDatabase("");
	    	break;

	    case R.id.btnMAWatchCollection:
	    	QueryDatabase("$");
	    	break;
	    	
	    default:
	    	break;
	  }
	}
	
	///////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
