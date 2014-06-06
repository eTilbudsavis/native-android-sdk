package com.eTilbudsavis.etasdk.Log;

import android.util.Log;

public class DevLogger implements EtaLogger {

	/** Variable to control the size of the exception log */
	public static final int DEFAULT_EXCEPTION_LOG_SIZE = 32;
	
	private EventLog mLog = new EventLog(DEFAULT_EXCEPTION_LOG_SIZE);
	
	public int v(String tag, String msg) {
		return Log.v(tag, msg);
	}
	
	public int v(String tag, String msg, Throwable tr) {
		return Log.v(tag, msg, tr);
	}
	
	public int d(String tag, String msg) {
		return Log.d(tag, msg);
	}
	
	public int d(String tag, String msg, Throwable tr) {
		return Log.d(tag, msg, tr);
	}
	
	public int i(String tag, String msg) {
		return Log.i(tag, msg);
	}
	
	public int i(String tag, String msg, Throwable tr) {
		return Log.i(tag, msg, tr);
	}
	
	public int w(String tag, String msg) {
		return Log.w(tag, msg);
	}
	
	public int w(String tag, String msg, Throwable tr) {
		return Log.w(tag, msg, tr);
	}
	
	public int e(String tag, String msg) {
		return Log.e(tag, msg);
	}
	
	public int e(String tag, String msg, Throwable tr) {
		if (msg == null || msg.length() == 0) {
			msg = tr.getMessage();
		}
		tr.printStackTrace();
		return Log.e(tag, msg, tr);
	}

	public EventLog getLog() {
		return mLog;
	}
	
}
