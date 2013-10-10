package com.eTilbudsavis.sdkdemo;

import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Api.ListListener;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Pageflip;
import com.eTilbudsavis.etasdk.Pageflip.PageflipListener;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.Utils.Utils;
import com.etilbudsavis.sdkdemo.R;

public class CatalogViewer extends Activity {

	public static final String TAG = "CatalogViewer";
	Pageflip mPageflip;
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
        
		mPageflip = (Pageflip)findViewById(R.id.pageflip);
		/* The view session hack, to fix a problem of redrawing the WebView
		 * in a Fragment, that has had a onDestroyView() */
		mPageflip.setViewSession(mViewSession);
		
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Eta.getInstance().onResume();
    	mPd = ProgressDialog.show(CatalogViewer.this, "", "Getting catalogs...", true, true);
    	Eta.getInstance().getCatalogList(catalogListener).execute();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	Eta.getInstance().onPause();
    }
    
	// A catalogs listener, 
	ListListener<Catalog> catalogListener = new ListListener<Catalog>() {
		
		@Override
		public void onComplete(boolean isCache, int statusCode, List<Catalog> list, EtaError error) {

			mPd.dismiss();

			/* If the request is a success and one or more catalogs is returned,
			 * show the first catalog in a pageflip. */
			if (Utils.isSuccess(statusCode) && !list.isEmpty()) {
				
				mPd = ProgressDialog.show(CatalogViewer.this, "", "Loading catalog into pageflip...", true, true);
				
		        mPageflip.execute(Eta.getInstance(), pfl, list.get(0).getId());
		        
			} else {
				
				Utils.logd(TAG, error.toString());
				
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
		public void onReady(String uuid, String viewSession) {
			// Remember to set the viewSession variable, first chance you get.
			mViewSession = viewSession;
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
