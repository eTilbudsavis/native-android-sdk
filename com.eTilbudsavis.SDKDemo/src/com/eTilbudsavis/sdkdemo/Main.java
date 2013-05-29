package com.eTilbudsavis.sdkdemo;

import java.util.ArrayList;

import Utils.Utilities;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Api.CatalogListListener;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Session.SessionListener;
import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "Main";
	
	// Create ETA and API objects.
	private Eta mEta;
	// Set API key and secret.
	private String mApiKey = Keys.API_KEY;
	private String mApiSecret = Keys.API_SECRET;
	
	TextView tvSession;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tvSession = (TextView)findViewById(R.id.tvSession);
        
        mEta = new Eta(mApiKey, mApiSecret, this);
        
        mEta.getLocation().set(55.63105, 12.5766, 10000, false);
        
		if (mEta.getSession().getJson() != null) {
			tvSession.setText(mEta.getSession().toString());
			cat();
		} else {
			mEta.getSession().subscribe(new SessionListener() {
				
				@Override
				public void onUpdate() {
					tvSession.setText(mEta.getSession().toString());
					cat();
				}
			});
		}
		
    }
    
    public void cat() {
    	
    	mEta.getCatalogs(new CatalogListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				ArrayList<Catalog> cs = (ArrayList<Catalog>)object;
				for (Catalog c : cs)
					Utilities.logd(TAG, c.toString());
			}
		}, 0);
    	
    }
    
}