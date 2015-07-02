/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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

package com.eTilbudsavis.sdkdemo;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Catalog;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Response;
import com.eTilbudsavis.etasdk.network.impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.network.impl.NetworkDebugger;
import com.eTilbudsavis.etasdk.utils.Api;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class CatalogListActivity extends BaseListActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = CatalogListActivity.class.getSimpleName();

    private static final String STATE_CATALOGS = "catalogs";

    private List<Catalog> mCatalogs = new ArrayList<Catalog>();

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
            getCatalogList();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_CATALOGS, new ArrayList<Catalog>(mCatalogs));
    }

    Response.Listener<JSONArray> mCatalogListener = new Response.Listener<JSONArray>() {

        @Override
        public void onComplete(JSONArray response, EtaError error) {

            hideProgress();

            if (response != null && response.length() > 0) {

                // The request was a success, take the first catalog and display it
                List<Catalog> catalogs = Catalog.fromJSON(response);
                mCatalogs.addAll(catalogs);
                ((CatalogAdapter)getListAdapter()).notifyDataSetChanged();

            } else {

                showDislog("No catalogs", "Try changing the SDK location, or increase the radius.");
                EtaLog.e(TAG, error.getMessage(), error);

            }
        }
    };

    private void getCatalogList() {

        JsonArrayRequest catalogReq = new JsonArrayRequest(Api.Endpoint.CATALOG_LIST, mCatalogListener);
        // This debugger prints relevant information about a request
        catalogReq.setDebugger(new NetworkDebugger());
        Eta.getInstance().add(catalogReq);

    }

    public class CatalogAdapter extends BaseAdapter {

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
            return tv;
        }
    }
}
