package com.eTilbudsavis.sdkdemo;

import java.util.ArrayList;

import Utils.Utilities;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Api.CatalogListListener;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Session.SessionListener;
import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "Main";
	
	// Create ETA and API objects.
	private Eta mEta;
	// Set API key and secret.
	private String mApiKey = Keys.API_KEY;
	private String mApiSecret = Keys.API_SECRET;
	ArrayList<Catalog> cs = new ArrayList<Catalog>();
	
	TextView tvSession;
	TextView tvCatalogs;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tvSession = (TextView)findViewById(R.id.tvSession);
        tvCatalogs = (TextView)findViewById(R.id.tvCatalogs);
        
        mEta = new Eta(mApiKey, mApiSecret, this);
        // Herrup
        mEta.getLocation().set(56.40875, 8.91922, 15000, false);
        // Fields
//        mEta.getLocation().set(55.63105, 12.5766, 10000, false);
        mEta.getSession().subscribe(new SessionListener() {
			
			@Override
			public void onUpdate() {
				tvSession.setText(mEta.getSession().toString());
				catalogs();
			}
		}).start();
        
    }
    
    public void catalogs() {
    	
    	mEta.getCatalogs(new CatalogListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				
				switch (statusCode) {
				case 200:
					ArrayList<Catalog> tmp = (ArrayList<Catalog>)object;
					cs.addAll(tmp);
					if (tmp.size() == 25) {
						catalogs();
					} 
					break;

				default:
					break;
				}
				print();
			}
		}, cs.size(), new String[] {Catalog.SORT_NAME});
    	
    }
    
    public void print() {

		StringBuilder sb = new StringBuilder();
		for (Catalog c : cs)
			sb.append(c.toString()).append("\n");
		
		tvCatalogs.setText(sb.append(cs.size()).toString());

    }
    
}