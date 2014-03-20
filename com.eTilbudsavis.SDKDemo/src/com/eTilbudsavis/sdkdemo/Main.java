package com.eTilbudsavis.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaLocation;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
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
         *  Even better do this in Application (global state), that way the
         *  singleton will be available in all activities even if your app
         *  gets garbage collected by the system.
         */
        Eta eta = Eta.getInstance();
        
        /* 
         * You MUST set the Eta object, in order for it to work
         * ApiKey and ApiSecret are not included in the demo/SDK, but you can
         * get your own at https://etilbudsavis.dk/developers/ :-)
         */
        eta.set(Keys.API_KEY, Keys.API_SECRET, this);
        
        /* 
         * Enable debug mode, so debug info will show in LogCat
         * You might not want to have this set to true in a release version.
         */
        EtaLog.enableLogd(true);
        
        /*
         * Set the location (This could also be set via LocationManager)
         */
        EtaLocation loc = Eta.getInstance().getLocation();
        loc.setLatitude(55.63105);
        loc.setLongitude(12.5766);
        loc.setRadius(700000);
        loc.setSensor(false);
        
        /*
         * You are now done setting up the SDK, the rest is just Android stuff
         */
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
