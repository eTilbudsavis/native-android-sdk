package com.eTilbudsavis.etasdk.log;

import com.eTilbudsavis.etasdk.Constants;


public class SyncLog {

	public static final String TAG = Constants.getTag(SyncLog.class);

	private static boolean mLogSyncCycle = false;
	private static boolean mSyncCycle = false;

	public static void setLogSync(boolean logSyncCycle) {
		mLogSyncCycle = logSyncCycle;
		
	}

	public static void setLog(boolean log) {
		mSyncCycle = log;
	}

	public static int sync(String tag, String msg) {
		return (mLogSyncCycle ? EtaLog.v(tag, msg) : 0);
	}

	public static int log(String tag, String msg) {
		return (mSyncCycle ? EtaLog.v(tag, msg) : 0);
	}
	
}
