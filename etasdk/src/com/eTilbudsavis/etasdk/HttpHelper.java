package com.eTilbudsavis.etasdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.os.AsyncTask;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Api.RequestListener;

public class HttpHelper extends AsyncTask<Void, Void, Void> {
	
	private int CONNECTION_TIME_OUT = 15 * 1000;
	private String mUrl;
	private List<NameValuePair> mQuery;
	private Bundle mHeaders;
	private Api.RequestType mRequestType;
	private RequestListener mRequestListener;
	
	private String mResult = "";
	private int mResponseCode;

	// Constructor for HttpHelper.
	public HttpHelper(String url, List<NameValuePair> query, Bundle headers, Api.RequestType requestType, RequestListener requestListener) {

		mUrl = url;
		mQuery = query;
		mHeaders = headers;
		mRequestType = requestType;
		mRequestListener = requestListener;
		
	}
	
	@Override
	protected Void doInBackground(Void... params) {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpParams, CONNECTION_TIME_OUT);
		
		Iterator<String> headerIterator = mHeaders.keySet().iterator();
		HttpResponse response = null;
		try {
			
			switch (mRequestType) {
			case POST:
				
				HttpPost post = new HttpPost(mUrl);
				if (mQuery.size() > 0)
					post.setEntity(new UrlEncodedFormEntity(mQuery, HTTP.UTF_8));
				
				while (headerIterator.hasNext()) {
					String s = headerIterator.next();
					post.setHeader(s, mHeaders.getString(s));
				}
				
				response = httpClient.execute(post);
				
				break;

			case GET:

				if (mQuery.size() > 0)
					mUrl = mUrl + "?" + URLEncodedUtils.format(mQuery, HTTP.UTF_8);

				HttpGet get = new HttpGet(mUrl);

				while (headerIterator.hasNext()) {
					String s = headerIterator.next();
					get.setHeader(s, mHeaders.getString(s));
				}
				
				response = httpClient.execute(get);
				break;
				
			case DELETE:
				break;
				
			case PUT:
				break;
				
			case HEAD:
				break;
				
			case OPTIONS:
				break;
				
			default:
				break;
			}
			
			/**
			 * Do not get content with this:
			 * EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			 * As this will make some very unfortunate line breaks in e.g. eta.dk/connect/ 
			 */
			mResponseCode = response.getStatusLine().getStatusCode();
			if (mResponseCode == HttpStatus.SC_OK) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			    StringBuilder sb = new StringBuilder();
			    String line = null;
			    try {
			        while ((line = reader.readLine()) != null)
			            sb.append(line);
			        
			    } catch (IOException e) {
			        e.printStackTrace();
			    } 
			    mResult = sb.toString();
			} else
				mResult = response.getStatusLine().getReasonPhrase();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

        // Close connection, to deallocate resources
		httpClient.getConnectionManager().shutdown();

		return null;
	}
	
	// Do callback in the UI thread
	@Override
	protected void onPostExecute(Void result) {
		mRequestListener.onComplete(mResponseCode, mResult);
    }
	
}