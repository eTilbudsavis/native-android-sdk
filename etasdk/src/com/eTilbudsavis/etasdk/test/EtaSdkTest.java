package com.eTilbudsavis.etasdk.test;

import com.eTilbudsavis.etasdk.Log.EtaLog;

public class EtaSdkTest {
	
	public static final String TAG = EtaSdkTest.class.getSimpleName();
	
	public static void test() {
		
		long start = System.currentTimeMillis();

		UtilsTest.test();
		
		ObjectTest.test();
		log(TAG, "All test passed (" + (System.currentTimeMillis()-start) + "ms)");
		
	}
	

	protected static void log(String tag, String testName) {
		EtaLog.d(tag, "Done: " + testName);
	}
	
	
}
