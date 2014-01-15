package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.LinkedList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.SessionManager;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

@SuppressWarnings("rawtypes")
public class RequestQueue {
	
	public static final String TAG = "RequestQueue";
	
    /** Number of network request dispatcher threads to start. */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
    
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
    
    /** Thread lock */
    private Object LOCK = new Object();
    
    /** Atomic number generator for sequencing requests in the queues */
    private AtomicInteger mSequenceGenerator = new AtomicInteger();
    
    /**
     * 
     * @param eta
     * @param cache
     * @param network
     * @param poolSize
     * @param delivery
     */
    public RequestQueue(Eta eta, Cache cache, Network network, int poolSize, Delivery delivery) {
    	mEta = eta;
		mCache = cache;
		mNetwork = network;
		mDelivery = delivery;
		mNetworkDispatchers = new NetworkDispatcher[poolSize];
		mDelivery.mRequestQueue = this;
	}
    
	/** Construct with default poolsize, and the eta handler running on main thread */
    public RequestQueue(Eta eta, Cache cache, Network network) {
    	this(eta, cache, network, DEFAULT_NETWORK_THREAD_POOL_SIZE, new Delivery(eta.getHandler()));
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
    
	public void badSession(Request req, EtaError error) {
		
		if (req.isSession()) {
			
			// If it's a login attempt or alike, then we'll just skip it
			if (error == null || SessionManager.recoverableError(error)) {
				refreshOrDieTrying(req, error);
			} else {
				mDelivery.postError(req, error);
			}
			
		} else {
			
			refreshOrDieTrying(req, error);
			
		}
			
	}
	
	private void refreshOrDieTrying(Request req, EtaError error) {
		
		// Or not session endpoint
		boolean refreshing = mEta.getSessionManager().refresh();
		if (refreshing && !req.isSession()) {
			mParking.add(req);
		} else {
			mDelivery.postError(req, error);
		}
		
	}
	
	public void runParkedQueue() {
		
		if (mEta.getSessionManager().requestInFlight()) {
			EtaLog.d(TAG, "Cannot resume yet, session still in flight.");
			return;
		}
		
		synchronized (mParking) {
			
			for (Request r : mParking) {
				r.addEvent("resuming-request");
			}
			mNetworkQueue.addAll(mParking);
			mParking.clear();
			
		}
		
	}
	
	public void finish(Request req, Response resp) {
		
		EtaLog.d(TAG, "Duration: " + req.getLog().getTotalDuration());
		
	}
	
	/** Add a new request to this RequestQueue, everything from this point onward will be performed on separate threads */
    public Request add(Request r) {
    	
    	r.setSequence(mSequenceGenerator.incrementAndGet());
    	r.addEvent("request-added");
    	
    	EtaLog.d(TAG, r.getMethodString()  + ": " + r.getUrl());
    	
		prepareRequest(r);
		
    	if (mEta.getSessionManager().requestInFlight()) {
    		synchronized (mParking) {
        		r.addEvent("session-in-flight-added-to-parking");
        		mParking.add(r);
			}
    		
    	} else {
    		
    		if (r.getUrl().contains(Request.Endpoint.SESSIONS)) {
    			EtaLog.d(TAG, "Request to session endpoint, not originating from SessionManager.\n"
    					+ "If you don't update SessionManager afterwards this will cause problems");
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
			String preUrl = Request.Endpoint.getHost();
			request.setUrl(preUrl + url);
		}
		
		// Append necessary API parameters
		Bundle params = new Bundle();

		String version = Eta.getInstance().getAppVersion();
		if (version != null) {
			params.putString(Request.Param.API_AV, version);
		}

		if (request.useLocation() && mEta.getLocation().isSet()) {
			params.putAll(mEta.getLocation().getQuery());
		}

		request.putQueryParameters(params);

	}

    
}
