/**
 * <p>
 * This class represents the base for building requests to the API.
 * The class has the 4 basic methods for initializing requests in an convenient
 * way, these corresponds to the HTTP request types:</p>
 * <ul>
 * 	<li> {@link #get get} for getting information to server
 * </ul>
 * 
 * @author			Danny Hvam <danny@etilbudsavis.dk>
 */
package com.eTilbudsavis.etasdk;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.eTilbudsavis.etasdk.Api.RequestType;
import com.eTilbudsavis.etasdk.SessionManager.OnSessionChangeListener;
import com.eTilbudsavis.etasdk.EtaObjects.EtaErnObject;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.Network.EtaResponse;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Params;
import com.eTilbudsavis.etasdk.Utils.Sort;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Api implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "Api";
	
	/**  The default offset for API calls  */
	public static final int DEFAULT_OFFSET = 0;

	/**  The default limit for API calls. 
	 * Note that this number is best for performance on the server. */
	public static final int DEFAULT_LIMIT = 25;

	/**  Use this flag to enable and disable the usage of location, in the API call  */
	public static final int FLAG_USE_LOCATION			= 1;

	/**  Use this flag to enable and disable the usage of cache, in the API call  */
	public static final int FLAG_USE_CACHE				= 1 << 1;

	/**  Use this flag to enable and disable the usage of location, in the API call  */
	public static final int FLAG_PRINT_DEBUG				= 1 << 2;

	/**  Use this flag to enable and disable the usage of location, in the API call  */
	public static final int FLAG_CANCEL_IF_POSSIBLE	= 1 << 3;

	/**  Use this flag to enable and disable the usage of multiple callbacks, in the API call.
	 * use this if you want to use cache hits only  */
	public static final int FLAG_ONLY_RETURN_CACHE	= 1 << 4;

	private static final int FLAG_SESSION_REFRESH	= 1 << 25;

	private static final int FLAG_CACHE_HIT			= 1 << 26;
	
	/** Determins if the request is finished */
	private boolean mFinished = false;
	
	/** Type of HTTP request. */
	public enum RequestType { POST, GET, PUT, DELETE }
	
	/** Content-Type used in request. */
	public enum ContentType {
		JSON			{ public String toString() { return "application/json; charset=utf-8"; } },
		URLENCODED		{ public String toString() { return "application/x-www-form-urlencoded; charset=utf-8"; } },
//		FORMDATA		{ public String toString() { return "multipart/form-data; charset=utf-8"; } }
	}

	private enum ListenerType { ITEM, LIST, OBJECT, ARRAY, STRING }
	
	private Eta mEta;
	private static final int CONNECTION_TIME_OUT = 10000;
	private ApiListener<?> mListener = null;
	private String mPath = null;
	private Bundle mApiParams = null;
	private RequestType mRequestType = null;
	private ContentType mContentType = null;
	private List<Header> mHeaders;
	private String mId = null;
	private int mFlags;
	private Handler mHandler;
	private int mRetryCount = 0;
	private JSONObject mBody = null;
	
	private StringBuilder debug = new StringBuilder();
	
	/**
	 * Default constructor for API
	 * @param Eta object with relevant information e.g. location
	 */
	public Api(Eta eta) {
		mEta = eta;
		// set default flags
		mFlags = FLAG_USE_LOCATION | FLAG_USE_CACHE ;
	}
	
	public Api setBody(JSONObject data) {
		mBody = data;
		return this;
	}
	
	/**
	 * Set a handler, for the return, this way you can have the SDK return results on any thread.
	 * @param handler to return callbacks
	 * @return this Object
	 */
	public Api setHandler(Handler handler) {
		mHandler = handler;
		return this;
	}
	
	/**
	 * Set various options for this API call.
	 * All flags are defined with a prefix "FLAG_", so to enable debugging output, just:
	 * {@link #setFlag(int...) enableFlag(Api.FLAG_DEBUG, Api.CANCEL)} 
	 * @param flags to enable
	 * @return this API object
	 */
	public Api setFlag(int... flags) {
		for (int i = 0; i < flags.length ; i++)
			mFlags |= flags[i];
		return this;
	}

	/**
	 * Set various options for this API call.
	 * All flags are defined with a prefix "FLAG_", so to enable debugging output, just:
	 * {@link #setFlag(int) enableFlag(Api.FLAG_DEBUG)} 
	 * @param flag to enable
	 * @return this API object
	 */
	public Api setFlag(int mask) {
		mFlags |= mask;
		return this;
	}
	
	/**
	 * Ask if a specific flag is set.<br />
	 * {@link #isFlag(int) isFlag(Api.DEBUG)} will tell you if debugging is enabled
	 * @param flag to query for
	 * @return
	 */
	public boolean isFlag(int flag) {
		return (mFlags & flag) == flag;
	}
	
	/**
	 * Disable flags previously set.
	 * @param flag to disable
	 * @return
	 */
	public Api removeFlag(int flag) {
		mFlags = mFlags & ~flag;
		return this;
	}
	
	/**
	 * Set an option to have debug info printed to LogCat
	 * @param print true if debug should print
	 * @return this object
	 */
	public Api printDebug(boolean print) {
		if (print)
			setFlag(FLAG_PRINT_DEBUG);
		else
			removeFlag(FLAG_PRINT_DEBUG);
		return this;
	}
	
	/**
	 * TODO: Write proper JavaDoc<br>
	 * <code>new String[] {Api.SORT_DISTANCE, Api.SORT_PUBLISHED}</code>
	 * @param order
	 * @return This {@link com.eTilbudsavis.etasdk.Api Api} object to allow for chaining of calls to set methods
	 */
	public Api setOrderBy(String order) {
		mApiParams.putString(Request.Sort.ORDER_BY, order);
		return this;
	}

	public Api setOrderBy(List<String> order) {
		String tmp = TextUtils.join(",",order);
		mApiParams.putString(Request.Sort.ORDER_BY, tmp);
		return this;
	}
	
	public String getOrderBy() {
		return mApiParams.getString(Request.Sort.ORDER_BY);
	}

	public Api setOffset(int offset) {
		mApiParams.putInt(Request.Param.OFFSET, offset);
		return this;
	}
	
	public int getOffset() {
		return mApiParams.getInt(Request.Param.OFFSET);
	}

	public Api setLimit(int limit) {
		mApiParams.putInt(Request.Param.LIMIT, limit);
		return this;
	}
	
	public int getLimit() {
		return mApiParams.getInt(Request.Param.LIMIT);
	}
	
	/**
	 * Sets a list of id's to filter result by.
	 * @param id's to filter by
	 * @return this object
	 */
	public Api setCatalogIds(Set<String> ids) {
		applyFilter(Request.Param.FILTER_CATALOG_IDS, ids);
		return this;
	}

	/**
	 * Returns a list of id's that this {@link com.eTilbudsavis.etasdk.Api Api} will filter results by.
	 * @return a list if id's
	 */
	public Set<String> getCatalogIds() {
		return getFilter(Request.Param.FILTER_CATALOG_IDS);
	}

	/**
	 * Sets a list of id's to filter result by.
	 * @param id's to filter by
	 * @return this object
	 */
	public Api setDealerIds(Set<String> ids) {
		applyFilter(Request.Param.FILTER_DEALER_IDS, ids);
		return this;
	}

	/**
	 * Returns a list of id's that this {@link com.eTilbudsavis.etasdk.Api Api} will filter results by.
	 * @return a list if id's
	 */
	public Set<String> getDealerIds() {
		return getFilter(Request.Param.FILTER_DEALER_IDS);
	}

	/**
	 * Sets a list of id's to filter result by.
	 * @param id's to filter by
	 * @return this object
	 */
	public Api setStoreIds(Set<String> ids) {
		applyFilter(Request.Param.FILTER_STORE_IDS, ids);
		return this;
	}

	/**
	 * Returns a list of id's that this {@link com.eTilbudsavis.etasdk.Api Api} will filter results by.
	 * @return a list if id's
	 */
	public Set<String> getStoreIds() {
		return getFilter(Request.Param.FILTER_STORE_IDS);
	}

	/**
	 * Sets a list of id's to filter result by.
	 * @param id's to filter by
	 * @return this object
	 */
	public Api setOfferIds(Set<String> ids) {
		applyFilter(Request.Param.FILTER_OFFER_IDS, ids);
		return this;
	}

	/**
	 * Returns a list of id's that this {@link com.eTilbudsavis.etasdk.Api Api} will filter results by.
	 * @return a list if id's
	 */
	public Set<String> getOfferIds() {
		return getFilter(Request.Param.FILTER_OFFER_IDS);
	}

	/**
	 * Sets a list of id's to filter result by.
	 * @param ids to filter by
	 * @return this object
	 */
	public Api setAreaIds(Set<String> ids) {
		applyFilter(Request.Param.FILTER_AREA_IDS, ids);
		return this;
	}

	/**
	 * Returns a list of id's that this {@link com.eTilbudsavis.etasdk.Api Api} will filter results by.
	 * @return a list if id's
	 */
	public Set<String> getAreaIds() {
		return getFilter(Request.Param.FILTER_AREA_IDS);
	}
	
	/**
	 * Set a parameter for what specific id's to get from a given endpoint.<br><br>
	 * 
	 * E.g.: setIds(Catalog.PARAM_IDS, new String[]{"eecdA5g","b4Aea5h"});
	 * @param	filterName of the endpoint parameter e.g. Catalog.PARAM_IDS
	 * @param	ids to filter by
	 * @return	this object
	 */
	public Api applyFilter(String filterName, Set<String> ids) {
		String idList = TextUtils.join(",",ids);
		mApiParams.putString(filterName, idList);
		return this;
	}
	
	public Api removeFilter(String filterName) {
		mApiParams.remove(filterName);
		return this;
	}
	
	/**
	 * Returns a list of id's that this {@link com.eTilbudsavis.etasdk.Api Api} will filter results by.
	 * @param	filterName 
	 * @return	a list if id's
	 */
	public Set<String> getFilter(String filterName) {
		// I've used the same logic in the EtaCache, please remember to change
		
		String tmp = mApiParams.getString(filterName);
		Set<String> list = new HashSet<String>();
		Collections.addAll(list, TextUtils.split(tmp, ","));
		return list;
	}
	
	/**
	 * Set a parameter for what specific id to get from a given endpoint.<br><br>
	 * 
	 * E.g.: setIds(Catalog.PARAM_IDS, "b4Aea5h");
	 * @param id to filter by
	 * @return this object
	 */
	public Api setId(String id) {
		mId = id;
		return this;
	}
	
	public String getId() {
		return mId;
	}
	
	public Api setContentType(ContentType type) {
		mContentType = type;
		return this;
	}

	public ContentType getContentType() {
		return mContentType;
	}

	public Api setRequestType(RequestType type) {
		mRequestType = type;
		return this;
	}

	public RequestType getRequestType() {
		return mRequestType;
	}

	public Bundle getApiParameters() {
		return mApiParams;
	}
	
	public Api addApiParameters(Bundle params) {
		mApiParams.putAll(params);
		return this;
	}
	
	public Api setApiParameters(Bundle params) {
		mApiParams = params;
		return this;
	}

	public String getUrl() {
		return mPath;
	}

	public ApiListener<?> getListener() {
		return mListener;
	}

	public Api setListener(ApiListener<?> listener) {
		mListener = listener;
		return this;
	}
	
	/**
	 * Set any headers that you want in this API call. This should not generally
	 * be used, as the SDK handles all headers.
	 * @param name the name of the header.
	 * @param value the value of the header.
	 * @return This {@link com.eTilbudsavis.etasdk.Api Api} object to allow for chaining of calls to set methods
	 */
	public Api setHeader(String name, String value) {
		mHeaders.add(new BasicHeader(name, value));
		return this;
	}
	
	/**
	 * Get a list of the headers that this API call will send to server.<br>
	 * <b>Note</b> that X-Token a X-Signature is not included in this bundle.
	 * @return List of headers.
	 */
	public List<Header> getHeaders() {
		return mHeaders;
	}
	
	/**
	 * Attempts to cancel execution of this task.<br><br>
	 * This is just as cancelable as any other thread. (no guarantees)
	 * @param cancleIfPossible
	 * @return this object
	 */
	public synchronized Api cancel(boolean cancleIfPossible) {
		if (cancleIfPossible) {
			setFlag(FLAG_CANCEL_IF_POSSIBLE);
		} else {
			removeFlag(FLAG_CANCEL_IF_POSSIBLE);
		}
		return this;
	}
	
	public boolean isFinished() {
		return mFinished;
	}
	
	public Api search(String url, ListListener<?> listener, String query) {
		if (!url.contains("/search"))
			EtaLog.d(TAG, "url does not match a search endpoint, don't expect anything good...");
		
		Bundle apiParams = new Bundle();
		apiParams.putString(Request.Param.QUERY, query);
		return request(url, listener, apiParams, RequestType.GET, ContentType.URLENCODED, null);
	}
	
	public Api get(String url, ApiListener<?> listener) {
		return request(url, listener, null, RequestType.GET, ContentType.URLENCODED, null);
	}

	public Api get(String url, ApiListener<?> listener, Bundle apiParams) {
		return request(url, listener, apiParams, RequestType.GET, ContentType.URLENCODED, null);
	}

	public Api post(String url, ApiListener<?> listener, Bundle apiParams) {
		return request(url, listener, apiParams, RequestType.POST, ContentType.URLENCODED, null);
	}

	public Api delete(String url, ApiListener<?> listener, Bundle apiParams) {
		return request(url, listener, apiParams, RequestType.DELETE, ContentType.URLENCODED, null);
	}

	public Api put(String url, ApiListener<?> listener, Bundle apiParams) {
		return request(url, listener, apiParams, RequestType.PUT, ContentType.URLENCODED, null);
	}
	
	public Api request(String url, ApiListener<?> listener, Bundle apiParams, RequestType requestType, ContentType contentType, List<Header> headers) {
		if (url == null || 
			listener == null || 
			requestType == null ) {
			EtaLog.d(TAG, "Api parameters error: url, callback interface and requestType must not be null");
			return null;
		}
		mPath = url;
		mListener = listener;
		mApiParams = apiParams == null ? new Bundle() : apiParams;
		mRequestType = requestType;
		mContentType = contentType == null ? ContentType.URLENCODED : contentType;
		mHeaders = headers == null ? new ArrayList<Header>(3) : headers;
		return this;
	}

	/**
	 * This will start executing the request.
	 * Note that if the {@link #request(String, CallbackJSON, Bundle, RequestType, Bundle) request()}'s 
	 * optionalKeys bundle contains options that have also been set by
	 * any of the Api-setters, then the setters will be used.
	 * @return HttpHelper, so execution of background task can be cancelled. <br>
	 * <b>Note</b> HttpHelper is <code>null</code> if there is no valid session. In this case
	 * {@link #execute() execute()} will try to get a valid session and then instantiate HttpHelper, here after the previous call is continued.
	 */
	public Api execute() {
		
		// Check if all variables needed are okay
		if (
				mPath == null ||
				mListener == null || 
				mApiParams == null || 
				mRequestType == null || 
				mHeaders == null) {
			
			EtaLog.d(TAG, "A request() must be made before execute()");
			return null;
		}
		
		// Append HOST if needed
		if (!mPath.matches("^http.*")) {
			mPath = Request.Endpoint.getHost() + mPath;
		}
		
		if (mId != null) {
			mPath += mId;
		}
		
		// Give the request to SessionManager
		mEta.getSessionManager().performRequest(Api.this);
		return this;
	}
	
	public void runThread() {
		mRetryCount++;
		mEta.getThreadPool().execute(worker);
	}
	
	Runnable worker = new Runnable() {
		
		public void run() {
			
			// Required API key.
			if (mRequestType == RequestType.POST && mPath.contains(Request.Endpoint.SESSIONS)) {
				
				mApiParams.putString(Request.Param.API_KEY, mEta.getApiKey());
				
			} else {
				
				String token = mEta.getSessionManager().getSession().getToken();
				setHeader(Request.Header.X_TOKEN, token);
				String sha256 = Utils.generateSHA256(mEta.getApiSecret() + token);
				setHeader(Request.Header.X_SIGNATURE, sha256);
				
			}
			
			// Add location
			if (isFlag(FLAG_USE_LOCATION) && mEta.getLocation().isSet()) {
				mApiParams.putAll(mEta.getLocation().getQuery());
			}
			
			if (mBody != null || mContentType != ContentType.JSON) {
				setHeader(Request.Header.CONTENT_TYPE, mContentType.toString());
			}
			
			if (isFlag(FLAG_USE_CACHE) && mRequestType == RequestType.GET) {

				EtaResponse r = mEta.getCache().get(mPath, mApiParams);
				
				if (r != null) {
					setFlag(FLAG_CACHE_HIT);
					convert(true, r);
				}

			}
			
			if (!mEta.isOnline()) {
				EtaError e = new EtaError();
				e.setCode(-1);
				e.setDetails("Please check connection status");
				e.setMessage("No internet");
				e.setId("internal error");
				runOnUiThread(false, -1, null, e);
				return;
			}
			
			/*
			 * Do the actual interwebs stuff
			 */
			
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
			
			HttpRequestBase request;
			EtaResponse response = new EtaResponse();
			try {
				
				// Execute the correct request type
				switch (mRequestType) {
				case POST:
					HttpPost post = null;
					if (mBody == null) {
						post = new HttpPost(mPath);
						post.setEntity(new UrlEncodedFormEntity(Utils.bundleToNameValuePair(mApiParams), HTTP.UTF_8));
					} else {
						mPath = pathQuery(mPath, mApiParams);
						post = new HttpPost(mPath);
						if (mBody != null) {
							post.setEntity(new StringEntity(mBody.toString()));
						}
					}
					request = post;
					break;
					
				case GET: 
					mPath = pathQuery(mPath, mApiParams);
					HttpGet get = new HttpGet(mPath);
					request = get;
					break;
					
				case PUT: 
					HttpPut put = null;
					if (mBody == null) {
						put = new HttpPut(mPath);
						put.setEntity(new UrlEncodedFormEntity(Utils.bundleToNameValuePair(mApiParams), HTTP.UTF_8));						
					} else {
						mPath = pathQuery(mPath, mApiParams);
						put = new HttpPut(mPath);
						if (mBody != null) {
							put.setEntity(new StringEntity(mBody.toString()));
						}
					}
					request = put;
					break;
					
				case DELETE:
					mPath = pathQuery(mPath, mApiParams);
					HttpDelete delete = new HttpDelete(mPath);
					request = delete;
					break;
					
				default:
					EtaLog.d(TAG, "Unknown RequestType: " + mRequestType.toString() + " - Aborting!");
					return;
				}
				
				for (Header h : mHeaders)
					request.setHeader(h);
				
				HttpResponse resp = httpClient.execute(request);
				
				response.set(resp);
				
			} catch (UnknownHostException e) {
				response.set(errorToString(EtaError.UNKNOWN_HOST, "UnknownHostException"));
				EtaLog.d(TAG, e);
			} catch (ClientProtocolException e) {
				response.set(errorToString(EtaError.CLIENT_PROTOCOL_EXCEPTION, "ClientProtocolException"));
				EtaLog.d(TAG, e);
			} catch (IOException e) {
				response.set(errorToString(EtaError.IO_EXCEPTION, "IOException"));
				EtaLog.d(TAG, e);
			} finally {
				// Close connection, to deallocate resources
				httpClient.getConnectionManager().shutdown();
			}

			if (isFlag(FLAG_PRINT_DEBUG)) {
				printDebug(response);
			}
			
			if (Utils.isSuccess(response.getStatusCode())) {
				
				if (!mPath.contains(Request.Endpoint.SESSIONS)) {
					updateSessionInfo(response.getHeaders());
				}
				
				mEta.getCache().put(response);
				
				if ( !isFlag(FLAG_CACHE_HIT) || !isFlag(FLAG_ONLY_RETURN_CACHE)) {
					convert(false, response);
				}

			// Error, try to get new session token, then do request again.
			} else {
				
				EtaError error = EtaError.fromJSON(response.getJSONObject());
				
				int e = error.getCode();
				
				if ( !isFlag(FLAG_SESSION_REFRESH) && ( e == 1101 || e == 1108 ) ) {
					
					setFlag(FLAG_SESSION_REFRESH);
					mEta.getSessionManager().performRequest(Api.this);
					mEta.getSessionManager().recoverSession();
					
				} else if ( e == 2015 && mRetryCount < 3) {
					
				    for (Header h : response.getHeaders()) {
				    	if (h.getName().equals(Request.Header.RETRY_AFTER)) {
				    		
				    		int sec = Integer.valueOf(h.getValue());
				    		
				    		mEta.getHandler().postDelayed(new Runnable() {
								
								public void run() {
									runThread();
								}
							}, sec);
				    		
				    	} 
				    }
				    
				} else {
					error.setOriginalData(response.getString());
					runOnUiThread(false, response.getStatusCode(), null, error);
				}
			}
		}
	};
	
	private String pathQuery(String path, Bundle params) {
		if (params.size() == 0)
			return path;
		
		List<NameValuePair> nvp = Utils.bundleToNameValuePair(mApiParams);
		return path + "?" + URLEncodedUtils.format(nvp, HTTP.UTF_8);
	}
	
	/**
	 * Method checks headers to find X-Token and X-Token-Expires.<br>
	 * If they do not exist, nothing happens as the call has a wrong endpoint, or other
	 * non-API regarding error. If they do exist, then they are checked by the Session
	 * to find out if there are any changes.
	 * @param headers to check for new token.
	 */
	private void updateSessionInfo(Header[] headers) {
		String token = null;
	    String expire = null;
	    for (Header h : headers) {
	    	if (h.getName().equals(Request.Header.X_TOKEN)) {
	    		token = h.getValue();
	    	} else if (h.getName().equals(Request.Header.X_TOKEN_EXPIRES)) {
	    		expire = h.getValue();
	    	}
	    }
	    if (token == null || expire == null)
	    	return;
	    
	    mEta.getSessionManager().updateTokens(token, expire);
	    
	}

	/**
	 * Prints any relevant information about the Api object, that has just been executed
	 * @param dataWrapper with information
	 */
	private void printDebug(EtaResponse dataWrapper) {
    	debug.append("#Request: ").append(mRequestType.toString()).append(" ").append(mPath).append("\n");
    	if (mBody != null) {
        	debug.append("Body: ").append(mBody.toString()).append("\n");
    	}
    	debug.append("Headers: ").append(mHeaders.toString()).append("\n");
    	
    	debug.append("Status: ").append(dataWrapper.getStatusCode()).append("\n");
    	debug.append("Return Headers: ");
		for (Header h : dataWrapper.getHeaders())
			debug.append(h.getName()).append(": ").append(h.getValue()).append(", ");
		debug.append("\n");
		String data = dataWrapper.getString();
    	debug.append("Data: ").append(data);
    	EtaLog.d(TAG, debug.toString());
	}
	
	/**
	 * Converts a valid http response into a format, that will match the 
	 * callback interface given to this Api object.
	 * @param isCache is data from the EtaCache
	 * @param resp the data to convert
	 */
	private void convert(boolean isCache, EtaResponse resp) {
		
		EtaError er = null;
		switch (getListenerType(mListener)) {
		
		case ITEM: 
			EtaObject object = EtaErnObject.fromJSON(resp.getJSONObject());
			if (object == null) {
				er = new EtaError();
				er.setCode(EtaError.SDK_ERROR_MISMATCH).setMessage("").setOriginalData(resp.getString());
			}
			runOnUiThread(isCache, resp.getStatusCode(), object, er);
			break;
			
		case LIST:
			ArrayList<EtaObject> objects = EtaErnObject.fromJSON(resp.getJSONArray());
			if (objects.size() == 0) {
				er = new EtaError();
				er.setOriginalData(resp.getString());
			}
			runOnUiThread(isCache, resp.getStatusCode(), objects, er);
			break;

		case OBJECT:
			runOnUiThread(isCache, resp.getStatusCode(), resp.getJSONObject(), er);
			break;

		case ARRAY:
			runOnUiThread(isCache, resp.getStatusCode(), resp.getJSONArray(), er);
			break;

		case STRING:
			runOnUiThread(isCache, resp.getStatusCode(), resp.getString(), er);
			break;
			
		default:
			
			break;
		}
		
	}
	
	/**
	 * Determines if current thread is UI thread, or we need a Runnable when
	 * doing the callback. <br>
	 * Creating a Runnable and adding a new message to the main Looper is rather time
	 * consuming (100+ milliseconds) so, we'd rather not hit that every time.
	 * @param isCache is the data from the EtaCache
	 * @param statusCode from the Api response
	 * @param data from the Api response
	 * @param error from the Api response
	 */
	private void runOnUiThread(final boolean isCache, final int statusCode,final Object data,final EtaError error) {
		
		mFinished = true;
		
		Runnable postResult = new Runnable() {

			public void run() {
				triggerListener(isCache, statusCode, data, error);
			}
		};
		
		if (mHandler != null) {
			mHandler.post(postResult);
		} else {
			mEta.getHandler().post(postResult);
		}
		
	}
	
	/**
	 * The thing that actually does the callback.
	 * @param isCache
	 * @param statusCode
	 * @param data
	 * @param error
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void triggerListener(final boolean isCache, final int statusCode,final Object data,final EtaError error) {
		
		if (!isFlag(FLAG_CANCEL_IF_POSSIBLE)) {
			
			switch (getListenerType(mListener)) {
			case ITEM:
				((ItemListener)mListener).onComplete(isCache, statusCode, (EtaErnObject)data, error);
				break;

			case LIST: 
				((ListListener)mListener).onComplete(isCache, statusCode, (ArrayList<EtaErnObject>)data, error); 
				break;

			case OBJECT:
				((JsonObjectListener)mListener).onComplete(isCache, statusCode, (JSONObject)data, error);
				break;

			case ARRAY:
				((JsonArrayListener)mListener).onComplete(isCache, statusCode, (JSONArray)data, error);
				break;

			case STRING:
				((StringListener)mListener).onComplete(isCache, statusCode, (String)data, error);
				break;

			default:
				throw new IllegalArgumentException("Invalid interface: " + mListener.getClass().getName());
			}
			
		}
	}
	
	private ListenerType getListenerType(ApiListener<?> listener) {

		if (mListener instanceof ItemListener<?>)
			return ListenerType.ITEM;

		if (mListener instanceof ListListener<?>)
			return ListenerType.LIST;

		if (mListener instanceof JsonObjectListener)
			return ListenerType.OBJECT;

		if (mListener instanceof JsonArrayListener)
			return ListenerType.ARRAY;

		if (mListener instanceof StringListener)
			return ListenerType.STRING;
		
		return null;
		
	}
	
	private String errorToString(int errorCode, String data) {
		EtaError er = new EtaError();
		er.setCode(EtaError.UNKNOWN_HOST);
		er.setDetails(data);
		er.setId("No API error code available.");
		er.setMessage("Client side error, perhaps no internet?");
		return er.toJSON().toString();
	}
	
	public interface ApiListener<T> { }

	public interface ItemListener<T extends EtaErnObject> extends ApiListener<T> {
		public void onComplete(boolean isCache, int statusCode, T item, EtaError error);
	}

	public interface ListListener<T extends EtaErnObject> extends ApiListener<List<? extends EtaErnObject>> {
		public void onComplete(boolean isCache, int statusCode, List<T> list, EtaError error);
	}

    public interface JsonObjectListener extends ApiListener<JSONObject> {
		public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error);
    }

    public interface JsonArrayListener extends ApiListener<JSONArray> {
		public void onComplete(boolean isCache, int statusCode, JSONArray list, EtaError error);
    }

    public interface StringListener extends ApiListener<String> {
		public void onComplete(boolean isCache, int statusCode, String string, EtaError error);
    }

}