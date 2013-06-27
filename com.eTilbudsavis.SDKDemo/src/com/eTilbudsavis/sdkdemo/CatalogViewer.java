package com.eTilbudsavis.sdkdemo;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Api;
import com.eTilbudsavis.etasdk.Api.CallbackCatalogList;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Pageflip;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.Pageflip.PageflipListener;
import com.eTilbudsavis.etasdk.Session.SessionListener;
import com.eTilbudsavis.etasdk.Tools.Utilities;
import com.etilbudsavis.sdkdemo.R;

public class CatalogViewer extends Activity {

	public static final String TAG = "CatalogViewer";
	Eta mEta;
	String catId;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_viewer);
        
        mEta = new Eta(Keys.API_KEY, Keys.API_SECRET, this);
        mEta.debug(true);

        mEta.clearPreferences();
        
        mEta.getLocation().set(55.63105, 12.5766, 700000, false);	// Fields
        
        mEta.getSession().subscribe(new SessionListener() {
			
			@Override
			public void onUpdate() {
				mEta.getSession().unSubscribe(this);
				getCats();
			}
		}).start();
        
    }
    
    private void getCats() {
    	Api.CallbackCatalogList cbcl = new CallbackCatalogList() {
			
			@Override
			public void onComplete(int statusCode, ArrayList<Catalog> catalogs,
					EtaError error) {

				if (statusCode == 200 && !catalogs.isEmpty()) {
					Tools.logd(TAG, catalogs.get(0).toString());
					pageflip(catalogs.get(0).getId());
				} else {
					Tools.logd(TAG, error.toString());
				}
			}
		};
		mEta.getCatalogList(cbcl).execute();
    }
    
    private void pageflip(String id) {

        PageflipListener pfl = new PageflipListener() {
			
			@Override
			public void onPageflipEvent(String event, JSONObject object) {
				Utilities.logd(TAG, event + " - " + object.toString());
			}
		};
        
        Pageflip pf = (Pageflip)findViewById(R.id.pageflip);
        pf.execute(mEta, pfl, id);
        
    }

}
