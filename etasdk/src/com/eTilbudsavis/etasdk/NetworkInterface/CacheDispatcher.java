package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import android.os.Bundle;
import android.os.Process;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Endpoint;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.Utils.Utils;

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

				Cache.Item cache = mCache.get(request);
				
				// if the cache is valid, then return it
				boolean useCache = !(cache == null || cache.isExpired()) && !request.skipCache();
				if ( useCache  ) {
					
					// Parse the response here on the worker thread.
					Response response = null; // = request.parseCacheResponse(networkResponse);
					mDelivery.postResponse(request, response);

				}
				
				boolean useNetwork = true || !useCache;
				if ( useNetwork ) {
					
					mNetworkQueue.add(request);
					
				}
				
			} catch (Exception e) {
				// What kind of errors do we expect?
				mDelivery.postError(request, new EtaError());
			}
		}
	}

	/**
	 * Method for adding required parameters for calling the eTilbudsavis.<br>
	 * @param request
	 */
	private void prepare(Request request) {

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
