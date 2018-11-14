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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.demo.base.BaseListActivity;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.impl.CatalogListRequest;
import com.shopgun.android.sdk.utils.CatalogThumbBitmapTransformation;
import com.shopgun.android.utils.UnitUtils;

import java.util.ArrayList;
import java.util.List;

public class CatalogListActivity extends BaseListActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = CatalogListActivity.class.getSimpleName();

    private static final String STATE_CATALOGS = "catalogs";

    private final List<Catalog> mCatalogs = new ArrayList<Catalog>();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Catalog c = mCatalogs.get(position);
        PagedPublicationActivity.start(this, c);
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
            // Just because it's possible doesn't mean you have to attach everything.
            // The more you add, the worse performance you'll get...
            r.loadStore(true);
            r.loadDealer(true);
            // Limit is default to 24, it's good for cache performance on the API
            r.setLimit(24);
            // Offset can be used for pagination, and if default to 0
            r.setOffset(0);
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
        public void onRequestComplete(List<Catalog> response, List<ShopGunError> errors) {
            update(response, errors);
        }

        @Override
        public void onRequestIntermediate(List<Catalog> response, List<ShopGunError> errors) {
            update(response, errors);
        }
    };

    private void update(List<Catalog> response, List<ShopGunError> errors) {

        if (errors.isEmpty()) {

            hideProgress();

            if (response.size() == 0) {

                // This is usually the case when either location or radius could use some adjustment.
                String title = "No catalogs available";
                String msg = "Try changing the SDK location, or increase the radius.";
                Tools.showDialog(CatalogListActivity.this, title, msg);

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
            Tools.showDialog(CatalogListActivity.this, title, error.toString());
            SgnLog.e(TAG, error.getMessage(), error);

        }

    }

    public class CatalogAdapter extends BaseAdapter {

        private final int mPadding = UnitUtils.dpToPx(5, CatalogListActivity.this);

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
            if(convertView == null){
                convertView = LayoutInflater.from(CatalogListActivity.this).inflate(R.layout.card_catalog, parent, false);
            }
            TextView tv = convertView.findViewById(R.id.card_catalog_dealer);
            TextView tv1 = convertView.findViewById(R.id.card_catalog_distance);
            TextView tv2 = convertView.findViewById(R.id.card_catalog_street);
            TextView tv3 = convertView.findViewById(R.id.card_catalog_zipcode);
            ImageView logo = convertView.findViewById(R.id.card_catalog_logo);

            Catalog c = mCatalogs.get(position);

            tv.setText(c.getBranding().getName());
            Store store = c.getStore();
            if (store != null) {
                tv1.setText(store.getCity());
                tv2.setText(store.getStreet());
                tv3.setText(store.getZipcode());
            }

            GlideApp.with(getApplicationContext())
                    .load(c.getImages().getThumb())
                    .transform(new CatalogThumbBitmapTransformation(c)) // consider using .fitCenter() from Glide
                    .placeholder(R.drawable.placeholder_px)
                    .into(logo);

            return convertView;
        }

    }
}
