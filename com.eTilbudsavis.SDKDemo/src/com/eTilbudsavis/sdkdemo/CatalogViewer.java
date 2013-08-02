package com.eTilbudsavis.sdkdemo;

import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Api.ListListener;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Pageflip;
import com.eTilbudsavis.etasdk.Pageflip.PageflipListener;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.Utils.Utils;
import com.eTilbudsavis.sdkdemo.helpers.Keys;
import com.etilbudsavis.sdkdemo.R;

public class CatalogViewer extends Activity {

	public static final String TAG = "CatalogViewer";
	Eta mEta;
	Pageflip pf;

	// A catalogs listener, 
	ListListener<Catalog> catalogListener = new ListListener<Catalog>() {
		
		@Override
		public void onComplete(int statusCode, List<Catalog> list, EtaError error) {

			if (statusCode == 200 && !list.isEmpty()) {
				// If the callback one or more catalogs, 
				// show the first catalog in a pageflip.
				pf = (Pageflip)findViewById(R.id.pageflip);
//				pf.setWe("192.168.1.131", "8081");
		        pf.execute(mEta, pfl, list.get(0).getId());
		        
			} else {
				
				Utils.logd(TAG, error.toString());
				
			}
		}
	};
	
	// Pageflip listener, triggered on callbacks from the pageflip.
    PageflipListener pfl = new PageflipListener() {
		
		@Override
		public void onEvent(String event, String uuid, JSONObject object) {
			Toast.makeText(getApplicationContext(), event, Toast.LENGTH_SHORT).show();
			Utils.logd(TAG, event + " - " + object.toString());
		}
		
		@Override
		public void onReady(String uuid) {
			Toast.makeText(getApplicationContext(), "Ready", Toast.LENGTH_SHORT).show();
			Utils.logd(TAG, "Ready: " + uuid);
		}

	};
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_viewer);
        
        // Create a new instance of Eta
        mEta = new Eta(Keys.API_KEY, Keys.API_SECRET, this).debug(true);
        
        // Set the location (This could also be set via LocationManager)
        mEta.getLocation().setLatitude(55.63105);
        mEta.getLocation().setLongitude(12.5766);
        mEta.getLocation().setRadius(700000);
        mEta.getLocation().setSensor(false);
        
		mEta.getCatalogList(catalogListener).execute();
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
    		pf.toggleThumbnails();
    		break;

    	case 1:
    		pf.toggleThumbnails();
    		break;
    		
    	default:
    		break;

    	}
    	return super.onOptionsItemSelected(item);
    }
    
}
