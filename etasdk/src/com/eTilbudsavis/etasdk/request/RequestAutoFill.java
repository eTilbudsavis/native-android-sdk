package com.eTilbudsavis.etasdk.request;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.RequestDebugger;
import com.eTilbudsavis.etasdk.Network.RequestQueue;

public abstract class RequestAutoFill<T> {
	
	private OnAutoFillCompleteListener mListener;
	private T mData;
	private AutoFillParams mParams;
	private List<Request<?>> mRequests = new ArrayList<Request<?>>();
	
	public abstract List<Request<?>> createRequests(T data);
	
	public void setAutoFillParams(AutoFillParams params) {
		mParams = params;
	}
	
	public void setOnAutoFillCompleteListener(OnAutoFillCompleteListener l) {
		mListener = l;
	}
	
	public void execute(T data, RequestQueue rq) {
		mRequests.clear();
		mData = data;
		mRequests.addAll(createRequests(mData));
		runRequests(rq);
		done();
	}
	
	private void runRequests(RequestQueue rq) {
		
		for (Request<?> r : mRequests) {
			r.addEvent("executed-by-autofiller");
			mParams.applyParams(r);
			r.setDeliverOnThread(true);
			rq.add(r);
		}
		
	}

	protected void done() {
		if (isFinished()) {
			mListener.onComplete();
		}
	}

	public List<Request<?>> getRequests() {
		return mRequests;
	}
	
	/**
	 * Returns true if ALL requests in this {@link RequestAutoFill} is finished
	 * @return true if all {@link RequestAutoFill} are finished, else false
	 */
	public boolean isFinished() {
		for (Request<?> r : mRequests) {
			if (!r.isFinished()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns true if ALL requests in this {@link RequestAutoFill} is cancelled
	 * @return true if all {@link RequestAutoFill} are cancelled, else false
	 */
	public boolean isCancled() {
		for (Request<?> r : mRequests) {
			if (!r.isCanceled()) {
				return false;
			}
		}
		return true;
	}
	
	public void cancel() {
		for (Request<?> r : mRequests) {
			r.cancel();
		}
	}
	
	public interface OnAutoFillCompleteListener {
		public void onComplete();
	}
	
	public static class AutoFillParams {
		
		private Object tag = null;
		private RequestDebugger debugger = null;
		private Handler handler = null;
		private boolean useLocation = true;
		private boolean ignoreCache = false;
//		private boolean isCachable = true;
		
		public void applyParams(Request<?> r) {
			r.setTag(tag);
			r.setDebugger(debugger);
			r.setUseLocation(useLocation);
			r.setHandler(handler);
			r.setIgnoreCache(ignoreCache);
		}
		
		public AutoFillParams() {
			this(new Object(), null, null, true, false);
		}
		
		public AutoFillParams(Request<?> parent) {
			this(parent.getTag(), parent.getDebugger(), parent.getHandler(), parent.useLocation(), parent.ignoreCache());
		}
		
		public AutoFillParams(Object tag, RequestDebugger debugger, Handler h, boolean useLocation, boolean ignoreCache) {
			this.tag = (tag == null ? new Object() : tag);
			this.debugger = debugger;
			this.handler = h;
			this.useLocation = useLocation;
			this.ignoreCache = ignoreCache;
		}
		
	}
}
