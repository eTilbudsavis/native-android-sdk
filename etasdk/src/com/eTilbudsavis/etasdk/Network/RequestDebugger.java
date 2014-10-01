package com.eTilbudsavis.etasdk.Network;

public interface RequestDebugger {

	public void onFinish(Request<?> r);

	public void onDelivery(Request<?> r);
	
}
