package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.concurrent.BlockingQueue;

import android.os.Process;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.Utils.Endpoint;

@SuppressWarnings("rawtypes")
public class CacheDispatcher extends Thread {

    /** Eta object controlling the whole lot */
    private final Eta mEta;
    
    /** The queue of requests to service. */
    private final BlockingQueue<Request> mQueue;

    /** The queue of requests to service. */
    private final BlockingQueue<Request> mNetworkQueue;

    /** The cache to write to. */
    private final Cache mCache;
    
    /** For posting responses and errors. */
    private final Delivery mDelivery;
    
    /** Used for telling us to die. */
    private volatile boolean mQuit = false;

    public CacheDispatcher(Eta eta, BlockingQueue<Request> cacheQueue, BlockingQueue<Request> networkQueue, Cache cache, Delivery delivery) {
        mQueue = cacheQueue;
        mNetworkQueue = networkQueue;
        mCache = cache;
        mDelivery = delivery;
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
                
                Response response = mCache.get(request);
                
                // Try cache, only if Method == GET
                NetworkResponse networkResponse = null;
                
                
                //TODO add to cache, if possible
                // hmm, we'd need a parsed response for that...
                
                // Parse the response here on the worker thread.
                response = request.parseNetworkResponse(networkResponse);
                
                
                mDelivery.postResponse(request, response);
                
            } catch (Exception e) {
            	// What kind of errors do we expect?
                // VolleyLog.e(e, "Unhandled exception %s", e.toString());
                mDelivery.postError(request, new EtaError());
            }
        }
    }
	
	public void prepare(Request request) {

		// Append HOST if needed
		String url = request.getUrl();
		if (!url.matches("^http.*")) {
			request.setUrl(Endpoint.HOST + url);
		}
		
//		if (mId != null) {
//			mPath += mId;
//		}
		
		// Required API key.
//		mApiParams.putString(API_KEY, mEta.getApiKey());

		// Add location
		if (request.useLocation() && mEta.getLocation().isSet()) {
//			mApiParams.putAll(mEta.getLocation().getQuery());
		}
		
		// Set headers if session is OK
		if (mEta.getSessionManager().getSession().getToken() != null) {
//			setHeader(HEADER_X_TOKEN, mEta.getSession().getToken());
//			String sha256 = Utils.generateSHA256(mEta.getApiSecret() + mEta.getSession().getToken());
//			setHeader(HEADER_X_SIGNATURE, sha256);
		}

//		setHeader(HEADER_CONTENT_TYPE, mContentType.toString());


	}
	
}
