package com.eTilbudsavis.etasdk.test;

import com.eTilbudsavis.etasdk.Log.EtaLog;

public class EtaSdkTest {
	
	public static final String TAG = EtaSdkTest.class.getSimpleName();
	private static String mCurrentTitle;
	private static long mStartGlobal = 0;
	private static long mStartScoped = 0;
	
	public static void test() {
		
		mStartGlobal = System.currentTimeMillis();
		UtilsTest.test();
		ObjectTest.test();
		
		EtaLog.d(TAG, "Done: " + mCurrentTitle + " (" + (System.currentTimeMillis()-mStartScoped) + "ms)");
		EtaLog.d(TAG, "**************************************");
		EtaLog.d(TAG, " All test passed (" + (System.currentTimeMillis()-mStartGlobal) + "ms)");
		EtaLog.d(TAG, "**************************************");
		
	}
	
	protected static void start(String testName) {
		
		if (mCurrentTitle==null) {
			mStartScoped = System.currentTimeMillis();
			EtaLog.d(TAG, "Performing: " + testName);
		} else if (!mCurrentTitle.equals(testName)) {
			EtaLog.d(TAG, "Done: " + mCurrentTitle + " (" + (System.currentTimeMillis()-mStartScoped) + "ms)");
			mStartScoped = System.currentTimeMillis();
			EtaLog.d(TAG, "Performing: " + testName);
		}
		
		mCurrentTitle = testName;
		
	}
	
	protected static void logTest(String tag, String testName) {
		EtaLog.d(tag, " - " + testName);
	}
	
	
}
