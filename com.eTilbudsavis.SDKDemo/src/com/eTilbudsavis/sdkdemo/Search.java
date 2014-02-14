package com.eTilbudsavis.sdkdemo;

import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkHelpers.JsonArrayRequest;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Param;
import com.etilbudsavis.sdkdemo.R;

public class Search extends Activity {

	public static final String TAG = "Search";

	EditText mQuery;
	Button mPerformSearch;
	ProgressDialog mPd;
	List<Offer> mOffers;
	ListView mResultDisplayer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        /*
         *  Note here, that because we setup Eta in the Main activity, 
         *  we don't necessarily need to do t again. So we can just
         *  call Eta.getInstance()
         */
        
        // Find views
        mQuery = (EditText) findViewById(R.id.etQuery);
        mResultDisplayer = (ListView) findViewById(R.id.lvResult);
        
        // Set listeners
        mPerformSearch = (Button) findViewById(R.id.btnPerformSearch);
        mPerformSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mPd = ProgressDialog.show(Search.this, "", "Searching...", true, true);
				String q = mQuery.getText().toString();
				
				/* This is where the magic happens.
				 * This simple query gets a list of offers, based on a search query. */
				
				// Create a new Listener
				Listener<JSONArray> offerListener = new Listener<JSONArray>() {

					@Override
					public void onComplete(JSONArray response, EtaError error) {

						mPd.dismiss();
						
						/* If it's a successfull request, the list will be populated and error will be null
						 * else an error is returned, and the list is null */
						if (response != null) {
							// Use the factory methods in each Class to easily convert from JSON to Object
							mOffers = Offer.fromJSON(response);
							mResultDisplayer.setAdapter(new SearchAdapter());
						} else {
							EtaLog.d(TAG, error == null ? "null" : error.toJSON().toString());
						}
						
					}
				};
				
				Bundle args = new Bundle();
				args.putString(Param.QUERY, q);
				
				// Create the request
				JsonArrayRequest offerRequest = new JsonArrayRequest(Endpoint.OFFER_SEARCH, offerListener);
				offerRequest.putQueryParameters(args);
				
				// Send the request to the SDK for execution
				Eta.getInstance().add(offerRequest);
				
			}
		});
        
        
        
    }

    @Override
    public void onResume() {
    	super.onResume();
    	Eta.getInstance().onResume();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	Eta.getInstance().onPause();
    }
    
    class SearchAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mOffers.size();
		}

		@Override
		public Object getItem(int position) {
			return mOffers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Offer o = mOffers.get(position);
			TextView tv = new TextView(getApplicationContext());
			tv.setText(o.getHeading());
			return tv;
		}
    	
    }
    
}
