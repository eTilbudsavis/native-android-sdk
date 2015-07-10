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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.model.Offer;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.utils.Api;
import com.shopgun.android.sdk.demo.base.BaseActivity;

import org.json.JSONObject;

public class OfferViewActivity extends BaseActivity {

    public static final String TAG = OfferViewActivity.class.getSimpleName();

    private static final String EXTRA_OFFER = "offer";

    private static final String STATE_OFFER = "state_offer";

    private Offer mOffer;

    private TextView mHeading;
    private TextView mStore;

    public static void launch(Activity a, Offer o) {
        Intent i = new Intent(a, OfferViewActivity.class);
        i.putExtra(EXTRA_OFFER, o);
        a.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mOffer = savedInstanceState.getParcelable(STATE_OFFER);
        } else {
            mOffer = getIntent().getExtras().getParcelable(EXTRA_OFFER);
        }

        setContentView(R.layout.offer_view);

        mHeading = (TextView) findViewById(R.id.heading);
        mStore = (TextView) findViewById(R.id.store);

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTexts();
        getStore();
    }

    private void getStore() {

        if (mOffer.getStore() != null) {
            return;
        }

        JsonObjectRequest storeReq = new JsonObjectRequest(Api.Endpoint.storeId(mOffer.getStoreId()), new Response.Listener<JSONObject>() {
            @Override
            public void onComplete(JSONObject response, ShopGunError error) {
                if (response != null) {
                    mOffer.setStore(Store.fromJSON(response));
                    updateTexts();
                }
            }
        });
        ShopGun.getInstance().add(storeReq);

    }

    private void updateTexts() {

        mHeading.setText(mOffer.getHeading());

        if (mOffer.getStore() != null) {

            Store s = mOffer.getStore();
            mStore.setText(s.getBranding().getName());
            mStore.setBackgroundColor(s.getBranding().getColor());
            mStore.setTextColor(Tools.getTextColor(s.getBranding().getColor()));

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_OFFER, mOffer);
    }

}
