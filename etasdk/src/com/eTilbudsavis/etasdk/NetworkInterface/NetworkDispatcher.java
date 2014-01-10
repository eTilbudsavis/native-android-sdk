package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import android.os.Bundle;
import android.os.Process;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkHelpers.SessionError;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Endpoint;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

@SuppressWarnings("rawtypes")
public class NetworkDispatcher extends Thread {

	public static final String TAG = "NetworkDispatcher";
	
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
                
                prepare(request);
                
                // If session is expired, add request to a OnSessionUpdate retry queue
                if (mEta.getSessionManager().getSession().isExpired() && !request.getUrl().contains(Endpoint.SESSIONS)) {
                	mRequestQueue.performSessionUpdate(request);
                	continue;
                }
                
                // Perform the network request.
                NetworkResponse networkResponse = mNetwork.performRequest(request);
                
                updateSessionInfo(networkResponse.headers);
                
                // Parse the response here on the worker thread.
                Response<?> response = request.parseNetworkResponse(networkResponse);
                
                request.printDebug(request, networkResponse);
                
                // If the request is cachable
                if (request.getMethod() == Method.GET && request.cacheResponse() && response.cache != null) {
                	mCache.put(response.cache);
                }
                
                mRequestQueue.complete(request);
                mDelivery.postResponse(request, response);
                
            } catch (SessionError e) {
            	
            	if (!mRequestQueue.performSessionUpdate(request)) {
                    mDelivery.postError(request, e);
            	}
            	
            } catch (EtaError e) {

            	EtaLog.d(TAG, e);
                mDelivery.postError(request, e);
                
            } catch (Exception e) {
            	
            	EtaLog.d(TAG, e);
                mDelivery.postError(request, new EtaError());
                
            }
        }
    }
	
	/**
	 *  If it's a post to sessions, it's to create a new Session, then the API key is needed.
	 *  In any other case, just set the headers, with the current session token and signature.
	 * @param request
	 */
	private void prepare(Request<?> request) {
		
        if (request.getMethod() == Method.POST && request.getUrl().contains(Request.Endpoint.SESSIONS)) {
        	
        	Bundle b = new Bundle();
        	b.putString(Request.Param.API_KEY, mEta.getApiKey());
        	request.putQueryParameters(b);
        	
        } else {
        	
        	Map<String, String> headers = new HashMap<String, String>();
        	String token = mEta.getSessionManager().getSession().getToken();
        	headers.put(Request.Header.X_TOKEN, token);
        	String sha256 = Utils.generateSHA256(mEta.getApiSecret() + token);
        	headers.put(Request.Header.X_SIGNATURE, sha256);
        	request.setHeaders(headers);
        	
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
	    
	    if ( !(token == null || expire == null) ) {
	    	mEta.getSessionManager().updateTokens(token, expire);
	    }
	    
	}
	
}
