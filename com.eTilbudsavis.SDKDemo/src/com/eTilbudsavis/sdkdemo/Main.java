package com.eTilbudsavis.sdkdemo;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.API.RequestListener;
import com.eTilbudsavis.etasdk.ETA;
import com.etilbudsavis.sdkdemo.R;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class Main extends Activity {

	// Create ETA and API objects.
	public ETA eta;

	// Set API key and secret.
	private String mApiKey = "";
	private String mApiSecret = "";
	
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
        
        eta = new ETA(mApiKey, mApiSecret, getApplicationContext());
        
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

				eta.api.request(mUrl, new RequestListener() {
					// onSuccess we will add items to the spinner, and make it visible.
					@Override
					public void onSuccess(Integer response, Object object) {
						try {
							// First cast "object" to a JSONObject->JSONArray.
							catalogArray = new JSONObject(object.toString()).getJSONArray("data");

							// Then setup the spinner.
							setupSpinner();
							btnCatalogInfo.setEnabled(true);
							btnWebView.setEnabled(true);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						progressDialog.dismiss();
					}

					@Override
					public void onError(Integer response, Object object) {
						progressDialog.dismiss();
						new AlertDialog.Builder(Main.this)
						.setTitle("Error")
						.setMessage("Did you remember to add API key and secret?")
						.setNeutralButton("Ok", null)
						.show();
					}
				});
				
			}
		});
        
        btnCatalogInfo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, CatalogInfo.class);
				intent.putExtra("JSONObject", catalogArray.optJSONObject(spinner.getSelectedItemPosition()).toString());
		        startActivity(intent);
			}
		});
        
        btnWebView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Main.this, CatalogWebView.class);
				intent.putExtra("JSONObject", catalogArray.optJSONObject(spinner.getSelectedItemPosition()).toString());
				intent.putExtra("eta", eta);
		        startActivity(intent);
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