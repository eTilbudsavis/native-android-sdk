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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.demo.base.BaseListActivity;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.impl.CatalogListRequest;
import com.shopgun.android.sdk.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CatalogListLoaderActivity extends BaseListActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = CatalogListLoaderActivity.class.getSimpleName();

    private static final String STATE_CATALOGS = "catalogs";

    private final List<Catalog> mCatalogs = new ArrayList<Catalog>();

    long start = 0;

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Catalog c = mCatalogs.get(position);
        CatalogViewerActivity.launch(this, c);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            ArrayList<Catalog> tmp = savedInstanceState.getParcelableArrayList(STATE_CATALOGS);
            mCatalogs.addAll(tmp);
        }
        setListAdapter(new CatalogAdapter());
        getListView().setOnItemClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCatalogs.isEmpty()) {
            showProgress("Fetching catalogs");
            CatalogListRequest r = new CatalogListRequest(mCatalogListener);
            r.loadStore(true);
            r.setLimit(10);
            start = System.nanoTime();
            ShopGun.getInstance().add(r);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_CATALOGS, new ArrayList<Catalog>(mCatalogs));
    }

    private final LoaderRequest.Listener<List<Catalog>> mCatalogListener = new LoaderRequest.Listener<List<Catalog>>() {
        @Override
        public void onComplete(List<Catalog> response, List<ShopGunError> errors) {
            Log.d(TAG, "onComplete");
            SgnLog.d(TAG, "Time: " + (TimeUnit.NANOSECONDS.toMillis((System.nanoTime()-start))));
            update(response, errors);
        }

        @Override
        public void onIntermediate(List<Catalog> response, List<ShopGunError> errors) {
            Log.d(TAG, "onIntermediate");
            update(response, errors);
        }
    };

    private void update(List<Catalog> response, List<ShopGunError> errors) {

        hideProgress();

        if (response != null) {

            if (response.size() == 0) {

                // This is usually the case when either location or radius could use some adjustment.
                Tools.showDialog(CatalogListLoaderActivity.this, "No catalogs available", "Try changing the SDK location, or increase the radius.");

            } else {

                // The request was a success, take the first catalog and display it
                if (mCatalogs.isEmpty()) {
                    mCatalogs.addAll(response);
                }
                ((CatalogAdapter)getListAdapter()).notifyDataSetChanged();

            }

        } else {

            // There could be a bunch of things wrong here.
            // Please check the error code, and details for further information
            ShopGunError error = errors.get(0);
            String title = error.isApi() ? "API Error" : "SDK Error";
            Tools.showDialog(CatalogListLoaderActivity.this, title, error.toString());
            SgnLog.e(TAG, error.getMessage(), error);

        }
    }

    public class CatalogAdapter extends BaseAdapter {

        private final int mPadding = Utils.convertDpToPx(5, CatalogListLoaderActivity.this);

        @Override
        public int getCount() {
            return mCatalogs.size();
        }

        @Override
        public Object getItem(int position) {
            return mCatalogs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(CatalogListLoaderActivity.this);
            Catalog c = mCatalogs.get(position);
            String text = c.getBranding().getName();
            if (c.getStore() != null) {
                text = text + ", " + c.getStore().getCity();
            }
            tv.setText(text);
            tv.setTextSize(30);
            tv.setPadding(mPadding, mPadding, mPadding, mPadding);
            int color = c.getBranding().getColor();
            tv.setBackgroundColor(color);
            tv.setTextColor(Tools.getTextColor(color));
            return tv;
        }

    }
}
