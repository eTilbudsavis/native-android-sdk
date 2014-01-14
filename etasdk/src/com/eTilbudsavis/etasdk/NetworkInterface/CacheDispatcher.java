package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.concurrent.BlockingQueue;

import android.os.Bundle;
import android.os.Process;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

@SuppressWarnings("rawtypes")
public class CacheDispatcher extends Thread {

	public static final String TAG = "CacheDispatcher";
	
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
				
				request.addEvent("cache-dispatcher");
				// If the request was cancelled already, do not perform the network request.
				if (request.isCanceled()) {
					request.addEvent("request-cancelled");
					continue;
				}
				
				Cache.Item cache = mCache.get(request);
				
				// if the cache is valid, then return it
				boolean useCache = !(cache == null || cache.isExpired()) && !request.skipCache();
				if ( useCache  ) {
					request.addEvent("cache-item-valid");
					// Parse the response here on the worker thread.
					Response response = null; // = request.parseCacheResponse(networkResponse);
					mDelivery.postResponse(request, response);

				}
				
				EtaLog.d(TAG,"ready-for-network");
				// Cannot remember what true is suppose to cover... damn it!
				boolean useNetwork = true || !useCache;
				if ( useNetwork ) {
					request.addEvent("added-to-network-queue");
					mNetworkQueue.add(request);
				}
				
			} catch (Exception e) {
				request.addEvent(String.format("request-failed-%s", e.getMessage()));
				// What kind of errors do we expect?
				mDelivery.postError(request, new EtaError());
			}
		}
	}

}
