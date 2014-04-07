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
package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.os.Bundle;
import android.os.Handler;

import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.EtaLog.EventLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

@SuppressWarnings("rawtypes")
public abstract class Request<T> implements Comparable<Request<T>> {
	
	public static final String TAG = "Request";
	
	/** Default encoding for POST or PUT parameters. See {@link #getParamsEncoding()}. */
	protected static final String DEFAULT_PARAMS_ENCODING = "utf-8";
	
	/** Default cache time in milliseconds */
	protected static final long DEFAULT_CACHE_TTL = 3 * Utils.MINUTE_IN_MILLIS;
	
	/** Default connection timeout, this is for both connection and socket */
	private static final int CONNECTION_TIME_OUT = (int) (20 * Utils.SECOND_IN_MILLIS);
	
	/** Listener interface, for responses */
	private final Listener<T> mListener;
	
	/** Request method of this request.  Currently supports GET, POST, PUT, and DELETE. */
	private final Method mMethod;
	
	/** URL of this request. */
	private String mUrl;
	
	/** Headers to be used in this request */
	private Map<String, String> mHeaders = new HashMap<String, String>();
	
	/** Parameters to use in this request */
	private Bundle mQuery = new Bundle();
	
	/** Sequence number used for prioritizing the queue */
	private int mSequence = 0;

	/** Item for containing cache items */
	protected Map<String, Cache.Item> mCache = new HashMap<String, Cache.Item>();
	
	/** Should this request use location in the query */
	private boolean mUseLocation = true;
	
	/** If true Request will return data from cache if exists */
	private boolean mIgnoreCache = false;
	
	/** Whether or not responses to this request should be cached. */
	private boolean mIsCachable = true;
	
	/** Whether or not this request has been canceled. */
	private boolean mCanceled = false;

    /** Indication if the request is finished */
    private boolean mFinished = false;
    
    private int mTimeout = CONNECTION_TIME_OUT;
    
	/** Log of this request */
	private final EventLog mLog;
	
	private boolean mCacheHit = false;

	/** Allows for the network response printed */
    private boolean mDebugNetwork = false;

	/** Allows for the network response printed */
    private boolean mDebugPerformance = false;
    
	/** Handler, for returning requests on correct queue */
	private Handler mHandler;
	
	/** Boolean deciding if the summary should be added to RequestQueue log entries */
	private boolean mLogSummary = true;
	
	/**  */
	private RequestQueue mRequestQueue;
	
	/** A tag to identify the request, useful for bulk operations */
	private Object mTag;
	
	public enum Priority {
		LOW, MEDIUM, HIGH
	}
	
	/** Supported request methods. */
//	public interface Method {
//		int GET = 0;
//		int POST = 1;
//		int PUT = 2;
//		int DELETE = 3;
//	}
	
	public enum Method { GET, POST, PUT, DELETE }

	/**
	 * Creates a new request with the given method (one of the values from {@link Method}),
	 * URL, and error listener.  Note that the normal response listener is not provided here as
	 * delivery of responses is provided by subclasses, who have a better idea of how to deliver
	 * an already-parsed response.
	 */
	public Request(Method method, String url, Listener<T> listener) {
		mMethod = method;
		mUrl = url;
		mListener = listener;
		mLog = new EventLog();
	}
	
	/** Adds event to a request, for later debugging purposes */
	public void addEvent(String event) {
		mLog.add(event);
	}
	
	/**
	 * Get the log for this request, log contains actions, and timings that have been performed on this request
	 * @return the EventLog for this request
	 */
	public EventLog getLog() {
		return mLog;
	}
	
	/**
	 * If true, this requests summary (found in the EventLog with getLog()) will be saved in
	 * the RequestQueue's log history. This is true by default.<br><br>
	 * This can be set to false, if you e.g. want to avoid flooding the log, with 
	 * unnecessary messages.
	 * @param saveSummaryToLog true is logging should be enabled for this request.
	 * @return this object
	 */
	public Request logSummary(boolean saveSummaryToLog) {
		mLogSummary = saveSummaryToLog;
		return this;
	}
	
	public boolean logSummary() {
		return mLogSummary;
	}
	
	/** Mark this request as canceled.  No callback will be delivered. */
	public void cancel() {
		mCanceled = true;
	}

	/** Returns true if this request has been canceled. */
	public boolean isCanceled() {
		return mCanceled;
	}
	
	/**
	 * Method for determining if the request is finished.
	 * Whether the request was successful or not, it <b>NOT</b> reflected here.
	 * @return true if the SDK, has finished this request
	 */
    public boolean isFinished() {
            return mFinished;
    }
    
    /**
     * Method for marking a request as finished
     * @return this object
     */
    public Request finish(String reason) {
    	mLog.add(reason);
    	
		try {
			mLog.getSummary().put("duration", getLog().getTotalDuration());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
    	mFinished = true;
    	mRequestQueue.finish(this);
    	return Request.this;
    }
    
    public void stats(int in, int out) {
    	mRequestQueue.dataIn += in;
    	mRequestQueue.dataOut += out;
    }
    
    /**
     * Get the connection timeout for this request.
     * <p>The timeout will be the same for connecting, and for reading data</p>
     * @return The timeout in milliseconds
     */
    public int getTimeOut() {
    	return mTimeout;
    }
    
    /**
     * Set the timeout value for this request.
     * <p>The timeout will be the same for connecting, and for reading data</p>
     * @param timeout
     * @return
     */
    public Request setTimeOut(int timeout) {
    	mTimeout = timeout;
    	return this;
    }
    
	/** Returns a list of headers for this request. */
	public Map<String, String> getHeaders() {
		return mHeaders;
	}
	
	/**
	 * Set any headers wanted in the request
	 * @param headers to include
	 */
	public void setHeaders(Map<String, String> headers) {
		mHeaders.putAll(headers);
	}
	
	/**
	 * Return the method for this request.  Can be one of the values in {@link Method}.
	 */
	public Method getMethod() {
		return mMethod;
	}
	
//	public String getMethodString() {
//		switch (mMethod) {
//		case 0: return "GET";
//		case 1: return "POST";
//		case 2: return "PUT";
//		case 3: return "DELETE";
//		default: return "UNDEFINED";
//		}
//	}
	/**
	 * Returns the response listener for this request
	 * @return a listener of type T
	 */
	public Listener getListener() {
		return mListener;
	}

	/**
	 * returns whether this request is cachable or not
	 * @return true if the request is cachable
	 */
	public boolean isCachable() {
		return mIsCachable;
	}
	
	/**
	 * Whether this request should be added to cache.
	 * @param isResponseCachable
	 */
	protected void setCachable(boolean isResponseCachable) {
		mIsCachable = isResponseCachable;
	}
	
	/**
	 * Set a tag on this request. This can later be used for bulk operations.
	 * @param tag An object to identify this request buy
	 * @return This object
	 */
	public Request setTag(Object tag) {
		mTag = tag;
		return this;
	}
	
	/**
	 * A tag to identify this request (or its origin) for performing batch operations.
	 * @return An object. This requests tag or null.
	 */
	public Object getTag() {
		return mTag;
	}
	
	/**
	 * Set the executing request queue, in order to later inform the RequestQueue
	 * if this requests finished execution.
	 * @param requestQueue The RequestQueue that is performing this Request
	 * @return This object
	 */
	public Request setRequestQueue(RequestQueue requestQueue) {
		mRequestQueue = requestQueue;
		return this;
	}
	
	/**
	 * Find out if the response from this request is from cache or not.
	 * @return true if response is from cache, else false
	 */
	public boolean isCacheHit() {
		return mCacheHit;
	}
	
	/**
	 * Set whether this was from cache
	 * @param cacheHit true is response is cache hit, else false
	 * @return this object
	 */
	public Request setCacheHit(boolean cacheHit) {
		mCacheHit = cacheHit;
		return this;
	}
	
	/**
	 * The time-to-live for a given Cache.Item this request may create
	 * @return request time-to-live in milliseconds
	 */
	public long getCacheTTL() {
		return DEFAULT_CACHE_TTL;
	}
	
	/**
	 * Method determining is cache should be ignored
	 * @return true, if this request should query the cache for data
	 */
	public boolean ignoreCache() {
		return mIgnoreCache;
	}

	/**
	 * Set whether this request may use data from cache or not
	 * @param skip true if cache should not be used
	 * @return this object
	 */
	public Request setIgnoreCache(boolean skip) {
		mIgnoreCache = skip;
		return Request.this;
	}
	
	/**
	 * This method enables you to have the response posted via any given handler.
	 * Thereby returning on any (also non-UI) thread, for further processing.
	 * @param handler that will receive the callback
	 * @return this object
	 */
	public Request setHandler(Handler handler) {
		mHandler = handler;
		return Request.this;
	}
	
	/**
	 * Get the custom handler for this request.
	 * @return a handler or null, if using default handler
	 */
	public Handler getHandler() {
		return mHandler;
	}
	
	/**
	 * Get the cache item that this request have generated
	 * @return an Cache.Item, or null is no Cache.Item have been generated
	 */
	public Map<String, Cache.Item> getCache() {
		return mCache;
	}
	
	/**
	 * Set the Cache.Item that have been generated from this request.
	 * @param cache the generated Cache.Item
	 * @return this object
	 */
	public Request putCache(Map<String, Cache.Item> cache) {
		mCache.putAll(cache);
		return this;
	}
	
	/**
	 * Determining if this request should include location data.
	 * @return true if location data should be used in this request, else false.
	 */
	public boolean useLocation() {
		return mUseLocation;
	}
	
	/**
	 * Enable or disable the usage of location data in this request.<br>
	 * Please use with care, <b>most API v2 endpoints require location data</b>
	 * @param useLocation true to include, and false exclude location data in request parameters
	 * @return
	 */
	public Request setUseLocation(boolean useLocation) {
		mUseLocation = useLocation;
		return Request.this;
	}
	
	/** 
	 * Get the url for this request
	 * @return the url for this request
	 */
	public String getUrl() {
		return mUrl;
	}

	/** 
	 * Set the url of this request
	 * @param url to use in this request
	 * @return this object
	 */
	public Request setUrl(String url) {
		mUrl = url;
		return Request.this;
	}
	
	/**
	 * Get the query parameters that will be used to perform this query.<br>
	 * @return the query parameters
	 */
	public Bundle getQueryParameters() {
		return mQuery;
	}
	
	/**
	 * Add request parameters to this request. The parameters will be appended 
	 * as HTTP query parameters to the URL, when the SDK executes the request. 
	 * Therefore you should <b>not</b> do nested structures, only simple key-value-pairs.
	 * This is <b>not the same as appending a body</b> to the request, when doing a PUT or POST request.
	 * @param query
	 * @return
	 */
	public Request putQueryParameters(Bundle query) {
		mQuery.putAll(query);
		return Request.this;
	}
	
	/**
	 * Get the priority of which this request has.
	 * @return the request priority
	 */
	protected Priority getPriority() {
		return Priority.MEDIUM;
	}
	
	/**
	 * Get the sequence number that this request have been given. 
	 * The sequence number reflects the order of which the request was handed to the
	 * {@link #com.eTilbudsavis.etasdk.NetworkInterface.RequestQueue RequestQueue}.
	 * and can partially be used to determine the order of execution.
	 * @return the sequence number (a non-negative number)
	 */
	protected int getSequence() {
		return mSequence;
	}
	
	/**
	 * Set a sequence number for when the request was received by
	 * {@link #com.eTilbudsavis.etasdk.NetworkInterface.RequestQueue RequestQueue}
	 * in order to partially determine the order of execution of requests.
	 * @param seq
	 */
	protected void setSequence(int seq) {
		mSequence = seq;
	}
	
	/**
	 * Get parameter encoding of the request. Useful for decoding data.
	 * @return the encoding
	 */
	public String getParamsEncoding() {
		return DEFAULT_PARAMS_ENCODING;
	}
	
	/**
	 * Get content type of the body. Useful for setting headers.
	 * @return
	 */
	public String getBodyContentType() {
		return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
	}
	
	/**
	 * Get the body of this request. 
	 * @return body, if body have been set, else null
	 */
	public byte[] getBody() {
		return null;
	}
	
	/**
	 * Method to be implemented, should handle parsing of network data, and simultaneously create
	 * a Cache.Item (or several Cache.Item) if such a item(s) can and shall be created.
	 * @param response NetworkResponse to parse into <b>both</b> a Response, and Cache.Item
	 * @return a valid Response is possible, or null
	 */
	abstract protected Response<T> parseNetworkResponse(NetworkResponse response);
	
	/**
	 * Method to be implemented in subclasses, which will be able to parse a Cache.Item,
	 * previously generated by this request in {@link #parseNetworkResponse(NetworkResponse) parseNetworkResponse()}.
	 * @param c item to parse
	 * @return a valid Response is possible, or null
	 */
	abstract protected Response<T> parseCache(Cache c);
	
	/**
	 * Method for easily delivering the response to the user, via the given callback-listener.
	 * @param response to deliver, may be null
	 * @param error to deliver, may be null
	 */
	protected void deliverResponse(T response, EtaError error) {
		if (mListener != null) {
			mListener.onComplete(response, error);
		}
	}
	
	public int compareTo(Request<T> other) {
		Priority left = this.getPriority();
		Priority right = other.getPriority();
		return left == right ? this.mSequence - other.mSequence : right.ordinal() - left.ordinal();
	}
	
	/**
	 * Set to true, to enable printing of the request timings via LogCat.
	 * @see {@link #com.eTilbudsavis.etasdk.Utils.EtaLog EtaLog} for detalis about SDK Log.d output
	 * @param printRequestTimings true to print timings of the request
	 * @return this object
	 */
	public Request debugPerformance(boolean printRequestTimings) {
		mDebugPerformance = printRequestTimings;
		return this;
	}

	/**
	 * Set to true, to enable printing of network debugging information via LogCat.
	 * @see {@link #com.eTilbudsavis.etasdk.Utils.EtaLog EtaLog} for detalis about SDK Log.d output
	 * @param printNetworkInfo true to print a complete network debug log
	 * @return this object
	 */
	public Request debugNetwork(boolean printNetworkInfo) {
		mDebugNetwork = printNetworkInfo;
		return this;
	}
	
	/**
	 * Method for triggering print of log information, if logging was enabled.
	 */
	public void debugPrint() {
		
		if (mDebugNetwork) {
			mLog.printSummary();
		}
		
		if (mDebugPerformance) {
			mLog.printEventLog(getClass().getSimpleName());
		}
		
	}

	/**
	 * Returns a complete printable representation of this Request, e.g:
	 * 
	 * <li>GET: https://api.etilbudsavis.dk/v2/catalogs/{catalog_id}?param1=value1&amp;param2=value2</li>
	 * 
	 * <p>The SDK/API parameters are not added to the 
	 * {@link Request#getQueryParameters() query parameters}, before the request
	 * is handed to the {@link RequestQueue}. So if you want to have the SDK/API
	 * parameters appended as well in the string do:</p>
	 * <li>Eta.getInstance().add(Request)</li>
	 * <p>and then call: </p>
	 * <li>toString()</li>
	 */
	@Override
	public String toString() {
		return mMethod.toString() + ": " + Utils.buildQueryString(this);
	}
	
}
