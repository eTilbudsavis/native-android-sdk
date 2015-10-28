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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.demo.base.BaseListActivity;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonArrayRequest;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class CatalogListActivity extends BaseListActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = CatalogListActivity.class.getSimpleName();

    private static final String STATE_CATALOGS = "catalogs";

    private final List<Catalog> mCatalogs = new ArrayList<Catalog>();

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
            JsonArrayRequest catalogReq = new JsonArrayRequest(Endpoints.CATALOG_LIST, mCatalogListener);
            ShopGun.getInstance().add(catalogReq);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_CATALOGS, new ArrayList<Catalog>(mCatalogs));
    }

    private final Response.Listener<JSONArray> mCatalogListener = new Response.Listener<JSONArray>() {

        @Override
        public void onComplete(JSONArray response, ShopGunError error) {

            hideProgress();

            if (response != null) {

                if (response.length() == 0) {

                    // This is usually the case when either location or radius could use some adjustment.
                    Tools.showDialog(CatalogListActivity.this, "No catalogs available", "Try changing the SDK location, or increase the radius.");

                } else {

                    // The request was a success, take the first catalog and display it
                    List<Catalog> catalogs = Catalog.fromJSON(response);
                    mCatalogs.addAll(catalogs);
                    ((CatalogAdapter)getListAdapter()).notifyDataSetChanged();

                }

            } else {

                // There could be a bunch of things wrong here.
                // Please check the error code, and details for further information
                String title = error.isApi() ? "API Error" : "SDK Error";
                Tools.showDialog(CatalogListActivity.this, title, error.toString());
                SgnLog.e(TAG, error.getMessage(), error);

            }
        }
    };

    public class CatalogAdapter extends BaseAdapter {

        private final int mPadding = Utils.convertDpToPx(5, CatalogListActivity.this);

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
            TextView tv = new TextView(CatalogListActivity.this);
            Catalog c = mCatalogs.get(position);
            tv.setText(c.getBranding().getName());
            tv.setTextSize(30);
            tv.setPadding(mPadding, mPadding, mPadding, mPadding);
            int color = c.getBranding().getColor();
            tv.setBackgroundColor(color);
            tv.setTextColor(Tools.getTextColor(color));
            return tv;
        }

    }
}
