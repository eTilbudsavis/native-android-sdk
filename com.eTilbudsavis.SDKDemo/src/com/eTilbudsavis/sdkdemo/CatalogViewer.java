package com.eTilbudsavis.sdkdemo;

import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Api.ListListener;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.PageflipWebview;
import com.eTilbudsavis.etasdk.PageflipWebview.PageflipListener;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;
import com.etilbudsavis.sdkdemo.R;

public class CatalogViewer extends Activity {

	public static final String TAG = "CatalogViewer";
	FrameLayout mPageflipContainer;
	PageflipWebview mPageflip;
	ProgressDialog mPd;
	boolean gotCatalogs = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.catalog_viewer);
        
        // Eta was 'set' once in the Main activity,  we do not need to do it again.
        
        // Notice this: We use a FrameLayout in the xml, and add a new pageflip view to it
        // in this way, we do not have to initialize the pageflip object again (slow process)
        mPageflipContainer = (FrameLayout) findViewById(R.id.pageflip);
		if (mPageflip == null) {
			mPageflip = new PageflipWebview(getApplicationContext());
			mPageflip.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
		
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	Eta.getInstance().onResume();
		mPageflipContainer.addView(mPageflip);
    	if (!gotCatalogs) {
        	mPd = ProgressDialog.show(CatalogViewer.this, "", "Getting catalogs...", true, true);
        	Eta.getInstance().getCatalogList(catalogListener).execute();
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	Eta.getInstance().onPause();
    	mPageflipContainer.removeAllViews();
    }
    
	// A catalogs listener, 
	ListListener<Catalog> catalogListener = new ListListener<Catalog>() {
		
		@Override
		public void onComplete(boolean isCache, int statusCode, List<Catalog> list, EtaError error) {

			mPd.dismiss();

			/* If the request is a success and one or more catalogs is returned,
			 * show the first catalog in a pageflip. */
			if (Utils.isSuccess(statusCode) && !list.isEmpty()) {
				
				gotCatalogs = true;
				mPd = ProgressDialog.show(CatalogViewer.this, "", "Loading catalog into pageflip...", true, true);
		        mPageflip.execute(Eta.getInstance(), pfl, list.get(0).getId());
		        
			} else {
				
				EtaLog.d(TAG, error.toString());
				
			}
		}
	};
	
	// Pageflip listener, triggered on callbacks from the pageflip.
    PageflipListener pfl = new PageflipListener() {

		@Override
		public void onReady(String uuid) {
			mPd.dismiss();
		}
		
		@Override
		public void onEvent(String event, String uuid, JSONObject object) {
			Toast.makeText(getApplicationContext(), event, Toast.LENGTH_SHORT).show();
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
