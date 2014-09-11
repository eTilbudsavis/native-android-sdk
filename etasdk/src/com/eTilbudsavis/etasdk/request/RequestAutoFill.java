package com.eTilbudsavis.etasdk.request;

import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;

import com.eTilbudsavis.etasdk.Network.Request;

public class RequestAutoFill {

	private boolean mIsRunning = false;
	private Request<?> mPriorRequest;
	private OnAutoFillComplete mListener;
	private Handler mHandler;
	private AtomicInteger mCount = new AtomicInteger();
	
	protected void done() {
		if (mCount.get()==0 && !mPriorRequest.isCanceled()) {
			mListener.onComplete();
		}
		mCount.decrementAndGet();
	}
	
	protected void run(Request<?> priorRequest, OnAutoFillComplete listener) {
		mPriorRequest = priorRequest;
		mListener = listener;
		mCount.incrementAndGet();
	}
	
	protected boolean isRunning() {
		return mIsRunning;
	}
	
	protected void add(Request<?> request) {
		if (mPriorRequest.isCanceled()) {
			return;
		}
		mCount.incrementAndGet();
		request.setTag(mPriorRequest.getTag());
		
	}
	
	public boolean isCancled() {
		return mPriorRequest == null || mPriorRequest.isCanceled();
	}
	
	public OnAutoFillComplete getListener() {
		return mListener;
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
	
}
