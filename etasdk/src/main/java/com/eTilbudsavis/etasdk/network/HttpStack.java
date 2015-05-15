package com.eTilbudsavis.etasdk.network;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import java.io.IOException;

public interface HttpStack {
	
	public abstract HttpResponse performNetworking(Request<?> request) throws ClientProtocolException, IOException;
	
}
