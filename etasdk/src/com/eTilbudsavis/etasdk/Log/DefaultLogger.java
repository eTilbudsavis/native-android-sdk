package com.eTilbudsavis.etasdk.Log;

import android.util.Log;

public class DefaultLogger implements EtaLogger {

	/** Variable to control the size of the exception log */
	public static final int DEFAULT_EXCEPTION_LOG_SIZE = 16;
	
	private EventLog mLog = new EventLog(DEFAULT_EXCEPTION_LOG_SIZE);
	
	public int v(String tag, String msg) {
		return 0;
	}
	
	public int v(String tag, String msg, Throwable tr) {
		return 0;
	}
	
	public int d(String tag, String msg) {
		return 0;
	}
	
	public int d(String tag, String msg, Throwable tr) {
		return 0;
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
