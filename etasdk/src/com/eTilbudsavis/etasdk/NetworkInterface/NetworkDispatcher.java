package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import android.os.Process;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.Utils.Endpoint;

@SuppressWarnings("rawtypes")
public class NetworkDispatcher extends Thread {

    /** Eta object controlling the whole lot */
    private final Eta mEta;
    
    /** The queue of requests to service. */
	private final BlockingQueue<Request> mQueue;
	
	/**  */
	private final RequestQueue mRequestQueue;
	
    /** The network interface for processing requests. */
    private final Network mNetwork;
    
    /** The cache to write to. */
    private final Cache mCache;
    
    /** For posting responses and errors. */
    private final Delivery mDelivery;
    
    /** Used for telling us to die. */
    private volatile boolean mQuit = false;
    
    public NetworkDispatcher(Eta eta, RequestQueue requestQueue, BlockingQueue<Request> queue, Network network, Cache cache, Delivery delivery) {
        mQueue = queue;
        mNetwork = network;
        mCache = cache;
        mDelivery = delivery;
        mRequestQueue = requestQueue;
        mEta = eta;
    }
    
    public void quit() {
    	mQuit = true;
    }
    
	@Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Request request;
        while (true) {
            try {
                // Take a request from the queue.
                request = mQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }

            try {
                // If the request was cancelled already, do not perform the network request.
                if (request.isCanceled()) {
                    continue;
                }
                
                // If session is expired, add request to a OnSessionUpdate retry queue
                if (mEta.getSessionManager().getSession().isExpired() && !request.getUrl().contains(Endpoint.SESSIONS)) {
                	mRequestQueue.sessionUpdate(request);
                	continue;
                }
                
                // Perform the network request.
                NetworkResponse networkResponse = mNetwork.performRequest(request);
                
                // Parse the response here on the worker thread.
                Response<?> response = request.parseNetworkResponse(networkResponse);

                if(request.shouldPrintDebug()) {
                	request.printDebug(request, networkResponse);
                }
                
                updateSessionInfo(networkResponse.headers);
                
                //TODO add to cache, if possible
                // hmm, we'd need a parsed response for that... 
                
                mDelivery.postResponse(request, response);
                
            } catch (Exception e) {
            	// What kind of errors do we expect?
                // VolleyLog.e(e, "Unhandled exception %s", e.toString());
                mDelivery.postError(request, new EtaError());
            }
        }
    }

	/**
	 * Method checks headers to find X-Token and X-Token-Expires.<br>
	 * If they do not exist, nothing happens as the call has a wrong endpoint, or other
	 * non-API regarding error. If they do exist, then they are checked by the Session
	 * to find out if there are any changes.
	 * @param headers to check for new token.
	 */
	private void updateSessionInfo(Map<String, String> headers) {
		String token = headers.get(Request.Header.X_TOKEN);
	    String expire = headers.get(Request.Header.X_TOKEN_EXPIRES);
	    
	    if (token == null || expire == null)
	    	return;
	    
	    mEta.getSessionManager().updateTokens(token, expire);
	}
	
}
