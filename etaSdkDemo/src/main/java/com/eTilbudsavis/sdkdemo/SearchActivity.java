/**
 * ****************************************************************************
 * Copyright 2014 eTilbudsavis
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package com.eTilbudsavis.sdkdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Offer;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.network.impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.utils.Api.Param;

import org.json.JSONArray;

import java.util.ArrayList;

public class SearchActivity extends BaseActivity {

    public static final String TAG = SearchActivity.class.getSimpleName();

    public static final String ARG_OFFERS = "offers";
    public static final String ARG_QUERY = "query";

    EditText mQuery;
    ArrayList<Offer> mOffers;

    ProgressDialog mPd;
    ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        // Find views
        mQuery = (EditText) findViewById(R.id.etQuery);
        mListView = (ListView) findViewById(R.id.lvResult);

        // Check for any saved state
        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_OFFERS) && savedInstanceState.containsKey(ARG_QUERY)) {
            mOffers = savedInstanceState.getParcelableArrayList(ARG_OFFERS);
            if (mOffers != null) {
                mListView.setAdapter(new SearchAdapter());
            }
            String q = savedInstanceState.getString(ARG_QUERY);
            if (q != null) {
                mQuery.setText(q);
                mQuery.setSelection(q.length());
            }

        }

        System.out.print("Eta null: " + Eta.getInstance().getClass().toString());

        Button search = (Button) findViewById(R.id.btnPerformSearch);
        search.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String query = mQuery.getText().toString().trim();

                if (query.length() > 0) {

                    mPd = ProgressDialog.show(SearchActivity.this, "", "Searching...", true, true);
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

                if (mPd != null) {
                    mPd.dismiss();
                }
				
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
                    mOffers = (ArrayList<Offer>) Offer.fromJSON(response);
                    mListView.setAdapter(new SearchAdapter());

                } else {
					
					/*
					 * If the request failed, you can print the error message
					 */
                    EtaLog.d(TAG, "", error);
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(ARG_OFFERS, mOffers);
        outState.putString(ARG_QUERY, mQuery.getText().toString());
    }

    class SearchAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mOffers == null ? 0 : mOffers.size();
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
            LayoutInflater i = LayoutInflater.from(SearchActivity.this);
            TextView tv = (TextView) i.inflate(android.R.layout.simple_list_item_1, parent, false);
            tv.setText(o.getHeading());
            return tv;
        }

    }

}
