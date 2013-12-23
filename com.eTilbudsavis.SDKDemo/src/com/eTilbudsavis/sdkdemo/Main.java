package com.eTilbudsavis.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaLocation;
import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "Main";

	Button btnCatalogs;
	Button btnSearch;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*
         *  Eta is a singleton, so we'll set it, first chance we get.
         */
        Eta eta = Eta.getInstance();
        
        // We MUST set the Eta, in order for it to work
        eta.set(Keys.API_KEY, Keys.API_SECRET, this);
        
        /* Enable logging mode, so debug info will show in LogCat
         * You might not want to have this set to true in a release version. */
        Eta.DEBUG_LOGD = true;
        
        
        
        // Set the location (a valid location is required in order to get data from the API)
        EtaLocation loc = Eta.getInstance().getLocation();
        // latitude, longitude that matched Fields, copenhagen
        loc.setLatitude(55.63105);
        loc.setLongitude(12.5766);
        // Radius of 7 kilometers
        loc.setRadius(7000);
        // And no sensor
        loc.setSensor(false);
        
        
        btnCatalogs= (Button)findViewById(R.id.btnCatalogs);
        btnCatalogs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Main.this, CatalogViewer.class);
				startActivity(i);
			}
		});

        btnSearch = (Button)findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Main.this, Search.class);
				startActivity(i);
			}
		});

    }
    
}
