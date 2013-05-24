package com.eTilbudsavis.etasdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Utils.Utilities;
import android.os.AsyncTask;

import com.eTilbudsavis.etasdk.Api.RequestListener;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;

public class HttpHelper extends AsyncTask<Void, Void, Void> {
	
	public static final String TAG = "HttpHelper";
	private int CONNECTION_TIME_OUT = 15 * 1000;
	private Eta mEta;
	private String mUrl;
	private List<NameValuePair> mQuery;
	private List<Header> mHeaders;
	private Api.RequestType mRequestType;
	private RequestListener mRequestListener;
	
	private String mResponse = "";
	private Object mReturn = null;
	private int mResponseCode;

	// Constructor for HttpHelper.
	public HttpHelper(Eta eta, String url, List<NameValuePair> query, List<Header> headers, Api.RequestType requestType, RequestListener requestListener) {
		mEta = eta;
		mUrl = url;
		mQuery = query;
		mHeaders = headers;
		mRequestType = requestType;
		mRequestListener = requestListener;
	}
	
	@Override
	protected Void doInBackground(Void... params) {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		// Print debug information
		if (Eta.DEBUG) {
			Utilities.logd(TAG, "Url: " + mUrl);
			Utilities.logd(TAG, "Headers: " + mHeaders.toString());
			StringBuilder sb = new StringBuilder();
			for (NameValuePair nvp : mQuery) {
				sb.append(nvp.getName()).append(": ").append(nvp.getValue()).append(", ");
			}
			Utilities.logd(TAG, "Query: " + sb.toString());
		}
		
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpParams, CONNECTION_TIME_OUT);
		
		
		HttpResponse response = null;
		Header[] responseHeaders;
		try {
			
			switch (mRequestType) {
			case POST:
				
				HttpPost post = new HttpPost(mUrl);
				if (mQuery.size() > 0)
					post.setEntity(new UrlEncodedFormEntity(mQuery, HTTP.UTF_8));

				for (Header h : mHeaders)
					post.setHeader(h);
				
				response = httpClient.execute(post);
				responseHeaders = response.getAllHeaders();
				break;

			case GET:

				if (mQuery.size() > 0)
					mUrl = mUrl + "?" + URLEncodedUtils.format(mQuery, HTTP.UTF_8);

				HttpGet get = new HttpGet(mUrl);

				for (Header h : mHeaders)
					get.setHeader(h);
				
				response = httpClient.execute(get);
				responseHeaders = response.getAllHeaders();
				break;
				
			case DELETE:
				Utilities.logd(TAG, "RequestType DELETE is not implemented yet");
				break;
				
			case PUT:
				Utilities.logd(TAG, "RequestType PUT is not implemented yet");
				break;
				
			case HEAD:
				Utilities.logd(TAG, "RequestType HEAD is not implemented yet");
				break;
				
			case OPTIONS:
				Utilities.logd(TAG, "RequestType OPTIONS is not implemented yet");
				break;
				
			default:
				break;
			}
			
			/**
			 * Do not get content with: EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			 * As this will make some very unfortunate line breaks in e.g. eta.dk/connect/ 
			 */
			mResponseCode = response.getStatusLine().getStatusCode();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		    StringBuilder sb = new StringBuilder();
		    String line = null;
		    try {
		        while ((line = reader.readLine()) != null)
		            sb.append(line);
		        
		    } catch (IOException e) {
		        e.printStackTrace();
		    } 
		    mResponse = sb.toString();
		    
		    
			if (200 <= mResponseCode && mResponseCode < 300) {
				
				mReturn = convertResponse(mResponse);
				
			} else if (400 <= mResponseCode ) {
				
				try {
					JSONObject jObject = new JSONObject(mResponse);
					mEta.addError(new EtaError(jObject));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				mReturn = mResponse;
				
			} else {
				mReturn = response.getStatusLine().getReasonPhrase();
			}

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
		mRequestListener.onComplete(mResponseCode, mReturn);
    }
	
	private Object convertResponse(String resp) {
		try {
			if (mRequestListener instanceof Api.CatalogListListener) {
				ArrayList<Catalog> c = new ArrayList<Catalog>();
				JSONArray jArray = new JSONArray(resp);
				for (int i = 0 ; i < jArray.length() ; i++ ) {
					c.add(new Catalog((JSONObject)jArray.get(i)));
				}
				return c;
			} else if  (mRequestListener instanceof Api.DealerListListener) {
				ArrayList<Dealer> d = new ArrayList<Dealer>();
				JSONArray jArray = new JSONArray(resp);
				for (int i = 0 ; i < jArray.length() ; i++ ) {
					d.add(new Dealer((JSONObject)jArray.get(i)));
				}
				return d;
			} else if  (mRequestListener instanceof Api.OfferListListener) {
				ArrayList<Offer> o = new ArrayList<Offer>();
				JSONArray jArray = new JSONArray(resp);
				for (int i = 0 ; i < jArray.length() ; i++ ) {
					o.add(new Offer((JSONObject)jArray.get(i)));
				}
				return o;
			} else if  (mRequestListener instanceof Api.StoreListListener) {
				ArrayList<Offer> o = new ArrayList<Offer>();
				JSONArray jArray = new JSONArray(resp);
				for (int i = 0 ; i < jArray.length() ; i++ ) {
					o.add(new Offer((JSONObject)jArray.get(i)));
				}
				return o;
			} else if  (mRequestListener instanceof Api.CatalogListener) {
				return new Catalog(new JSONObject(resp));
			} else if  (mRequestListener instanceof Api.DealerListener) {
				return new Dealer(new JSONObject(resp));
			} else if  (mRequestListener instanceof Api.OfferListener) {
				return new Offer(new JSONObject(resp));
			} else if  (mRequestListener instanceof Api.StoreListener) {
				return new Store(new JSONObject(resp));
			} 
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return resp;
	}
	
}