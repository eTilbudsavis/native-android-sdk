/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.Network.Impl;

import java.io.IOException;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpConnectionParams;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Network.HttpStack;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Utils.HeaderUtils;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class DefaultHttpNetwork implements HttpStack {

	public static final String TAG = Eta.TAG_PREFIX + DefaultHttpNetwork.class.getSimpleName();
	
	public HttpResponse performNetworking(Request<?> request) throws ClientProtocolException, IOException {
		
		// Start the interwebs work stuff
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		request.addEvent("set-apache-routeplanner");
		setHostNameVerifierAndRoutePlanner(httpClient);
		
		// Set timeouts
		request.addEvent(String.format("set-connection-timeout-%s", request.getTimeOut()*2));
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), request.getTimeOut());
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), request.getTimeOut());
		
		// Get the right request type, and set body if necessary
		HttpRequestBase httpRequest = createRequest(request);
		setHeaders(request, httpRequest);
		
		request.addEvent("performing-http-request");
		return httpClient.execute(httpRequest);
	}
	
	private void setHostNameVerifierAndRoutePlanner(DefaultHttpClient httpClient) {
		
		// Use custom HostVerifier to accept our wildcard SSL Certificates: *.etilbudsavis.dk
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", socketFactory, 443));
		SingleClientConnManager mgr = new SingleClientConnManager(httpClient.getParams(), registry);
		
		httpClient = new DefaultHttpClient(mgr, httpClient.getParams());
		
		// Change RoutePlanner to avoid SchemeRegistry causing IllegalStateException.
		// Some devices with faults in their default route planner
		httpClient.setRoutePlanner(new DefaultHttpRoutePlanner(registry));
		
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		
	}
	
	private HttpRequestBase createRequest(Request<?> request) {
		
		String url = Utils.buildQueryString(request);
		
		switch (request.getMethod()) {
		case POST: 
			HttpPost post = new HttpPost(url);
			setEntity(post, request);
			return post;

		case GET:
			HttpGet get = new HttpGet(url);
			return get;

		case PUT:
			HttpPut put = new HttpPut(url);
			setEntity(put, request);
			return put;

		case DELETE:
			HttpDelete delete = new HttpDelete(url);
			return delete;

		default:
			return null;
		}
		
	}
	
	private static void setEntity(HttpEntityEnclosingRequestBase httpRequest, Request<?> request) {
		byte[] body = request.getBody();
		if (body != null) {
			HttpEntity entity = new ByteArrayEntity(body);
			httpRequest.setEntity(entity);
			httpRequest.setHeader(HeaderUtils.CONTENT_TYPE, request.getBodyContentType());
		}
	}
	
	private void setHeaders(Request<?> request, HttpRequestBase http) {
		HashMap<String, String> headers = new HashMap<String, String>(request.getHeaders().size());
		headers.putAll(request.getHeaders());
		for(String key : headers.keySet())
			http.setHeader(key, headers.get(key));
	}

}
