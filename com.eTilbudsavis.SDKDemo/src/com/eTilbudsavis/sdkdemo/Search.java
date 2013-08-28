package com.eTilbudsavis.sdkdemo;

import java.util.List;

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

import com.eTilbudsavis.etasdk.Api;
import com.eTilbudsavis.etasdk.Api.ListListener;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.Utils.Utils;
import com.etilbudsavis.sdkdemo.R;

public class Search extends Activity {

	public static final String TAG = "Search";

	Eta mEta;
	EditText mQuery;
	Button mPerformSearch;
	ProgressDialog mPd;
	List<Offer> mOffers;
	ListView mResultDisplayer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        // Instantiate a new Eta object;
        mEta = new Eta(Keys.API_KEY, Keys.API_SECRET, getApplicationContext());
        
        // Enable debugging, if you want the SDK to output to LogCat
        mEta.debug(true);
        
        // Set the location, several methods are available
        mEta.getLocation().setLatitude(55.63105);
        mEta.getLocation().setLongitude(12.5766);
        mEta.getLocation().setRadius(700000);
        mEta.getLocation().setSensor(false);

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
				ListListener<Offer> offerListener = new ListListener<Offer>() {
					
					@Override
					public void onComplete(boolean isCache, int statusCode, List<Offer> list, EtaError error) {
						
						mPd.dismiss();
						
						/* If it's a successfull request, the list will be populated and error will be null
						 * else an error is returned, and the list is null */
						if (Utils.isSuccess(statusCode)) {
							mOffers = list;
							mResultDisplayer.setAdapter(new SearchAdapter());
						} else {
							Utils.logd(TAG, error.toString());
						}
						
					}
				};
				
				// Make the Api object, with one of the simple wrapper methods found in mEta
				Api api = mEta.searchOffers(offerListener, q);
				
				/* You can set other options on the Api object, as you please.
				 * E.g.: You can enable the debug flag, to get more info on this particulare
				 * Api request as it executes. 
				 * (This requires the Eta object to have set debugging to true, mEta.debug(true)) */
				api.setFlag(Api.FLAG_PRINT_DEBUG);
				
				// Finally execute the Api request, as you would with any other AsyncTast :-)
				api.execute();
				
				// This can all be wrapped into a one-liner but, for these examples we prefer some readability :-)
				
			}
		});
        
        
        
    }

    @Override
    public void onResume() {
    	super.onResume();
    	mEta.onResume();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mEta.onPause();
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
