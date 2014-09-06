package com.eTilbudsavis.etasdk.Network.Impl;

import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.RequestDebugger;

public class NetworkDebugger implements RequestDebugger{
	
	public String TAG = NetworkDebugger.class.getSimpleName();
	
	public void debug(Request<?> r) {
		EtaLog.d(TAG, r.getNetworkLog().toString());
	}
	
}
