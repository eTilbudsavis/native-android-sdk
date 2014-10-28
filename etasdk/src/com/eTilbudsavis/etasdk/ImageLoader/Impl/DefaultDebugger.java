package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import com.eTilbudsavis.etasdk.ImageLoader.ImageDebugger;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class DefaultDebugger implements ImageDebugger {
	
	public static final String TAG = DefaultDebugger.class.getSimpleName();
	
	public void debug(ImageRequest ir) {
		EtaLog.d(TAG, ir.getLog().getString(ir.getUrl()));
	}

}
