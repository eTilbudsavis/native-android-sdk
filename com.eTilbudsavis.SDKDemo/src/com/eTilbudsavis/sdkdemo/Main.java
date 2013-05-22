package com.eTilbudsavis.sdkdemo;

import android.app.Activity;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Session;
import com.eTilbudsavis.etasdk.Utilities;
import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "SDKDEMO";
	// Create ETA and API objects.
	private Eta mEta;

	// Set API key and secret.
	private String mApiKey = "";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mEta = new Eta(mApiKey, this);
        mEta.getSession();
        Utilities.logd(TAG, mEta.getSession().getJson());
        
    }
    
}