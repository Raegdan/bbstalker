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

    Button btnQuery;
    Button btnWatchDB;
    Button btnWatchCollection;
    
    EditText etQuery;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnQuery = (Button) findViewById(R.id.btnQuery);
        btnQuery.setOnClickListener(this);
        btnWatchDB = (Button) findViewById(R.id.btnWatchDB);
        btnWatchDB.setOnClickListener(this);
        btnWatchCollection = (Button) findViewById(R.id.btnWatchCollection);
        btnWatchCollection.setOnClickListener(this);
        etQuery = (EditText) findViewById(R.id.etQuery);
        etQuery.setOnClickListener(this);
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
	    case R.id.btnQuery:
	    	if (etQuery.getText().toString().equalsIgnoreCase(""))
	    	{
	    		ShowToast("Nothing to query :)");
	    	} else {
	    		QueryDatabase(etQuery.getText().toString());
	    	}
	    	break;
	    	
	    case R.id.btnWatchDB:
	    	QueryDatabase("");
	    	break;

	    case R.id.btnWatchCollection:
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
