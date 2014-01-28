package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Param;

@SuppressWarnings("rawtypes")
public class RequestQueue {
	
	public static final String TAG = "RequestQueue";

    /** Number of network request dispatcher threads to start. */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    /** Number of log entries the RequestQueue should save. */
    private static final int DEFAULT_LOG_SIZE = 20;
    
    /** Eta object controlling the whole lot */
    private final Eta mEta;
    
    /** Queue for preparation, and cache checks */
    private final PriorityBlockingQueue<Request> mCacheQueue = new PriorityBlockingQueue<Request>();
    
    /** The queue of requests that are actually going out to the network. */
    private final PriorityBlockingQueue<Request> mNetworkQueue = new PriorityBlockingQueue<Request>();
    
    /** Queue of items waiting for session request */
    private final LinkedList<Request> mParking = new LinkedList<Request>();
    
    /** Network dispatchers, the threads that will actually perform the work */
    private NetworkDispatcher[] mNetworkDispatchers;

    /** Network interface for performing requests. */
    private final Network mNetwork;
    
    /** Queue of requests, that need validation, and cache check */
    private CacheDispatcher mCacheDispatcher;
    
    /** Cache interface for retrieving and storing respones. */
    private final Cache mCache;

    /** Response delivery mechanism. */
    private final Delivery mDelivery;
    
    /** Atomic number generator for sequencing requests in the queues */
    private AtomicInteger mSequenceGenerator = new AtomicInteger();
    
    /** Logging mechanism that allows for the summary of requests to be stored */
    private List<JSONObject> mLog;
    
    /**
     * 
     * @param eta, the eTilbudsavis SDK object to use for requests
     * @param cache to use for this RequestQueue
     * @param network the implementation you want to use for this RequestQueue
     * @param poolSize, number of threads to do requests
     * @param delivery object for returning objects to UI thread
     * @param logSize the number of logs to save. use 0 to skip logging.
     */
    public RequestQueue(Eta eta, Cache cache, Network network, int poolSize, Delivery delivery, int logSize) {
    	mEta = eta;
		mCache = cache;
		mNetwork = network;
		mDelivery = delivery;
		mNetworkDispatchers = new NetworkDispatcher[poolSize];
		mDelivery.mRequestQueue = this;
		mLog = Collections.synchronizedList(new LinkedListFixedLength<JSONObject>(logSize));
	}
    
	/** Construct with default poolsize, and the eta handler running on main thread */
    public RequestQueue(Eta eta, Cache cache, Network network) {
    	this(eta, cache, network, DEFAULT_NETWORK_THREAD_POOL_SIZE, new Delivery(eta.getHandler()), DEFAULT_LOG_SIZE);
    }
    
	/** Initialize all mechanisms required to dispatch requests */
	public void start() {
		
		// Creates new CacheDispatcher
		mCacheDispatcher = new CacheDispatcher(mEta, mCacheQueue, mNetworkQueue, mCache, mDelivery);
		mCacheDispatcher.start();

        // Create network dispatchers (and corresponding threads) up to the pool size.
        for (int i = 0; i < mNetworkDispatchers.length; i++) {
            NetworkDispatcher networkDispatcher = new NetworkDispatcher(mEta, this, mNetworkQueue, mNetwork, mCache, mDelivery);
            mNetworkDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
        
    }
    
    /** Stop all currently running dispatchers (Staging, caching and network) */
    public void stop() {
    	
    	if (mCacheDispatcher != null)
    		mCacheDispatcher.quit();
    	
    	for (NetworkDispatcher n : mNetworkDispatchers) {
    		if (n != null)
    			n.quit();
    	}
    	
    }
    
    /**
     * Method that allows SessionManager to resume all requests, when no more session requests are to be made.<br>
     */
	public void runParkedQueue() {
		
		if (mEta.getSessionManager().isRequestInFlight()) {
			EtaLog.d(TAG, "Cannot resume yet, session still in flight.");
			return;
		}
		
		synchronized (mParking) {
			
			for (Request r : mParking) {
				
				r.addEvent("resuming-request");
	    		mCacheQueue.add(r);
	    		
			}
			mParking.clear();
			
		}
		
	}
	
	/**
	 * This method is mostly for statistics and allows RequestQueue to tie up any loose
	 * ends that might be in a request. In the future, this can be used for better SDK cache control
	 * as multiple requests to the same endpoint, can be queued, and only one may be dispatched.
	 * On complete the others can be triggered, and instantly hitting local cache.
	 * @param req - request, that finished
	 * @param resp - the server response
	 */
	public synchronized void finish(Request req, Response resp) {
		
		// If the log is enabled, add the request summary
		if (req.logSummary()) {
			
			JSONObject summary = req.getLog().getSummary();
			try {
				summary.put("duration", req.getLog().getTotalDuration());
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			}
			mLog.add(summary);
			
		}
		
	}
	
	/**
	 * Returns the log entries for the last <i>logSize</i> requests
	 * @return
	 */
	public List<JSONObject> getRequestLogs() {
		return mLog;
	}
	
	/** Add a new request to this RequestQueue, everything from this point onward will be performed on separate threads */
    public Request add(Request r) {
    	
    	r.setSequence(mSequenceGenerator.incrementAndGet());
    	
		prepareRequest(r);
		
    	if (mEta.getSessionManager().isRequestInFlight() && !r.isSessionEndpoint()) {
    		
    		r.addEvent("added-to-parking-queue");
    		
    		synchronized (mParking) {
        		mParking.add(r);
			}
    		
    	} else {
    		
        	r.addEvent("added-to-queue");
        	
    		if (r.isSessionEndpoint() && r != mEta.getSessionManager().getRequestInFlight()) {
    			EtaLog.d(TAG, "Session changes should be handled by SessionManager. This request might cause problems");
    		}
    		
    		mCacheQueue.add(r);
    		
    	}
    	
    	return r;
    	
    }
    
	/**
	 * Method for adding required parameters for calling the eTilbudsavis.<br>
	 * @param request
	 */
	private void prepareRequest(Request request) {
		
		request.addEvent("preparing-sdk-parameters");
		// Append HOST if needed
		String url = request.getUrl();
		if (!url.startsWith("http")) {
			String preUrl = Endpoint.getHost();
			request.setUrl(preUrl + url);
		}
		
		// Append necessary API parameters
		Bundle params = new Bundle();

		String version = Eta.getInstance().getAppVersion();
		if (version != null) {
			params.putString(Param.API_AV, version);
		}

		if (request.useLocation() && mEta.getLocation().isSet()) {
			params.putAll(mEta.getLocation().getQuery());
		}

		request.putQueryParameters(params);

	}

    
}
