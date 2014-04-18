/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
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
         * You MUST create an Eta instance before invoking Eta.getinstance.
         * 
         * You can do this in Application (global state), that way the
         * singleton will be available in all activities even if your app
         * gets garbage collected by the system.
         * 
         * ApiKey and ApiSecret are not included in the demo/SDK, but you can
         * get your own at https://etilbudsavis.dk/developers/ :-)
         */
        Eta.createInstance(Keys.API_KEY, Keys.API_SECRET, this);
        
        /* 
         * Enable debug mode, so debug info will show in LogCat
         * You might not want to have this set to true in a release version.
         */
        EtaLog.enableLogd(true);

        /*
         * Eta is a singleton you interact with via this method
         */
        Eta eta = Eta.getInstance();
        
        /*
         * Set the location (This could also be set via LocationManager)
         */
        EtaLocation loc = eta.getLocation();
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
