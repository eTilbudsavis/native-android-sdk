package com.eTilbudsavis.etasdk.imageloader.impl;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.imageloader.ImageDebugger;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;
import com.eTilbudsavis.etasdk.log.EtaLog;

public class ImageRequestDebugger implements ImageDebugger {

    public static final String TAG = Constants.getTag(ImageRequestDebugger.class);

    public void debug(ImageRequest ir) {
        EtaLog.d(TAG, ir.getLog().getString(ir.getUrl()));
    }

}
