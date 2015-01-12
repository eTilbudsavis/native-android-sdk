package com.eTilbudsavis.etasdk.network.impl;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.RequestDebugger;

public class DefaultDebugger implements RequestDebugger {
	
	public static final String TAG = Eta.TAG_PREFIX + DefaultDebugger.class.getSimpleName();
	
	public void onFinish(Request<?> req) {
		EtaLog.d(TAG, req.getNetworkLog().toString());
		EtaLog.d(TAG, req.getLog().getString(getClass().getSimpleName()));
	}
	
	public void onDelivery(Request<?> r) {
		EtaLog.d(TAG, "TotalDuration: " + r.getLog().getTotalDuration());
	}
	
}
