package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
import org.apache.http.util.ByteArrayBuffer;

import com.eTilbudsavis.etasdk.NetworkInterface.Network;
import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class HttpNetwork implements Network {

	public static final String TAG = "HttpNetwork";
	
	/**
	 * Default connection timeout, this is for both connection and socket
	 */
	private static final int CONNECTION_TIME_OUT = 10000;
	private static final int BUFFER_SIZE = 0x1000; // 4K
	
	public NetworkResponse performRequest(Request<?> request) throws EtaError {
		
		HttpResponse resp = null;
		int sc = 0;
		byte[] content = null;
		Map<String, String> responseHeaders = new HashMap<String, String>();
		try {
			
			resp = performHttpRequest(request);
			
			sc = resp.getStatusLine().getStatusCode();
			
			if (resp.getEntity() == null) {
				// add 0-byte for to mock no-content
				content = new byte[0];
			} else {
				request.addEvent("reading-input");
				content = entityToBytes(resp.getEntity());
			}
			
			for (org.apache.http.Header h : resp.getAllHeaders()) {
				responseHeaders.put(h.getName(), h.getValue());
			}
			
			NetworkResponse r = new NetworkResponse(resp.getStatusLine().getStatusCode(), content, responseHeaders);
			
			return r;
			
		} catch (Exception e) {
			EtaLog.d(TAG, e);
			throw new NetworkError(e);
		}
		
	}

	private HttpResponse performHttpRequest(Request<?> request) throws ClientProtocolException, IOException {
		
		// Start the interwebs work stuff
		DefaultHttpClient httpClient = new DefaultHttpClient();

		request.addEvent("set-apache-routeplanner");
		setHostNameVerifierAndRoutePlanner(httpClient);

		// Set timeouts
		request.addEvent(String.format("set-connection-timeout-%s", CONNECTION_TIME_OUT*2));
		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), CONNECTION_TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), CONNECTION_TIME_OUT);
		
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
		
		String url = buildUrl(request);
		
		switch (request.getMethod()) {
		case Request.Method.POST: 
			HttpPost post = new HttpPost(url);
			setEntity(post, request);
			return post;

		case Request.Method.GET:
			HttpGet get = new HttpGet(url);
			return get;

		case Request.Method.PUT:
			HttpPut put = new HttpPut(url);
			setEntity(put, request);
			return put;

		case Request.Method.DELETE:
			HttpDelete delete = new HttpDelete(url);
			return delete;

		default:
			return null;
		}
		
	}
	
	private String buildUrl(Request<?> r) {
		return r.getQueryParameters().isEmpty() ? r.getUrl() : r.getUrl() + "?" + Utils.bundleToQueryString(r.getQueryParameters());
	}
	
	private static void setEntity(HttpEntityEnclosingRequestBase httpRequest, Request<?> request) {
		byte[] body = request.getBody();
		if (body != null) {
			HttpEntity entity = new ByteArrayEntity(body);
			httpRequest.setEntity(entity);
			httpRequest.setHeader(Request.Header.CONTENT_TYPE, request.getBodyContentType());
		}
	}
	
	private void setHeaders(Request<?> request, HttpRequestBase http) {
		HashMap<String, String> headers = new HashMap<String, String>(request.getHeaders().size());
		headers.putAll(request.getHeaders());
		for(String key : headers.keySet())
			http.setHeader(key, headers.get(key));
	}
	
	private static byte[] entityToBytes(HttpEntity entity) throws IllegalStateException, IOException {
		
		// Find best buffer size
		int init_buf = 0 <= entity.getContentLength() ? (int)entity.getContentLength() : BUFFER_SIZE;
		
		ByteArrayBuffer bytes = new ByteArrayBuffer(init_buf);
			
		InputStream is = entity.getContent();
		if (is == null)
			return bytes.toByteArray();
		
		byte[] buf = new byte[init_buf];
		int c = -1;
		while (( c = is.read(buf)) != -1) {
			bytes.append(buf, 0, c);
		}
		
		return bytes.toByteArray();
	}
	
}
