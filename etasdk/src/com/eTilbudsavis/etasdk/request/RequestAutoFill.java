package com.eTilbudsavis.etasdk.request;

import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;

import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.RequestDebugger;

public class RequestAutoFill {
	
	private boolean mIsRunning = false;
	private Request<?> mParentRequest;
	private OnAutoFillComplete mParentListener;
	private Handler mHandler;
	private AtomicInteger mCount = new AtomicInteger();
	
	protected void done() {
		if (mCount.get()==0 && !mParentRequest.isCanceled()) {
			mParentListener.onComplete();
		}
		mCount.decrementAndGet();
	}
	
	protected void run(Request<?> parent, OnAutoFillComplete listener) {
		mParentRequest = parent;
		mParentListener = listener;
		mCount.incrementAndGet();
	}
	
	protected boolean isRunning() {
		return mIsRunning;
	}
	
	protected void add(Request<?> request) {
		if (mParentRequest.isCanceled()) {
			return;
		}
		mCount.incrementAndGet();
		request.setTag(mParentRequest.getTag());
		request.setDebugger(mParentRequest.getDebugger());
	}
	
	public boolean isCancled() {
		return mParentRequest == null || mParentRequest.isCanceled();
	}
	
	public OnAutoFillComplete getListener() {
		return mParentListener;
	}
	
	public void setHandler(Handler h) {
		mHandler = h;
	}
	
	protected Handler getHandlet() {
		return mHandler;
	}
	
	public interface OnAutoFillComplete {
		public void onComplete();
	}
	
	public static class AutoFillParams {
		
		public Object tag = null;
		public RequestDebugger debugger = null;
		
		public AutoFillParams(Object tag, RequestDebugger debugger) {
			this.tag = tag;
			this.debugger = debugger;
		}

		public AutoFillParams(Request<?> parent) {
			this(parent.getTag(), parent.getDebugger());
		}

	}
}
