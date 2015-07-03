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

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.model.Catalog;
import com.eTilbudsavis.etasdk.model.Hotspot;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.pageflip.PageflipFragment;
import com.eTilbudsavis.etasdk.pageflip.PageflipListener;
import com.eTilbudsavis.etasdk.pageflip.utils.PageflipUtils;
import com.eTilbudsavis.sdkdemo.base.BaseActivity;

public class CatalogViewerActivity extends BaseActivity {

    public static final String TAG = CatalogViewerActivity.class.getSimpleName();

    private static final String EXTRA_CATALOG = "catalog";
    private static final int MENU_PAGE_OVERVIEW = 1;
    private static final int REQUEST_CODE_PAGE_OVERVIEW = 432789;

    private PageflipFragment mPageflip;

    // Pageflip listener, triggered on callbacks from the pageflip.
    private PageflipListener mPageflipListener = new PageflipListener() {

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

    public static void launch(Activity a, Catalog c) {
        Intent i = new Intent(a, CatalogViewerActivity.class);
        i.putExtra(EXTRA_CATALOG, c);
        a.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_viewer);

        if (savedInstanceState != null) {
            mPageflip = PageflipFragment.newInstance(savedInstanceState);
        } else if (getIntent() != null && getIntent().getExtras() != null) {
            Catalog c = getIntent().getExtras().getParcelable(EXTRA_CATALOG);
            mPageflip = PageflipFragment.newInstance(c);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_PAGE_OVERVIEW, Menu.NONE, "Page Overview");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case MENU_PAGE_OVERVIEW:
                // We have created a simple DialogFragment - ready to use
                mPageflip.showPageOverview();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

}
