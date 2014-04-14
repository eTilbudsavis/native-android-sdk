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
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.PageflipWebview;
import com.eTilbudsavis.etasdk.PageflipWebview.PageflipListener;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.etilbudsavis.sdkdemo.R;

public class CatalogViewer extends Activity {

	public static final String TAG = "CatalogViewer";
	PageflipWebview mPageflip;
	ProgressDialog mPd;
	// Pageflip viewer hack
	String mViewSession = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_viewer);
        
        /*
         *  Note here, that because we setup Eta in the Main activity, 
         *  we don't necessarily need to do t again. So we can just
         *  call Eta.getInstance()
         */
        
		mPageflip = (PageflipWebview)findViewById(R.id.pageflip);
		
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
    	mPageflip.closePageflip();
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
				
		        mPageflip.execute(Eta.getInstance(), pfl, c.getId());
		        
			} else {
				
				EtaLog.e(TAG, error);
				
			}
		}
	};
	
    
	// Pageflip listener, triggered on callbacks from the pageflip.
    PageflipListener pfl = new PageflipListener() {
		
		@Override
		public void onEvent(String event, String uuid, JSONObject object) {
			Toast.makeText(getApplicationContext(), event, Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onReady(String uuid) {
			mPd.dismiss();
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
    		mPageflip.toggleThumbnails();
    		break;

    	default:
    		break;

    	}
    	return super.onOptionsItemSelected(item);
    }
    
}
