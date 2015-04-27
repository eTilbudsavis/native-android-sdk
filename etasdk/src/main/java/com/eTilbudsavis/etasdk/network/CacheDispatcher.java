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
package com.eTilbudsavis.etasdk.network;

import java.util.concurrent.BlockingQueue;

import android.os.Process;

import com.eTilbudsavis.etasdk.Constants;

public class CacheDispatcher extends Thread {
	
	public static final String TAG = Constants.getTag(CacheDispatcher.class);
	
	/** The queue of requests to service. */
	private final BlockingQueue<Request<?>> mQueue;

	/** The queue of requests to service. */
	private final BlockingQueue<Request<?>> mNetworkQueue;

	/** The cache to write to. */
	private final Cache mCache;

	/** For posting responses and errors. */
	private final Delivery mDelivery;

	/** Used for telling us to die. */
	private volatile boolean mQuit = false;

	public CacheDispatcher(BlockingQueue<Request<?>> cacheQueue, BlockingQueue<Request<?>> networkQueue, Cache cache, Delivery delivery) {
		mQueue = cacheQueue;
		mNetworkQueue = networkQueue;
		mCache = cache;
		mDelivery = delivery;
	}
	
	/**
	 * Terminate this CacheDispatcher. Once terminated, requests will no longer be passed to the NetworkDispatcher. 
	 */
	public void quit() {
		mQuit = true;
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		Request<?> request;
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
			
			// If the request was cancelled already, do not perform the network request.
			if (request.isCanceled()) {
				request.finish("cache-dispatcher-cancelled-on-recieved");
				continue;
			} else {
				request.addEvent("recieved-by-cache-dispatcher");
			}
			
			if (!request.ignoreCache()) {
				Response<?> response = request.parseCache(mCache);
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
