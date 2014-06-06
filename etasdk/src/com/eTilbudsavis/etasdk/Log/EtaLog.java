package com.eTilbudsavis.etasdk.Log;


public class EtaLog {

	public static final String TAG = EtaLog.class.getSimpleName();

	private static final String LOG_D_CHUNK = "[chunk %s/%s] %s";
	
	private static EtaLogger mLogger;
	
	public static EtaLogger getLogger() {
		return mLogger;
	}
	
	public static void setLogger(EtaLogger l) {
		mLogger = l;
	}
	
	public static int v(String tag, String msg) {
		return mLogger.v(tag, msg);
	}
	
	public static int v(String tag, String msg, Throwable tr) {
		return mLogger.v(tag, msg, tr);
	}
	
	public static int d(String tag, String msg) {
		return mLogger.d(tag, msg);
	}
	
	public static int d(String tag, String msg, Throwable tr) {
		return mLogger.d(tag, msg, tr);
	}
	
	public static int i(String tag, String msg) {
		return mLogger.i(tag, msg);
	}
	
	public static int i(String tag, String msg, Throwable tr) {
		return mLogger.i(tag, msg, tr);
	}
	
	public static int w(String tag, String msg) {
		return mLogger.w(tag, msg);
	}
	
	public static int w(String tag, String msg, Throwable tr) {
		return mLogger.w(tag, msg, tr);
	}
	
	public static int e(String tag, String msg) {
		return mLogger.e(tag, msg);
	}
	
	public static int e(String tag, String msg, Throwable tr) {
		return mLogger.e(tag, msg, tr);
	}
	
	/**
	 * Send a DEBUG log message. This method will allow messages above the usual Log.d() limit of 4000 chars.
	 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
	 * @param message The message you would like logged.
	 */
	public static void dAll(String tag, String message) {
		
		if (message.length() > 4000) {
		    int chunkCount = message.length() / 4000;     // integer division
	        
		    for (int i = 0; i <= chunkCount; i++) {
		        int max = 4000 * (i + 1);
		        int end = (max >= message.length()) ? message.length() : max;
		        d(tag, String.format(LOG_D_CHUNK, i, chunkCount, message.substring(4000 * i, end) ) );
		    }
		} else {
			d(tag,message);
		}
		
	}
	
	/**
	 * Print a StackTrace from any given point of your source code.
	 */
	public static void printStackTrace() {
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			d(TAG, String.valueOf(ste));
		}
	}
	
}
