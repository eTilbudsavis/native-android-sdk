package com.eTilbudsavis.etasdk.Network;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

public interface HttpStack {
	
	public abstract HttpResponse performNetworking(Request<?> request) throws ClientProtocolException, IOException;
	
}
