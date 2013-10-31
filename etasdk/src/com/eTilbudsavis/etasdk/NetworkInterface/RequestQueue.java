package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkHelpers.HttpNetwork;
import com.eTilbudsavis.etasdk.NetworkHelpers.StringRequest;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Utils;

@SuppressWarnings("rawtypes")
public class RequestQueue {

    /** Number of network request dispatcher threads to start. */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
    
    /** Eta object controlling the whole lot */
    private final Eta mEta;
    
    /** Queue for preparation, and cache checks */
    private final PriorityBlockingQueue<Request> mCacheQueue = new PriorityBlockingQueue<Request>();
    
    /** The queue of requests that are actually going out to the network. */
    private final PriorityBlockingQueue<Request> mNetworkQueue = new PriorityBlockingQueue<Request>();

    /** Queue of items waiting for session request */
    private final PriorityBlockingQueue<Request> mSessionQueue = new PriorityBlockingQueue<Request>();

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
    
	public synchronized void sessionUpdate(Request r) {
		
		if (mEta.getSessionManager().getSession().isExpired()) {
			// If the session is still expired
			mSessionQueue.add(r);
			
			mEta.getSessionManager().update(new Listener<String>() {

				public void onComplete(boolean isCache, String response, EtaError error) {
					
					if (response != null) {
						for(Request r : mSessionQueue) {
				    		mNetworkQueue.add(r);
				    	}
					} else {
						// TODO: retry somehow, based on error code
					}
					
				}
			});
				
		} else {
			// If this method had lock while a thread tried to add new request,
			// session might not be expired any more, so just retry immediately.
			mNetworkQueue.add(r);
		}
		
	}
	
	/** Add a new request to this RequestQueue, everything from this point onward will be performed on separate threads */
    public Request add(Request r) {
    	
    	r.setSequence(mSequenceGenerator.incrementAndGet());
    	
    	mCacheQueue.add(r);
    	
    	return r;
    	
    }

}
