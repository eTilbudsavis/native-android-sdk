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

import java.util.List;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Catalog;
import com.eTilbudsavis.etasdk.model.Hotspot;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.network.impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.pageflip.PageOverviewDialog;
import com.eTilbudsavis.etasdk.pageflip.PageflipFragment;
import com.eTilbudsavis.etasdk.pageflip.PageflipListener;
import com.eTilbudsavis.etasdk.pageflip.utils.PageflipUtils;
import com.eTilbudsavis.etasdk.utils.Api.Endpoint;

public class CatalogViewerActivity extends BaseActivity {

    public static final String TAG = CatalogViewerActivity.class.getSimpleName();

    private static final int MENU_PAGEOVERVIEW = 1;

    PageflipFragment mPageflip;
    ProgressDialog mProgressDialog;
    // Pageflip listener, triggered on callbacks from the pageflip.
    PageflipListener mPageflipListener = new PageflipListener() {

        @Override
        public void onZoom(View v, int[] pages, boolean zoonIn) {
            String text = "zoom " + (zoonIn ? "in" : "out");
            Toast.makeText(CatalogViewerActivity.this, text, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSingleClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
            if (hotspots.isEmpty()) {
                Toast.makeText(CatalogViewerActivity.this, "onSingleClick", Toast.LENGTH_SHORT).show();
            } else if (hotspots.size() == 1) {
                Toast.makeText(CatalogViewerActivity.this, "onSingleClick (" + hotspots.get(0).getOffer().getHeading() + ")", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CatalogViewerActivity.this, "onSingleClick (" + hotspots.size() + ")", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onReady() {
            Toast.makeText(CatalogViewerActivity.this, "onReady", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPageChange(int[] pages) {
            String text = "onPageChange: " + PageflipUtils.join("-", pages);
            Toast.makeText(CatalogViewerActivity.this, text, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onOutOfBounds(boolean left) {
            Toast.makeText(CatalogViewerActivity.this, "onOutOfBounds", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLongClick(View v, int page, float x, float y,
                                List<Hotspot> hotspots) {
            Toast.makeText(CatalogViewerActivity.this, "onLongClick", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(EtaError error) {
            Toast.makeText(CatalogViewerActivity.this, "onError", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDragStateChanged(int state) {
            // Called too often - will spam the toast queue
//			Toast.makeText(CatalogViewer.this, "onDragStateChanged", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDoubleClick(View v, int page, float x, float y,
                                  List<Hotspot> hotspots) {
            Toast.makeText(CatalogViewerActivity.this, "onDoubleClick", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_viewer);

        if (savedInstanceState != null) {
            mPageflip = PageflipFragment.newInstance(savedInstanceState);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mPageflip == null) {

            // First load, get a fresh list of catalogs for our area
            mProgressDialog = ProgressDialog.show(CatalogViewerActivity.this, "", "Getting catalog list...", true, true);
            getCatalogList();

        } else {

            // No need to load content again, as PageflipFragment have been setup from the saved instance state
            setupPageflip();

        }

    }

    private void getCatalogList() {

        Listener<JSONArray> mCatalogListener = new Listener<JSONArray>() {

            @Override
            public void onComplete(JSONArray response, EtaError error) {

                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }

                if (response != null && response.length() > 0) {

                    // The request was a success, take the first catalog and display it
                    List<Catalog> catalogs = Catalog.fromJSON(response);
                    mPageflip = PageflipFragment.newInstance(catalogs.get(0));
                    setupPageflip();

                } else {

                    dialog("No catalogs", "Try changing the SDK location, or increase the radius.");
                    EtaLog.e(TAG, "", error);

                }
            }
        };

        JsonArrayRequest catalogReq = new JsonArrayRequest(Endpoint.CATALOG_LIST, mCatalogListener);
        // This debugger prints relevant information about a request
//    	catalogReq.setDebugger(new NetworkDebugger());
        Eta.getInstance().add(catalogReq);

    }

    private void setupPageflip() {

        mPageflip.setPageflipListener(mPageflipListener);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.pageflip, mPageflip);
        ft.commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mPageflip.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    private void dialog(String title, String message) {
        AlertDialog.Builder b = new Builder(CatalogViewerActivity.this);
        b.setTitle(title);
        b.setMessage(message);
        b.setPositiveButton("OK", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        b.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_PAGEOVERVIEW, Menu.NONE, "Pageoverview");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case MENU_PAGEOVERVIEW:
                // We have created a simple DialogFragment - ready to use

                // The first element of the array returned in getPages is (almost) guaranteed to be set
                int page = mPageflip.getPages()[0];
                Catalog catalog = mPageflip.getCatalog();
                PageOverviewDialog f = PageOverviewDialog.newInstance(catalog, page);
                f.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mPageflip.setPage(position);
                    }
                });
                f.show(getSupportFragmentManager(), "PageGridOverview");
                break;

        }

        return super.onOptionsItemSelected(item);
    }

}
