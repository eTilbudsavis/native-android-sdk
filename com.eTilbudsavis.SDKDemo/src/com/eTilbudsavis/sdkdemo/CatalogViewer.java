package com.eTilbudsavis.sdkdemo;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Api;
import com.eTilbudsavis.etasdk.Api.CallbackCatalogList;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Pageflip;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.Pageflip.PageflipListener;
import com.eTilbudsavis.etasdk.Session.SessionListener;
import com.eTilbudsavis.etasdk.Tools.Utilities;
import com.eTilbudsavis.sdkdemo.helpers.Keys;
import com.etilbudsavis.sdkdemo.R;

public class CatalogViewer extends Activity {

	public static final String TAG = "CatalogViewer";
	Eta mEta;
	Pageflip pf;

	// The session listener (triggered on session events)
    SessionListener sl = new SessionListener() {
		
		@Override
		public void onUpdate() {
			if (!mEta.getSession().isExpired()) {
				
				// If the session is ready, unsubscribe this listener
				// and get a list of catalogs.
				mEta.getSession().unSubscribe(this);
				mEta.getCatalogList(cbcl).execute();
			}
		}
	};

	// A catalogs listener, 
	Api.CallbackCatalogList cbcl = new CallbackCatalogList() {
		
		@Override
		public void onComplete(int statusCode, ArrayList<Catalog> catalogs,
				EtaError error) {

			if (statusCode == 200 && !catalogs.isEmpty()) {
				// If the callback one or more catalogs, 
				// show the first catalog in a pageflip.
				pf = (Pageflip)findViewById(R.id.pageflip);
		        pf.execute(mEta, pfl, catalogs.get(0).getId());
		        
			} else {
				
				Tools.logd(TAG, error.toString());
				
			}
		}
	};
    
	// Pageflip listener, triggered on callbacks from the pageflip.
    PageflipListener pfl = new PageflipListener() {
		
		@Override
		public void onEvent(String event, String uuid, JSONObject object) {
			Toast.makeText(getApplicationContext(), event, Toast.LENGTH_SHORT).show();
			Utilities.logd(TAG, event + " - " + object.toString());
		}
		
		@Override
		public void onReady(String uuid) {
			Toast.makeText(getApplicationContext(), "Ready", Toast.LENGTH_SHORT).show();
			Utilities.logd(TAG, "Ready: " + uuid);
		}

	};
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_viewer);
        
        // Create a new instance of Eta
        mEta = new Eta(Keys.API_KEY, Keys.API_SECRET, this).debug(true);
        
        // Set the location (This could also be set via LocationManager)
        mEta.getLocation().set(55.63105, 12.5766, 700000, false);	// Fields Copenhagen
        
        // Subscribe a session listener, and start the session.
        // As soon as the session is ready, the listener is triggered.
        mEta.getSession().subscribe(sl).start();
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

    menu.add(Menu.NONE, 0, 0, "Sideoversigt");
    menu.add(Menu.NONE, 1, 1, "Luk");

    return super.onCreateOptionsMenu(menu); 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	switch (item.getItemId()) {

    	case 0:
    		pf.togglePagelist();
    		break;

    	case 1:
    		pf.togglePagelist();
    		break;
    		
    	default:
    		break;

    	}
    	return super.onOptionsItemSelected(item);
    }
    
}
