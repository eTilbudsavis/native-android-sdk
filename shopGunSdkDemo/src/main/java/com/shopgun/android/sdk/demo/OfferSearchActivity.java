/*******************************************************************************
 * Copyright 2015 ShopGun
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
 ******************************************************************************/

package com.shopgun.android.sdk.demo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.demo.base.BaseActivity;
import com.shopgun.android.sdk.model.Offer;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.impl.OfferListRequest;

import java.util.ArrayList;
import java.util.List;

public class OfferSearchActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final String TAG = OfferSearchActivity.class.getSimpleName();

    public static final String STATE_OFFERS = "offers";
    public static final String STATE_QUERY = "query";

    private EditText mQuery;
    private ArrayList<Offer> mOffers = new ArrayList<Offer>();

    private ProgressDialog mPd;
    private OfferAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for any saved state
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_OFFERS) && savedInstanceState.containsKey(STATE_QUERY)) {
            ArrayList<Offer> tmp = savedInstanceState.getParcelableArrayList(STATE_OFFERS);
            mOffers.addAll(tmp);
            String q = savedInstanceState.getString(STATE_QUERY);
            if (q != null) {
                mQuery.setText(q);
                mQuery.setSelection(q.length());
            }

        }

        setContentView(R.layout.search);

        // Find views
        mQuery = (EditText) findViewById(R.id.etQuery);
        ListView listView = (ListView) findViewById(R.id.lvResult);

        mAdapter = new OfferAdapter(this, mOffers);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);

        Button search = (Button) findViewById(R.id.btnPerformSearch);
        search.setOnClickListener(this);

    }

    /*
     * This is where the magic happens.
     * This simple query gets a list of offers, based on a search query.
     */
    private void performSearch(String query) {

        LoaderRequest.Listener<List<Offer>> mSearchListener = new LoaderRequest.Listener<List<Offer>>() {
            @Override
            public void onRequestComplete(List<Offer> response, List<ShopGunError> errors) {
                if (errors.isEmpty()) {
                    mOffers.addAll(response);
                    mAdapter.notifyDataSetChanged();
                } else {
                    // Do error checking and handle situation appropriately
                }
            }

            @Override
            public void onRequestIntermediate(List<Offer> response, List<ShopGunError> errors) {
                // Perform UI updates if necessary
            }
        };

        OfferListRequest request = new OfferListRequest(Endpoints.OFFER_SEARCH, mSearchListener);
        request.getParameters().put(Parameters.QUERY, query);

        // Send the request to the SDK for execution
        ShopGun.getInstance().add(request);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STATE_OFFERS, mOffers);
        outState.putString(STATE_QUERY, mQuery.getText().toString());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnPerformSearch:
                String query = mQuery.getText().toString().trim();
                if (query.length() > 0) {
                    mPd = ProgressDialog.show(OfferSearchActivity.this, "", "Searching...", true, true);
                    performSearch(query);
                }
                break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Offer o = mOffers.get(position);
        OfferViewActivity.launch(this, o);
    }
}
