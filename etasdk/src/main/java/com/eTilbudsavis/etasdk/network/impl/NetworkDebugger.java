package com.eTilbudsavis.etasdk.network.impl;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.RequestDebugger;

public class NetworkDebugger implements RequestDebugger {
	
	public static final String TAG = Constants.getTag(NetworkDebugger.class);
	
	public void onFinish(Request<?> req) {
		EtaLog.d(TAG, req.getNetworkLog().toString());
		EtaLog.d(TAG, req.getLog().getString(getClass().getSimpleName()));
	}
	
	public void onDelivery(Request<?> r) {
		EtaLog.d(TAG, "TotalDuration: " + r.getLog().getTotalDuration());
	}
	
}
