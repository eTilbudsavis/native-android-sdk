package com.etilbudsavis.etasdk;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.etilbudsavis.etasdk.API.RequestListener;
import com.etilbudsavis.etasdk.API.RequestType;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class HttpHelper extends AsyncTask<Void, Void, Void> {
	
	private String ETA_DOMAIN = "etilbudsavis.dk";
	private String mUrl;
	private List<NameValuePair> mQuery;
	private API.RequestType mRequestType;
	private RequestListener mRequestListener;
	private Context mContext;
	
	private String mResult = "";
	private Integer mResponseCode;

	// Constructor for HttpHelper.
	public HttpHelper(String url, List<NameValuePair> query, 
			API.RequestType requestType, RequestListener requestListener, Context context) {
		
		mUrl = url;
		mQuery = query;
		mRequestType = requestType;
		mRequestListener = requestListener;
		mContext = context;
	}
		
	@Override
	protected Void doInBackground(Void... params) {

		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		CookieSyncManager.createInstance(mContext);
		String[] keyValueSets = CookieManager.getInstance().getCookie("etilbudsavis.dk").split(";");
		for(String cookie : keyValueSets) {
			
		    String[] keyValue = cookie.split("=");
		    String value = ( keyValue.length > 1 ) ? keyValue[1] : "";
		    BasicClientCookie c = new BasicClientCookie(keyValue[0], value);
		    c.setDomain(ETA_DOMAIN);
		    httpClient.getCookieStore().addCookie(c);
		}
		
		HttpResponse response;
		try {
			if (mRequestType == RequestType.POST) {

				HttpPost post = new HttpPost(mUrl);
				if (mQuery.size() > 0)
					post.setEntity(new UrlEncodedFormEntity(mQuery, HTTP.UTF_8));
				
				response = httpClient.execute(post);
				
			} else {
				
				if (mQuery.size() > 0)
					mUrl = mUrl + "?" + URLEncodedUtils.format(mQuery, HTTP.UTF_8);
				
				HttpGet get = new HttpGet(mUrl);
				response = httpClient.execute(get);
			}

			mResponseCode = response.getStatusLine().getStatusCode();
			if (mResponseCode == HttpStatus.SC_OK)
				mResult = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			else
				mResult = response.getStatusLine().getReasonPhrase();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Add all cookies to global cookie store
		List<Cookie> cookies = httpClient.getCookieStore().getCookies();
		if(cookies != null) {
            for(Cookie cookie : cookies) {
                String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();                        
                CookieManager.getInstance().setCookie(cookie.getDomain(), cookieString);	                	
            }
        }
        CookieSyncManager.getInstance().sync();

        // Close connection, to deallocate resources
		httpClient.getConnectionManager().shutdown();

		return null;
	}
	
	// Do callback in the UI thread
	@Override
	protected void onPostExecute(Void result) {
		if (mResponseCode == HttpStatus.SC_OK) 
			mRequestListener.onSuccess(mResponseCode, mResult);
		else 
			mRequestListener.onError(mResponseCode, mResult);
    }
	
}