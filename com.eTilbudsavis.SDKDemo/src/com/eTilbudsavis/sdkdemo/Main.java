package com.eTilbudsavis.sdkdemo;

import java.util.ArrayList;

import Utils.Utilities;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Api;
import com.eTilbudsavis.etasdk.Api.CatalogListListener;
import com.eTilbudsavis.etasdk.Api.CatalogListener;
import com.eTilbudsavis.etasdk.Api.StoreListener;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Session.SessionListener;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "Main";
	
	private Eta mEta;
	private String mApiKey = Keys.API_KEY;
	private String mApiSecret = Keys.API_SECRET;
	private ArrayList<Catalog> catalogList = new ArrayList<Catalog>();
	private int mTestId = 0;

	TextView tvSession;
	TextView tvCatalogList;
	TextView tvCatalog;
	TextView tvStore;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        tvSession = (TextView)findViewById(R.id.tvSession);
        tvCatalogList = (TextView)findViewById(R.id.tvCatalogList);
        tvCatalog = (TextView)findViewById(R.id.tvCatalog);
        tvStore = (TextView)findViewById(R.id.tvStore);
        
        mEta = new Eta(mApiKey, mApiSecret, this);
        // Herrup
//        mEta.getLocation().set(56.40875, 8.91922, 15000, false);
        // Fields
//        mEta.getLocation().set(55.63105, 12.5766, 700000, false);
        // Nørresundby
        mEta.getLocation().set(57.057582, 9.934028, 5000, false);
        
        mEta.getSession().subscribe(new SessionListener() {
			
			@Override
			public void onUpdate() {
				tvSession.setText(mEta.getSession().toString(true));
				getAllCatalogs();
			}
		}).start();
        
    }
    
    private void getAllCatalogs() {
    	
    	mEta.getCatalogList(new CatalogListListener() {
    		
    		@Override
    		public void onComplete(int statusCode, Object object) {
    			
    			if (statusCode == 200) {
    				@SuppressWarnings("unchecked")
    				ArrayList<Catalog> tmp = (ArrayList<Catalog>)object;
    				catalogList.addAll(tmp);
    				if (tmp.size() == Api.DEFAULT_LIMIT)
    					getAllCatalogs();
    				else {
    					getStore();
    					getCatalog();
    				}
    			} else if (statusCode == 204){
					getStore();
    				getCatalog();
    			}
    			StringBuilder sb = new StringBuilder();
//    			for (Catalog c : cs)
//    				sb.append(c.toString()).append("\n");
    			
    			tvCatalogList.setText(sb.append("Count: ").append(catalogList.size()).toString());
    		}
    	}, catalogList.size(), new String[] {Catalog.SORT_NAME});
    	
    }
    
    private void getCatalog() {
    	mEta.getCatalogId(new CatalogListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				Catalog c = (Catalog)object;
				catalogList.get(mTestId).set(c);
				tvCatalog.setText(c.toString(true));
			}
		}, catalogList.get(mTestId).getId());
    }
    
    private void getStore() {
    	mEta.getStoreId(new StoreListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				
				if (statusCode == 200) {
					Store s = (Store)object;
					catalogList.get(mTestId).setStore(s);
					tvStore.setText(s.toString());
				}
			}
		}, catalogList.get(mTestId).getStoreId());
    }
    
}