package com.eTilbudsavis.etasdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import Utils.Utilities;
import android.os.AsyncTask;

import com.eTilbudsavis.etasdk.Api.RequestListener;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;

public class HttpHelper extends AsyncTask<Void, Void, Void> {
	
	public static final String TAG = "HttpHelper";
	public static final boolean DEBUG = false;
	
	private int CONNECTION_TIME_OUT = 15 * 1000;
	private Eta mEta;
	private String mUrl;
	private List<NameValuePair> mQuery;
	private List<Header> mHeaders;
	private Api.RequestType mRequestType;
	private RequestListener mListener;
	
	private String mResponse = "";
	private Object mReturn = null;
	private int mResponseCode;

	// Constructor for HttpHelper.
	public HttpHelper(Eta eta, String url, List<Header> headers, List<NameValuePair> query , Api.RequestType requestType, RequestListener listener) {
		mEta = eta;
		mUrl = url;
		mQuery = query;
		mHeaders = headers;
		mRequestType = requestType;
		mListener = listener;
	}
	
	@Override
	protected Void doInBackground(Void... params) {

		// Print debug information
		if (DEBUG) {
			Utilities.logd(TAG, "Url: " + mUrl);
			Utilities.logd(TAG, "Headers: " + mHeaders.toString());
			StringBuilder sb = new StringBuilder();
			for (NameValuePair nvp : mQuery) {
				sb.append(nvp.getName()).append(": ").append(nvp.getValue()).append(", ");
			}
			Utilities.logd(TAG, "Query: " + sb.toString());
		}

		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		// Set the connection timeout
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpParams, CONNECTION_TIME_OUT);
		
		HttpResponse response = null;
		try {
			
			switch (mRequestType) {
			case POST:
				
				HttpPost post = new HttpPost(mUrl);
				if (mQuery.size() > 0)
					post.setEntity(new UrlEncodedFormEntity(mQuery, HTTP.UTF_8));

				for (Header h : mHeaders)
					post.setHeader(h);
				
				response = httpClient.execute(post);
				break;

			case GET:
				
				if (mQuery.size() > 0)
					mUrl = mUrl + "?" + URLEncodedUtils.format(mQuery, HTTP.UTF_8);

				HttpGet get = new HttpGet(mUrl);
				
				for (Header h : mHeaders)
					get.setHeader(h);
				
				response = httpClient.execute(get);
				break;
				
			case PUT:
				
				HttpPut put = new HttpPut(mUrl);
				if (mQuery.size() > 0)
					put.setEntity(new UrlEncodedFormEntity(mQuery, HTTP.UTF_8));
				
				for (Header h : mHeaders)
					put.setHeader(h);
				
				response = httpClient.execute(put);
				break;
				
			default:
				Utilities.logd(TAG, "RequestType " + mRequestType.toString() + " is not implemented yet, execution stopped!");
				return null;
			}
			
			mResponseCode = response.getStatusLine().getStatusCode();
			
		    mResponse = getText(response.getEntity().getContent());
		    
		    // If server returns OK
			if (200 <= mResponseCode && mResponseCode < 300) {
				// Try to cast object to match listener
				mReturn = mResponse;
			
			// If server returns ERROR
			} else if (400 <= mResponseCode && mResponseCode < 500) {
				
				try {
					mReturn = new EtaError(new JSONObject(mResponse));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			
			// If server is retarded
			} else {
				// Just return the standard response
				mReturn = response.getStatusLine().getReasonPhrase();
			}
			
			updateSessionInfo(response.getAllHeaders());

		    if (DEBUG) {
		    	StringBuilder headers = new StringBuilder();
		    	headers.append("Return Headers: ");
		    	for (Header h : response.getAllHeaders())
		    		headers.append("name: ").append(h.getName()).append(", value: ").append(h.getValue());
		    	
		    	Utilities.logd(TAG, headers.toString());
		    	Utilities.logd(TAG, "Code: " + String.valueOf(mResponseCode) + ", Data: " + mResponse);
		    }
		    
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Close connection, to deallocate resources
			httpClient.getConnectionManager().shutdown();
		}

		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		mListener.onComplete(mResponseCode, mReturn);
    }
	
	private static String getText(InputStream in) {
		
		// Do not get content with: EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
		// As this will make some very unfortunate line breaks in e.g. eta.dk/connect/ 
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		String line = "";
		try {
			while ((line = reader.readLine()) != null)
				sb.append(line);

		} catch (IOException e) {
			e.printStackTrace();
		}  finally {
			try {
				in.close();
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}

	private void updateSessionInfo(Header[] headers) {
		String token = "";
	    String tokenExp = "";
	    for (Header h : headers) {
	    	if (h.getName().equals("X-Token")) {
	    		token = h.getValue();
	    	} else if (h.getName().equals("X-Token-Expires")) {
	    		tokenExp = h.getValue();
	    	}
	    }
	    mEta.getSession().updateOnInvalidToken(token, tokenExp);
	}
	
}