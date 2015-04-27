/*******************************************************************************
 * Copyright 2015 eTilbudsavis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
