package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.concurrent.BlockingQueue;

import android.os.Process;

import com.eTilbudsavis.etasdk.Eta;

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
			
			request.addEvent("cache-dispatcher");
			
			// If the request was cancelled already, do not perform the network request.
			if (request.isCanceled()) {
				request.addEvent("request-cancelled");
				continue;
			}
			
			if (!request.ignoreCache()) {
				Response response = request.parseCache(mCache);
				// if the cache is valid, then return it
				if ( response != null  ) {
					request.addEvent("post-cache-item");
					// Parse the response here on the worker thread.
					request.setCacheHit(true);
					mDelivery.postResponse(request, response);
					continue;
				}
			}
			
			request.addEvent("add-to-network-queue");
			mNetworkQueue.add(request);
			
		}
	}

}
