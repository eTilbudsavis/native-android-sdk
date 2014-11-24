/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.sdkdemo;

import java.util.List;

import org.json.JSONArray;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.pageflip.PageGridOverview;
import com.eTilbudsavis.etasdk.pageflip.PageflipFragment;
import com.eTilbudsavis.etasdk.pageflip.PageflipListener;

public class CatalogViewer extends FragmentActivity {

	public static final String TAG = "CatalogViewer";
	PageflipFragment mPageflip;
	Catalog mCatalog;
	ProgressDialog mPd;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_viewer);
        
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Eta.getInstance().onResume();
    	mPd = ProgressDialog.show(CatalogViewer.this, "", "Getting catalogs...", true, true);
    	JsonArrayRequest catalogReq = new JsonArrayRequest(Endpoint.CATALOG_LIST, catalogListener);
    	Eta.getInstance().add(catalogReq);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	Eta.getInstance().onPause();
    }
    
	// A catalogs listener, 
    Listener<JSONArray> catalogListener = new Listener<JSONArray>() {

		@Override
		public void onComplete(JSONArray response, EtaError error) {
			
			mPd.dismiss();

			/* If the request is a success and one or more catalogs is returned,
			 * show the first catalog in a pageflip. */
			if (response != null && !(response.length() == 0) ) {
				
				List<Catalog> catalogs = Catalog.fromJSON(response);
				Catalog c = catalogs.get(0);
				
				mPd = ProgressDialog.show(CatalogViewer.this, "", "Loading catalog into pageflip...", true, true);
				
				setupPageflip(c);
				
			} else {
				
				EtaLog.e(TAG, "", error);
				
			}
		}
	};
	
	private void setupPageflip(Catalog c) {
		
		if (mPageflip == null) {
			if (getArguments().containsKey(Arg.CATALOG)) {
				
				Catalog c = (Catalog) getArguments().getSerializable(Arg.CATALOG);
				mPageflip = PageflipFragment.newInstance(c, mPages[0]);
				
			} else {
				
				String id = getArguments().getString(Arg.CATALOG_ID);
				mPageflip = PageflipFragment.newInstance(id, mPages[0]);
				
			}
			
		}
		
		mPageflip.setPageflipListener(pfl);
		get
		FragmentManager fm = getChildFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.pageflip_container, mPageflip);
		ft.commit();
		
	}
    
	// Pageflip listener, triggered on callbacks from the pageflip.
    PageflipListener pfl = new PageflipListener() {
		
		@Override
		public void onZoom(View v, int[] pages, boolean zoonIn) {
			Toast.makeText(CatalogViewer.this, "onZoom", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onSingleClick(View v, int page, float x, float y,
				List<Hotspot> hotspots) {
			Toast.makeText(CatalogViewer.this, "onSingleClick", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onReady() {
			Toast.makeText(CatalogViewer.this, "onReady", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onPageChange(int[] pages) {
			Toast.makeText(CatalogViewer.this, "onPageChange", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onOutOfBounds(boolean left) {
			Toast.makeText(CatalogViewer.this, "onOutOfBounds", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onLongClick(View v, int page, float x, float y,
				List<Hotspot> hotspots) {
			Toast.makeText(CatalogViewer.this, "onLongClick", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onError(EtaError error) {
			Toast.makeText(CatalogViewer.this, "onError", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onDragStateChanged(int state) {
			Toast.makeText(CatalogViewer.this, "onDragStateChanged", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onDoubleClick(View v, int page, float x, float y,
				List<Hotspot> hotspots) {
			Toast.makeText(CatalogViewer.this, "onDoubleClick", Toast.LENGTH_SHORT).show();
		}
	};
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

    menu.add(Menu.NONE, 0, 0, "Sideoversigt");

    return super.onCreateOptionsMenu(menu); 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    	switch (item.getItemId()) {

    	case 0:
    		int page = mPageflip.getPages()[0];
    		PageGridOverview.newInstance(mCatalog, page);
    		break;
    		
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
    
}
