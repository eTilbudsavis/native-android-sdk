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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import com.eTilbudsavis.etasdk.Tools.Utilities;

import android.os.AsyncTask;

public class HttpHelper extends AsyncTask<Void, Void, Void> {
	
	public static final String TAG = "HttpHelper";
	
	private int CONNECTION_TIME_OUT = 15 * 1000;
	private boolean mDebug = false;
	private Eta mEta;
	private String mUrl;
	private List<NameValuePair> mQuery;
	private List<Header> mHeaders;
	private Api.RequestType mRequestType;
	private HttpListener mListener;
	
	private String mResponse = "";
	private int mResponseCode;
	private Header[] mResponsHeaders;

	// Constructor for HttpHelper.
	public HttpHelper(Eta eta, String url, List<Header> headers, List<NameValuePair> query , Api.RequestType requestType, HttpListener listener) {
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
		if (mDebug) {
			Utilities.logd(TAG, "---- Pre Execute ---- " + getClass().getSimpleName() + '@' + Integer.toHexString(hashCode()));
			Utilities.logd(TAG, "Url: " + mUrl);
			Utilities.logd(TAG, "Type: " + mRequestType.toString());
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

			case DELETE:
				
				if (mQuery.size() > 0)
					mUrl = mUrl + "?" + URLEncodedUtils.format(mQuery, HTTP.UTF_8);

				HttpDelete del = new HttpDelete(mUrl);
				
				for (Header h : mHeaders)
					del.setHeader(h);
				
				response = httpClient.execute(del);
				break;
				
			default:
				Utilities.logd(TAG, "RequestType " + mRequestType.toString() + " is not implemented yet, execution stopped!");
				return null;
			}
			
			mResponseCode = response.getStatusLine().getStatusCode();
			
		    mResponse = getText(response.getEntity().getContent());
		    
			mResponsHeaders = response.getAllHeaders();

			updateSessionInfo(mResponsHeaders);
			
		    if (mDebug) {
				Utilities.logd(TAG, "---- Post Execute ---- " + getClass().getSimpleName() + '@' + Integer.toHexString(hashCode()));
		    	StringBuilder headers = new StringBuilder();
		    	headers.append("Headers: [");
		    	for (Header h : response.getAllHeaders())
		    		headers.append(h.getName()).append(": ").append(h.getValue()).append(", ");
		    	
		    	Utilities.logd(TAG, headers.append("]").toString());
		    	Utilities.logd(TAG, "StatusCode: " + String.valueOf(mResponseCode));
		    	Utilities.logd(TAG, "Object: " + mResponse);
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
		mListener.onComplete(mResponseCode, mResponse, mResponsHeaders);
    }

	/**
	 * Method checks headers to find X-Token and X-Token-Expires.<br>
	 * If they do not exist, nothing happens as the call has a wrong endpoint, or other
	 * non-API regarding error. If they do exist, then they are checked by the Session
	 * to find out if there are any changes.
	 * @param headers to check for new token.
	 */
	private void updateSessionInfo(Header[] headers) {
		String token = "";
	    String expire = "";
	    for (Header h : headers) {
	    	if (h.getName().equals("X-Token")) {
	    		token = h.getValue();
	    	} else if (h.getName().equals("X-Token-Expires")) {
	    		expire = h.getValue();
	    	}
	    }
	    if (token.equals("") || expire.equals(""))
	    	return;
	    
	    mEta.getSession().update(token, expire);
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

	public HttpHelper debug(boolean useDebug) {
		mDebug = useDebug;
		return this;
	}
	
	public interface HttpListener {
		public void onComplete(int statusCode, String data, Header[] headers);
	}
	
}