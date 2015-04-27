package com.eTilbudsavis.etasdk.network;

public interface RequestDebugger {

	public void onFinish(Request<?> r);

	public void onDelivery(Request<?> r);
	
}
