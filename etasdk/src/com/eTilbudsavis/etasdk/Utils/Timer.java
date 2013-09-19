package com.eTilbudsavis.etasdk.Utils;

import android.os.SystemClock;

public class Timer {

	private long mStart = 0;
	private long mTime = 0;
	private String mResponse = null;
	private int mMaxTime = Integer.MAX_VALUE;
	private int mMinTime = 0;
	private int mMaxSize = Integer.MAX_VALUE;
	private int mMinSize = 0;
	private String mThreadName = "";
	
	public Timer() {
		mStart = SystemClock.elapsedRealtime();
		mThreadName = Thread.currentThread().getName();
	}

	public Timer setTimeMax(int timeInMilis) {
		mMaxTime = timeInMilis;
		return this;
	}

	public Timer setTimeMin(int timeInMilis) {
		mMinTime = timeInMilis;
		return this;
	}

	public Timer setSizeMax(int size) {
		mMaxSize = size;
		return this;
	}

	public Timer setSizeMin(int size) {
		mMinSize = size;
		return this;
	}
	
	public void print(String name) {
		mTime = SystemClock.elapsedRealtime()-mStart;
		
		int length = mResponse != null ? mResponse.length() : 0;
		
		if (mMinTime <= mTime && mTime <= mMaxTime && mMinSize <= length && length <= mMaxSize) {
			Utils.logd("TimePrinter", mThreadName + ": " + String.valueOf(mTime) + "ms, " + name + " (size: " + String.valueOf(length) + ")");
		}
	}

	public void print(String name, String response) {
		mResponse = response;
		print(name);
	}

	public void print(String name, int minTime) {
		mMinTime = minTime;
		print(name);
	}

	public void print(String name, int minTime, int maxTime) {
		mMinTime = minTime;
		mMaxTime = maxTime;
		print(name);
	}
	
}
