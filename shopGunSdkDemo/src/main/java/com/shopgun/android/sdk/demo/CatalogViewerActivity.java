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
import android.view.Menu;
import android.view.MenuItem;

import com.shopgun.android.sdk.demo.base.BaseActivity;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.pageflip.PageflipFragment;

public class CatalogViewerActivity extends BaseActivity {

    public static final String TAG = CatalogViewerActivity.class.getSimpleName();

    private static final String FRAGMENT_PAGEFLIP_TAG = TAG + ".pageflip";

    private static final String EXTRA_CATALOG = "catalog";
    private static final int MENU_PAGE_OVERVIEW = 1;

    private PageflipFragment mPageflip;

    public static void launch(Activity a, Catalog c) {
        Intent i = new Intent(a, CatalogViewerActivity.class);
        i.putExtra(EXTRA_CATALOG, c);
        a.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_viewer);

        mPageflip = (PageflipFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_PAGEFLIP_TAG);
        if (mPageflip == null) {
            Catalog c = getIntent().getExtras().getParcelable(EXTRA_CATALOG);
            mPageflip = PageflipFragment.newInstance(c);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.pageflip, mPageflip, FRAGMENT_PAGEFLIP_TAG)
                    .commit();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mPageflip.setPageflipListener(new PageflipListenerPrinter(TAG, false));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPageflip.setPageflipListener(null);
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
