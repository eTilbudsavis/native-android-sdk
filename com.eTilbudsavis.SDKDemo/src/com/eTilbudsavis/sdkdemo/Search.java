/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
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
	ProgressDialog mPd;
	List<Offer> mOffers;
	ListView mResultDisplayer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        /*
         *  Because we 'set' Eta in the Main activity, it's not necessarily to
         *  do it again. And therefore you can just call Eta.getInstance() when
         *  ever you need the SDK
         */
        
        // Find views
        mQuery = (EditText) findViewById(R.id.etQuery);
        mResultDisplayer = (ListView) findViewById(R.id.lvResult);
        
        Button search = (Button) findViewById(R.id.btnPerformSearch);
        search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String query = mQuery.getText().toString().trim();
				
				if (query.length() > 0) {
					
					mPd = ProgressDialog.show(Search.this, "", "Searching...", true, true);
					performSearch(query);
					
				}
				
			}
		});
        
        
        
    }

	/* 
	 * This is where the magic happens.
	 * This simple query gets a list of offers, based on a search query.
	 */
    private void performSearch(String query) {

		/*
		 * Create a new Listener.
		 * 
		 * This is a JSONArray listener, and it's therefore important
		 * to request an API endpoint that returns valid JSONArray data,
		 * or you will get a ParseError.
		 */
		Listener<JSONArray> offerListener = new Listener<JSONArray>() {

			@Override
			public void onComplete(JSONArray response, EtaError error) {
				
				mPd.dismiss();
				
				/* 
				 * Determining the state of the request response is simple.
				 * 
				 * If it's a successful request, the response will be populated
				 * and the error object will be null. And if the request failed
				 * the error object will be populated, and the request will be
				 * null.
				 * 
				 */
				if (response != null) {
					
					/*
					 * Generate object from the JSONArray, with the factory method
					 * in the Offer object.
					 */
					mOffers = Offer.fromJSON(response);
					mResultDisplayer.setAdapter(new SearchAdapter());
					
				} else {
					
					/*
					 * If the request failed, you can print the error message
					 */
					EtaLog.d(TAG, error.toJSON().toString());
				}
				
			}
		};
		
		Bundle args = new Bundle();
		args.putString(Param.QUERY, query);
		
		// Create the request
		JsonArrayRequest offerRequest = new JsonArrayRequest(Endpoint.OFFER_SEARCH, offerListener);
		offerRequest.putQueryParameters(args);
		
		// Send the request to the SDK for execution
		Eta.getInstance().add(offerRequest);
		
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
