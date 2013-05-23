package com.eTilbudsavis.sdkdemo;

import java.util.ArrayList;

import Utils.Endpoint;
import Utils.Utilities;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Api;
import com.eTilbudsavis.etasdk.Api.CatalogsListener;
import com.eTilbudsavis.etasdk.Api.RequestListener;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Session.SessionListener;
import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "Main";
	
	// Create ETA and API objects.
	private Eta mEta;
	// Set API key and secret.
	private String mApiKey = "590990e7ad62f206512ce390939ea3d3";
	private String mApiSecret = "braTh_chEs_etrA*e-aJAg&6axUg=zE*eza";
	
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
    	
    	mEta.getCatalogs(new CatalogsListener() {
			
			@Override
			public void onComplete(int responseCode, Object object) {
				Utilities.logd(TAG, "Code: " + String.valueOf(responseCode) + ", Data: " + object.toString());
			}
			
			@Override
			public void onComplete(int responseCode, ArrayList<Catalog> catalogs) {
				
			}
		}, 0);
    	
//    	Api a = new Api(mEta);
//    	a.build(Endpoint.CATALOG_LIST, new RequestListener() {
//			
//			@Override
//			public void onComplete(int responseCode, Object object) {
//				Utilities.logd(TAG, "Code: " + String.valueOf(responseCode) + ", Data: " + object.toString());
//			}
//		});
//    	a.execute();
    }
    
}