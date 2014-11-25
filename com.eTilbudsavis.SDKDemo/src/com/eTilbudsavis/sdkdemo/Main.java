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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaLocation;
import com.eTilbudsavis.etasdk.Log.DevLogger;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class Main extends Activity {
	
	public static final String TAG = Main.class.getSimpleName();

    private static final int MENU_LOCATION = 1;
    
    private Button mBtnCatalogs;
	private Button mBtnSearch;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* 
         * 
         */
        EtaLog.setLogger(new DevLogger());
        
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
         * Eta is a singleton you interact with via this method
         */
        Eta eta = Eta.getInstance();
        
        /*
         * Set the location (This could also be set via LocationManager)
         */
        EtaLocation loc = eta.getLocation();
        loc.setLatitude(Constants.ETA_HQ.lat);
        loc.setLongitude(Constants.ETA_HQ.lng);
        
        // Avoid using large distances in production, it's bad for performance (longer queries)
        // the 700km reaius here is just for demonstration purposes - we recommend 100km or less
        loc.setRadius(700000);
        loc.setSensor(false);
        
        /*
         * You are now done setting up the SDK, the rest is just Android stuff
         */
        mBtnCatalogs= (Button)findViewById(R.id.btnCatalogs);
        mBtnCatalogs.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Main.this, CatalogViewer.class);
				startActivity(i);
			}
		});
        
        mBtnSearch = (Button)findViewById(R.id.btnSearch);
        mBtnSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Main.this, Search.class);
				startActivity(i);
			}
		});
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, MENU_LOCATION, Menu.NONE, "Choose location");
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case MENU_LOCATION:
			Toast.makeText(Main.this, "not implemented yet", Toast.LENGTH_SHORT).show();
			break;

		default:
			break;
		}
    	return super.onOptionsItemSelected(item);
    }
}
