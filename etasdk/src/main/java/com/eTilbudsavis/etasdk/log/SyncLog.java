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
