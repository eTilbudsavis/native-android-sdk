package com.etilbudsavis.etasdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.etilbudsavis.etasdk.API.RequestListener;
import com.etilbudsavis.etasdk.API.RequestType;

public class HttpHelper extends AsyncTask<Void, Void, Void> {
	
	private String ETA_DOMAIN = "etilbudsavis.dk";
	private int CONNECTION_TIME_OUT = 15 * 1000;
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
		
		// Hack to force acceptance of our SSL Certificates to *.etilbudsavis.dk
		// It basically uses a different HostnameVerifier than the default
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		DefaultHttpClient tmpClient = new DefaultHttpClient();
		
		SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
		socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);

		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("https", socketFactory, 443));
		
		SingleClientConnManager mgr = new SingleClientConnManager(tmpClient.getParams(), registry);
		DefaultHttpClient httpClient = new DefaultHttpClient(mgr, tmpClient.getParams());

		HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		// End SSL Certificates hack
		
		
		// Set connection timeouts
		HttpParams httpParams = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIME_OUT);
		HttpConnectionParams.setSoTimeout(httpParams, CONNECTION_TIME_OUT);
		
		// An cookie exception... of course
		if (!mUrl.equals("https://etilbudsavis.dk/ajax/user/reset/")) {
			CookieSyncManager.createInstance(mContext);
			String cString = CookieManager.getInstance().getCookie("etilbudsavis.dk");
			if (cString != null) {
				String[] keyValueSets = cString.split(";");
				for(String cookie : keyValueSets) {
				    String[] keyValue = cookie.split("=");
				    String value = ( keyValue.length > 1 ) ? keyValue[1] : "";
				    BasicClientCookie c = new BasicClientCookie(keyValue[0], value);
				    c.setDomain(ETA_DOMAIN);
				    httpClient.getCookieStore().addCookie(c);
				}
			}
		}
		
		HttpResponse response;
		
		boolean showDebug = false;
		
//		showDebug = mUrl.equals("https://etilbudsavis.dk/api/v1/offer/search/");
//		
//		showDebug = showDebug 
//				|| (!mUrl.equals("https://etilbudsavis.dk/api/v1/shoppinglist/sync/")
//				&& !mUrl.equals("https://etilbudsavis.dk/api/v1/shoppinglist/list/"));
		
		try {
			
			if (showDebug) {
				Utilities.logd("HttpHelper", "URL: " + mUrl);
				Utilities.logd("HttpHelper", "Query: " + URLEncodedUtils.format(mQuery, HTTP.UTF_8));
			}
			
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
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Add all cookies to global cookie store
		List<Cookie> cookies = httpClient.getCookieStore().getCookies();
		if(cookies != null) {
			// If it's a sign out request, we must discard all AUTH cookies
			if (mUrl.equals("https://etilbudsavis.dk/api/v1/user/signout/")) {
	            for(Cookie cookie : cookies) {
	            	if (!cookie.getName().contains("auth[")) {
		                String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
		                CookieManager.getInstance().setCookie(cookie.getDomain(), cookieString);
	            	}
	            }
            } else {
            	for(Cookie cookie : cookies) {
	                String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
	                CookieManager.getInstance().setCookie(cookie.getDomain(), cookieString);
	            }
            }
            
        }
        CookieSyncManager.getInstance().sync();

        // Close connection, to deallocate resources
		httpClient.getConnectionManager().shutdown();

		if (showDebug) {
			Utilities.logd("HttpHelper", String.valueOf(mResponseCode));
			Utilities.logd("HttpHelper", mResult);
		}
		
		return null;
	}
	
	// Do callback in the UI thread
	@Override
	protected void onPostExecute(Void result) {
		if (mResponseCode != null) {
			if (mResponseCode == HttpStatus.SC_OK) 
				mRequestListener.onSuccess(mResponseCode, mResult);
			else 
				mRequestListener.onError(mResponseCode, mResult);
		} else {
			mRequestListener.onError(0, "No Internet");
		}
    }
	
}