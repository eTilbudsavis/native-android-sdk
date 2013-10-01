package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.EtaResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Network;
import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class HttpNetwork implements Network {
	
	public static final String TAG = "HttpNetwork";
	/**
	 * Default connection timeout, this is for both connection and socket
	 */
	private static final int CONNECTION_TIME_OUT = 10000;
	
	public NetworkResponse performRequest(Request<?> request) {
		

		// Start the interwebs work stuff
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		// Use custom HostVerifier to accept our wildcard SSL Certificates: *.etilbudsavis.dk
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", socketFactory, 443));
		SingleClientConnManager mgr = new SingleClientConnManager(httpClient.getParams(), registry);
		
		httpClient = new DefaultHttpClient(mgr, httpClient.getParams());
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), CONNECTION_TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), CONNECTION_TIME_OUT);
		
		// Change RoutePlanner to avoid SchemeRegistry causing IllegalStateException.
		// Some devices with faults in their default route planner
		httpClient.setRoutePlanner(new DefaultHttpRoutePlanner(registry));
		
		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		// End SSL Certificates hack
		
		HttpRequestBase httpRequest = null;
		EtaResponse response = new EtaResponse();
		try {
			
			// Execute the correct request type
			switch (request.getMethod()) {
			case Request.Method.POST: 
				HttpPost post = new HttpPost(request.getUrl());
//				post.setEntity(new UrlEncodedFormEntity(Utils.bundleToNameValuePair(request.), HTTP.UTF_8));
				httpRequest = post;
				break;
				
			case Request.Method.GET: 
				
//				if (mApiParams.size() > 0)
//					mPath = mPath + "?" + URLEncodedUtils.format(Utils.bundleToNameValuePair(mApiParams), HTTP.UTF_8);
				
				HttpGet get = new HttpGet(request.getUrl());
				httpRequest = get;
				break;
				
			case Request.Method.PUT: 
				HttpPut put = new HttpPut(request.getUrl()); 
//				put.setEntity(new UrlEncodedFormEntity(Utils.bundleToNameValuePair(mApiParams), HTTP.UTF_8));
//				request = put;
				break;
				
			case Request.Method.DELETE: 

//				if (mApiParams.size() > 0)
//					mPath = mPath + "?" + URLEncodedUtils.format(Utils.bundleToNameValuePair(mApiParams), HTTP.UTF_8);
				
				HttpDelete delete = new HttpDelete(request.getUrl());
				httpRequest = delete;
				break;
				
			default:
				Utils.logd(TAG, "Unknown RequestType: " + request.getMethod() + " - Aborting!");
				return null;
			}
			
			setHeaders(request, httpRequest);
			
			HttpResponse resp = httpClient.execute(httpRequest);
			
			if (resp.getStatusLine().getStatusCode() == 200) {
//				return new NetworkResponse(resp.getStatusLine().get, data, headers)
			} else {
//				error
			}
			
		} catch (UnknownHostException e) {
//			response.set(errorToString(EtaError.UNKNOWN_HOST, "UnknownHostException"));
			if (Eta.DEBUG) e.printStackTrace();
		} catch (ClientProtocolException e) {
//			response.set(errorToString(EtaError.CLIENT_PROTOCOL_EXCEPTION, "ClientProtocolException"));
			if (Eta.DEBUG) e.printStackTrace();
		} catch (IOException e) {
//			response.set(errorToString(EtaError.IO_EXCEPTION, "IOException"));
			if (Eta.DEBUG) e.printStackTrace();
		} finally {
			// Close connection, to deallocate resources
			httpClient.getConnectionManager().shutdown();
		}
		
		return null;
	}

	@SuppressWarnings("rawtypes")
	private void setBody(Request request, HttpRequestBase http) {
		
	}

	@SuppressWarnings("rawtypes")
	private void setHeaders(Request request, HttpRequestBase http) {
		
	}

}
