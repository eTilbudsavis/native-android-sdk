package com.eTilbudsavis.etasdk.network.impl;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.network.RequestDebugger;

public class NetworkDebugger implements RequestDebugger{
	
	public static final String TAG = Eta.TAG_PREFIX + NetworkDebugger.class.getSimpleName();
	
	public void onFinish(Request<?> r) {
		EtaLog.d(TAG, r.getNetworkLog().toString());
	}

	public void onDelivery(Request<?> r) {
		EtaLog.d(TAG, "TotalDuration: " + r.getLog().getTotalDuration());
	}
	
}
