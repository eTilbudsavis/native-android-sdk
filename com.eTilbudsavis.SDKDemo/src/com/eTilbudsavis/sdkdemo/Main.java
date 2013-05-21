package com.eTilbudsavis.sdkdemo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Utilities;
import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "SDKDEMO";
	// Create ETA and API objects.
	private Eta mEta;

	// Set API key and secret.
	private String mApiKey = "";
	
	// Specify endpoint.
	private String mUrl = "/api/v1/catalog/list/";
	private JSONArray catalogArray;
	
	private Button btnGetCatalog;
	private Button btnCatalogInfo;
	private Button btnWebView;
	private ProgressDialog progressDialog;
	private Spinner spinner;
	private List<String> spinnerList = new ArrayList<String>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mEta = new Eta(mApiKey, this);
        
        Utilities.logd(TAG, mEta.getSession().getJson());
        
        btnGetCatalog = (Button)findViewById(R.id.btnGetCatalog);
        btnCatalogInfo = (Button)findViewById(R.id.btnCatalogInfo);
        btnWebView = (Button)findViewById(R.id.btnWebView);
        
        spinner = (Spinner)findViewById(R.id.spinnerCatalogList);
        
        // A new onClick listener for the get catalogs button.
        btnGetCatalog.setOnClickListener(new OnClickListener() {		
        	// OnClick we will make the API request, and send along a new RequestListener.
			@Override
			public void onClick(View v) {
				progressDialog = ProgressDialog.show(Main.this, "Getting catalogs", "Downloading...");

			}
		});
        
    }
    
    private void setupSpinner() throws JSONException {
    	if (catalogArray.length() > 0) {
        	ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinnerList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	
            // Iterate over the JSONArray to get elements.
        	for (int i = 0; i < catalogArray.length(); i++) {
    			spinnerList.add(catalogArray.getJSONObject(i).getJSONObject("dealer").getString("name"));
    		}
    		spinner.setAdapter(dataAdapter);
    	}
    }
    
}