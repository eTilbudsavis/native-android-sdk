package com.eTilbudsavis.etasdk.NetworkInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;

import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog.EventLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

@SuppressWarnings("rawtypes")
public abstract class Request<T> implements Comparable<Request<T>> {
	
	public static final String TAG = "Request";

	/** Default encoding for POST or PUT parameters. See {@link #getParamsEncoding()}. */
	protected static final String DEFAULT_PARAMS_ENCODING = "utf-8";
	
	/** Default cache time */
	protected static final long DEFAULT_CACHE_TTL = 3 * Utils.MINUTE_IN_MILLIS;
	
	/** Listener interface, for responses */
	private final Listener<T> mListener;
	
	/** Request method of this request.  Currently supports GET, POST, PUT, and DELETE. */
	private final int mMethod;
	
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
	
	public enum Priority {
		LOW, MEDIUM, HIGH
	}

	/** Supported request methods. */
	public interface Method {
		int GET = 0;
		int POST = 1;
		int PUT = 2;
		int DELETE = 3;
	}

	/**
	 * Creates a new request with the given method (one of the values from {@link Method}),
	 * URL, and error listener.  Note that the normal response listener is not provided here as
	 * delivery of responses is provided by subclasses, who have a better idea of how to deliver
	 * an already-parsed response.
	 */
	public Request(int method, String url, Listener<T> listener) {
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
	 * @return
	 */
	public EventLog getLog() {
		return mLog;
	}
	
	/**
	 * If true, this requests summary (found in the EventLog with getLog()) will be saved in
	 * the RequestQueue's log history. This is true by default.<br><br>
	 * This can be set to false, if you e.g. want to avoid flooding the log. As seen in the ListSyncManager.
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
	 * Method for determining if the request is finished
	 * @return
	 */
    public boolean isFinished() {
            return mFinished;
    }
    
    /**
     * Method for marking a request as finished
     * @return
     */
    public Request finish() {
            mLog.add("finished");
            mFinished = true;
            return Request.this;
    }
    
	/** Returns a list of headers for this request. */
	public Map<String, String> getHeaders() {
		return mHeaders;
	}
	
	/**
	 * Set any headers wanted in the request
	 * @param headers
	 */
	public void setHeaders(Map<String, String> headers) {
		mHeaders.putAll(headers);
	}
	
	/**
	 * Return the method for this request.  Can be one of the values in {@link Method}.
	 */
	public int getMethod() {
		return mMethod;
	}
	
	public String getMethodString() {
		switch (mMethod) {
		case 0: return "GET";
		case 1: return "POST";
		case 2: return "PUT";
		case 3: return "DELETE";
		default: return "UNDEFINED";
		}
	}
	/**
	 * Returns the response listener for this request
	 * @return
	 */
	public Listener getListener() {
		return mListener;
	}

	/**
	 * returns wether this request is cachable or not
	 * @return
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
	
	public boolean isCacheHit() {
		return mCacheHit;
	}
	
	public Request setCacheHit(boolean cacheHit) {
		mCacheHit = cacheHit;
		return this;
	}
	
	/**
	 * The Time To Live for any given Cache.Item this request may create
	 * @return
	 */
	public long getCacheTTL() {
		return DEFAULT_CACHE_TTL;
	}
	
	/**
	 * Returns true, if this request should query the cache for data
	 * @return
	 */
	public boolean ignoreCache() {
		return mIgnoreCache;
	}

	/**
	 * Set whether this request may use data from cache or not
	 * @param useCache
	 * @return
	 */
	public Request setIgnoreCache(boolean skip) {
		mIgnoreCache = skip;
		return Request.this;
	}
	
	/**
	 * This method enables you to have the response returned on any handler.
	 * This is handy, if you want to have the reaponse returned on a non-ui thread
	 * @param h
	 * @return
	 */
	public Request setHandler(Handler h) {
		mHandler = h;
		return Request.this;
	}
	
	/**
	 * Get the custom handler for this request. <br>
	 * Null will be returned if the default handler will be used.
	 * @return a handler or null, if using default handler
	 */
	public Handler getHandler() {
		return mHandler;
	}
	
	public Map<String, Cache.Item> getCache() {
		return mCache;
	}
	
	public Request putCache(Map<String, Cache.Item> cache) {
		mCache.putAll(cache);
		return this;
	}
	
	public boolean useLocation() {
		return mUseLocation;
	}
	
	public Request setUseLocation(boolean useLocation) {
		mUseLocation = useLocation;
		return Request.this;
	}
	
	/** Return the url for this request. */
	public String getUrl() {
		return mUrl;
	}

	/** Set the url of this request. */
	public Request setUrl(String url) {
		mUrl = url;
		return Request.this;
	}
	
	/**
	 * Get the query parameters that will be used to perform this query.<br>
	 * NOTE that this is NOT the same as appending a body to the request, 
	 * when doing a PUT or POST request.
	 * @return
	 */
	public Bundle getQueryParameters() {
		return mQuery;
	}
	
	/**
	 * Add parameters to this request.
	 * @param query
	 * @return
	 */
	public Request putQueryParameters(Bundle query) {
		mQuery.putAll(query);
		return Request.this;
	}

	protected Priority getPriority() {
		return Priority.MEDIUM;
	}

	protected int getSequence() {
		return mSequence;
	}

	protected void setSequence(int seq) {
		mSequence = seq;
	}
	
	public boolean isSessionEndpoint() {
		return mUrl.contains(Endpoint.SESSIONS);
	}
	
	public String getParamsEncoding() {
		return DEFAULT_PARAMS_ENCODING;
	}

	public String getBodyContentType() {
		return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
	}
	
	public byte[] getBody() {
		Bundle params = getQueryParameters();
		if (params != null && params.size() > 0) {
			try {
				return Utils.buildQueryString(params, getParamsEncoding()).getBytes(getParamsEncoding());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	abstract protected Response<T> parseNetworkResponse(NetworkResponse response);

	abstract protected Response<T> parseCache(Cache c);
	
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
	
	public Request debugPerformance(boolean printRequestTimings) {
		mDebugPerformance = printRequestTimings;
		return this;
	}
	
	public Request debugNetwork(boolean printNetworkInfo) {
		mDebugNetwork = printNetworkInfo;
		return this;
	}
	
	public void debugPrint() {
		
		if (mDebugNetwork) {
			mLog.printSummary();
		}
		
		if (mDebugPerformance) {
			mLog.printEventLog(getClass().getSimpleName());
		}
		
	}
	
}
