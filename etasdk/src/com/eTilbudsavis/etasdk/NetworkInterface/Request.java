package com.eTilbudsavis.etasdk.NetworkInterface;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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

	/** Should this request use location in the query */
	private boolean mUseLocation = true;
	
	/** If true Request will return data from cache if exists */
	private boolean mSkipCache = false;
	
	/** Whether or not responses to this request should be cached. */
	private boolean mCacheResponse = true;
	
	/** Whether or not this request has been canceled. */
	private boolean mCanceled = false;
	
	/** Indication if the request is finished */
	private boolean mFinished = false;
	
	/** Item for containing cache items */
	private Cache.Item mCache;
	
	/** Log of this request */
	private final EventLog mLog;

	/** Allows for the network reponse printed */
    private boolean mDebugNetwork = false;

	/** Allows for log to be printed */
    private boolean mDebugLog = false;
    
    /** String containing network debug info */
    private String mDebugNetworkString = "Networkwork not complete yet";
    
	/** Handler, for returning requests on correct queue */
	private Handler mHandler;
	
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
	
	public EventLog getLog() {
		return mLog;
	}
	
	/** Mark this request as canceled.  No callback will be delivered. */
	public void cancel() {
		mCanceled = true;
	}

	/** Returns true if this request has been canceled. */
	public boolean isCanceled() {
		return mCanceled;
	}

	/** Returns a list of headers for this request. */
	public Map<String, String> getHeaders() {
		return mHeaders;
	}

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
	public boolean cacheResponse() {
		return mCacheResponse;
	}

	protected void cacheResponse(boolean isResponseCachable) {
		mCacheResponse = isResponseCachable;
	}
	
	/**
	 * Returns true, if this request should return cache, else false
	 * @return
	 */
	public boolean skipCache() {
		return mSkipCache;
	}

	/**
	 * Set whether this request may use data from cache or not
	 * @param useCache
	 * @return
	 */
	public Request skipCache(boolean skip) {
		mSkipCache = skip;
		return Request.this;
	}
	
	public Request setHandler(Handler h) {
		mHandler = h;
		return Request.this;
	}
	
	public Handler getHandler() {
		return mHandler;
	}
	
	public boolean useLocation() {
		return mUseLocation;
	}
	
	public Request useLocation(boolean useLocation) {
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
	
	public boolean hasCache() {
		return mCache != null;
	}
	
	public boolean isFinished() {
		return mFinished;
	}
	
	public Request finished() {
		mLog.add("finished");
		mFinished = true;
		return Request.this;
	}
	
	public Request setCacheItem(Cache.Item cache) {
		mCache = cache;
		return Request.this;
	}
	
	public Bundle getQueryParameters() {
		return mQuery;
	}

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
				return Utils.buildQueryString(params).getBytes(getParamsEncoding());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	abstract protected Response<T> parseNetworkResponse(NetworkResponse response);
	
	protected void deliverResponse(boolean isCache, T response, EtaError error) {
		if (mListener != null) {
			mListener.onComplete(isCache, response, error);
		}
	}
	
	public int compareTo(Request<T> other) {
		Priority left = this.getPriority();
		Priority right = other.getPriority();
		return left == right ? this.mSequence - other.mSequence : right.ordinal() - left.ordinal();
	}
	
    public boolean mayCache(NetworkResponse response) {
    	return false;
    }

	public Request debugNetwork(boolean debug) {
		mDebugNetwork = debug;
		return this;
	}

	public Request debugLog(boolean debug) {
		mDebugLog = debug;
		return this;
	}
	
    public void printDebug() {

    	if (mDebugLog) {
    		EtaLog.d(TAG, mLog.getString(getClass().getSimpleName()));
    	}
    	if (mDebugNetwork) {
        	EtaLog.d(TAG, mDebugNetworkString);
    	}

    }
    
	public void setNetworkDebug(NetworkResponse r) {
		
		String newLine = System.getProperty("line.separator");
		
		StringBuilder sb = new StringBuilder();
		sb.append("--- Begin debug - Request -----------------------------------").append(newLine);
		sb.append(getMethodString()).append(" ").append(getUrl()).append(newLine);
		sb.append("Query:        ").append(mQuery.toString()).append(newLine);
		sb.append("Content-Type: ").append(getBodyContentType()).append(newLine);
		if (getBody() != null) {
		sb.append("Body:         ").append(new String(getBody())).append(newLine);
		}
		sb.append("Headers:      ").append(getHeaders().toString()).append(newLine);
    	sb.append("--- Network Response ----------------------------------------").append(newLine);
		sb.append("HTTP-SC:      ").append(r.statusCode).append(newLine);
		sb.append("Headers:      ").append(r.headers.toString()).append(newLine);
		
		String data = new String(r.data);
		
		int l = data.length();
		if (data != null && 400 < l ) {
			data = data.substring(0, 200) + " ( TOO MUCH DATA FOR LOGCAT, ONLY DISPLAYING A SUBSTRING ) " + data.substring(l-100, l-1);
		}
		
		sb.append("Data:         ").append(data).append(newLine);
		sb.append("--- End debug - Request -------------------------------------");
		
		mDebugNetworkString = sb.toString();
		
	}
	
	
	public class Endpoint extends com.eTilbudsavis.etasdk.Utils.Endpoint { }

	public class Param extends com.eTilbudsavis.etasdk.Utils.Param { }

	public class Sort extends com.eTilbudsavis.etasdk.Utils.Sort { }

	public class Header extends com.eTilbudsavis.etasdk.Utils.Header { }
	
	
	
}
