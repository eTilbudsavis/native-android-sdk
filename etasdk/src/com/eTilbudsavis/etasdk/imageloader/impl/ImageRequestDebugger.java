package com.eTilbudsavis.etasdk.imageloader.impl;

import com.eTilbudsavis.etasdk.imageloader.ImageDebugger;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;
import com.eTilbudsavis.etasdk.log.EtaLog;

public class ImageRequestDebugger implements ImageDebugger {
	
	public static final String TAG = ImageRequestDebugger.class.getSimpleName();
	
	public void debug(ImageRequest ir) {
		EtaLog.d(TAG, ir.getLog().getString(ir.getUrl()));
	}

}
