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
package com.eTilbudsavis.etasdk.Network;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.EtaLog.EventLog;
import com.eTilbudsavis.etasdk.Utils.Param;

@SuppressWarnings("rawtypes")
public class RequestQueue {
	
	public static final String TAG = "RequestQueue";
	
    /** Number of network request dispatcher threads to start. */
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

    /** Number of log entries the RequestQueue should save. */
    private static final int DEFAULT_LOG_SIZE = 32;
    
    /** Eta object controlling the whole lot */
    private final Eta mEta;
    
    /** All requests currently being handled by this request queue */
    private final Set<Request> mCurrentRequests = new HashSet<Request>();
    
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
    private final AtomicInteger mSequenceGenerator = new AtomicInteger();
    
    /** The EventLog containing condensed information about requests and their responses */
    private EventLog mLog;

    /* tmp var for testing */
	public int dataIn = 0;
    /* tmp var for testing */
	public int dataOut = 0;
	
    /**
     * Construct a new RequestQueue for processing requests.
     * This RequestQueue is primarily aimed at fetching data from the eTilbudsavis API.
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
		mNetworkDispatchers = new NetworkDispatcher[poolSize];
		mDelivery = delivery;
		mLog = new EventLog(logSize);
	}
    
	/**
	 * Construct with default poolsize, and the eta handler running on main thread
     * @param eta - the eTilbudsavis SDK object to use for requests
     * @param cache - to use for this RequestQueue
     * @param network - the implementation you want to use for this RequestQueue
	 */
    public RequestQueue(Eta eta, Cache cache, Network network) {
    	this(eta, cache, network, DEFAULT_NETWORK_THREAD_POOL_SIZE, new Delivery(eta.getHandler()), DEFAULT_LOG_SIZE);
    }
    
	/**
	 * Initialize all mechanisms required to dispatch requests
	 */
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
    
    /**
     * Stop all currently running dispatchers (Staging, caching and network)
     */
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
			EtaLog.d(TAG, "Cannot resume yet, session request still in flight.");
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
	 * @param request - request, that finished
	 */
	public synchronized void finish(Request request) {
		
		synchronized (mCurrentRequests) {
			mCurrentRequests.remove(request);
		}
		
		// Append the request summary to the debugging log
		mLog.add(EventLog.TYPE_REQUEST, request.getLog().getSummary());
		
	}
	
	/**
	 * Get the log of all requests that have passed through this RequestQueue.<br><br>
	 * 
	 * The log contains a summary of the request it self, and the response given by the API.
	 * This can be very useful for debugging.
	 * @return the EventLog from this RequestQueue
	 */
	public EventLog getLog() {
		return mLog;
	}
	
	/**
	 * Get the {@link Network} associated with this {@link RequestQueue}
	 * @return A {@link Network}
	 */
	public Network getNetwork() {
		return mNetwork;
	}

	/**
	 * Get the {@link Cache} associated with this {@link RequestQueue}
	 * @return A {@link Cache}
	 */
	public Cache getCache() {
		return mCache;
	}
	
	public void clear() {
		mCache.clear();
		mLog.clear();
	}
	
	/**
	 * Add a new request to this RequestQueue, everything from this point onward will be performed on separate threads
	 * @param request
	 * 			the request to add
	 * @return the request object
	 */
    public Request add(Request request) {
    	
    	synchronized (mCurrentRequests) {
			mCurrentRequests.add(request);
		}
    	
    	request.setRequestQueue(this);
    	
    	request.setSequence(mSequenceGenerator.incrementAndGet());
    	
		prepareRequest(request);
		
    	if (mEta.getSessionManager().isRequestInFlight() && !isSessionEndpoint(request)) {
    		
    		request.addEvent("added-to-parking-queue");
    		
    		synchronized (mParking) {
        		mParking.add(request);
			}
    		
    	} else {
    		
        	request.addEvent("added-to-queue");
        	
    		if (isSessionEndpoint(request) && request != mEta.getSessionManager().getRequestInFlight()) {
    			EtaLog.d(TAG, "Session changes should be handled by SessionManager. This request might cause problems");
    		}
    		
    		mCacheQueue.add(request);
    		
    	}
    	
    	return request;
    	
    }
    
    /**
     * Cancel all {@link Request requests} with a given tag.
     * 
     * <p>Matching is done with {@code ==} and not the
     * {@link Object#equals(Object) equals} method</p>
     * 
     * @param tag A tag to match
     * @return
     */
    public int cancelAll(Object tag) {

    	int count = 0;
    	if (tag == null) {
    		// tag == null is no dice, it'll cancel all requests...
    		return count;
    	}
    	
    	synchronized (mCurrentRequests) {
			for (Request r : mCurrentRequests) {
				if (r.getTag() == tag) {
					count++;
					r.cancel();
				}
			}
		}
    	return count;
    }
    
	private boolean isSessionEndpoint(Request r) {
		return r.getUrl().contains(Endpoint.SESSIONS);
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
	
	/**
	 * Get the current count of requests performed by this RequestQueue.
	 * All requests are counted, including successful, errors, cancelled e.t.c.
	 * @return The number of requests received
	 */
	public int getRequestCount() {
		return mSequenceGenerator.get();
	}
    
}
