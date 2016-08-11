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

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.demo.base.BaseListActivity;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Offer;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.impl.OfferListRequest;

import java.util.ArrayList;
import java.util.List;

public class OfferListActivity extends BaseListActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = OfferListActivity.class.getSimpleName();

    private static final String STATE_OFFERS = "offers";

    private List<Offer> mOffers = new ArrayList<Offer>();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Offer o = mOffers.get(position);
        OfferViewActivity.launch(this, o);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<Offer> tmp = savedInstanceState.getParcelableArrayList(STATE_OFFERS);
            mOffers.addAll(tmp);
        }
        setListAdapter(new OfferAdapter());
        getListView().setOnItemClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mOffers.isEmpty()) {
            showProgress("Fetching catalogs");
            OfferListRequest req = new OfferListRequest(mListener);
            ShopGun.getInstance().add(req);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_OFFERS, new ArrayList<Offer>(mOffers));
    }

    LoaderRequest.Listener<List<Offer>> mListener = new LoaderRequest.Listener<List<Offer>>() {
        @Override
        public void onRequestComplete(List<Offer> response, List<ShopGunError> errors) {

            hideProgress();

            if (errors.isEmpty()) {

                if (response.isEmpty()) {

                    // This is usually the case when either location or radius could use some adjustment.
                    Tools.showDialog(OfferListActivity.this, "No offers available", "Try changing the SDK location, or increase the radius.");

                } else {

                    // The request was a success, take the first catalog and display it
                    mOffers.addAll(response);
                    ((OfferAdapter)getListAdapter()).notifyDataSetChanged();

                }

            } else {

                // There could be a bunch of things wrong here.
                // Please check the error code, and details for further information
                ShopGunError error = errors.get(0);
                String title = error.isApi() ? "API Error" : "SDK Error";
                Tools.showDialog(OfferListActivity.this, title, error.toString());
                SgnLog.e(TAG, error.getMessage(), error);

            }
        }

        @Override
        public void onRequestIntermediate(List<Offer> response, List<ShopGunError> errors) {

        }
    };

    public class OfferAdapter extends BaseAdapter {

        private int mDarkBackground = Color.argb(0x10, 0, 0, 0);
        private LayoutInflater mInflater = LayoutInflater.from(OfferListActivity.this);

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

            View v = mInflater.inflate(R.layout.offer_list_view, parent, false);
            TextView heading = (TextView) v.findViewById(R.id.heading);
            TextView price = (TextView) v.findViewById(R.id.price);

            heading.setText(o.getHeading());
            price.setText(o.getPricing().getPrice() + o.getPricing().getCurrency().getSymbol());

            if (position%2==0) {
                v.setBackgroundColor(mDarkBackground);
            }

            return v;
        }

    }
}
