package com.eTilbudsavis.etasdk.request;

import java.util.List;

import android.os.Handler;

import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.RequestDebugger;

public abstract class RequestAutoFill {
	
	private OnAutoFillCompleteListener mListener;
	private AutoFillParams mParams;
	private List<Request<?>> mRequests;
	
	protected void done() {
		if (isFinished()) {
			mListener.onComplete();
		}
	}
	
	protected void addRequest(Request<?> request) {
		mParams.applyParams(request);
		mRequests.add(request);
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
	
	protected void setOnAutoFillCompleteListener(OnAutoFillCompleteListener listener) {
		mListener = listener;
	}
	
	public OnAutoFillCompleteListener getOnAutoFillCompleteListener() {
		return mListener;
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
		private boolean isCachable = true;
		
		public void applyParams(Request<?> r) {
			
			r.setTag(tag);
			r.setDebugger(debugger);
			r.setUseLocation(useLocation);
			r.setHandler(handler);
			r.setIgnoreCache(ignoreCache);
			
		}
		
		public AutoFillParams() {
			this(new Object(), null, null, true, false, true);
		}
		
		public AutoFillParams(Request<?> parent) {
			this(parent.getTag(), parent.getDebugger(), parent.getHandler(), parent.useLocation(), parent.ignoreCache(), parent.isCachable());
		}
		
		public AutoFillParams(Object tag, RequestDebugger debugger, Handler h, boolean useLocation, boolean ignoreCache, boolean isCachable) {
			this.tag = (tag == null ? new Object() : tag);
			this.debugger = debugger;
			this.handler = h;
			this.useLocation = useLocation;
			this.ignoreCache = ignoreCache;
			this.isCachable = isCachable;
		}
		
	}
}
