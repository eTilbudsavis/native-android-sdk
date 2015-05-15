package com.eTilbudsavis.etasdk.test;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;

public class EtaSdkTest {
	
	public static final String TAG = Constants.getTag(EtaSdkTest.class);
	private static final boolean LOG = false;
	private static String mCurrentTitle;
	private static long mStartScoped = 0;
	
	public static void test() {
		
		long start = System.currentTimeMillis();

		ValidatorTest.test();
		UtilsTest.test();
		ColorUtilsTest.test();
		ObjectTest.test();
		CatalogThumbBitmapProcessorTest.test();
		SerializationSpeedTest.test();
		EnvironmentTest.test();
        EventTest.test();
		
		String ok = "*   All test passed (" + (System.currentTimeMillis()-start) + "ms)   *";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ok.length() ; i++) {
			sb.append("*");
		}
		String header = sb.toString();
		
		EtaLog.d(TAG, "Done: " + mCurrentTitle + " (" + (System.currentTimeMillis()-mStartScoped) + "ms)");
		EtaLog.d(TAG, header);
		EtaLog.d(TAG, ok);
		EtaLog.d(TAG, header);
		mCurrentTitle = null;
		
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
		if (LOG) {
			EtaLog.d(tag, " - " + testName);
		}
	}
	
	protected static void logTestWarning(String tag, String testName, String warning) {
		EtaLog.w(tag, " - " + testName + " - " + warning);
	}
	
}
