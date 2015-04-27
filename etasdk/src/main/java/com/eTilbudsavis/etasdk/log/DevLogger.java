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

import android.util.Log;

import com.eTilbudsavis.etasdk.Constants;

public class DevLogger implements EtaLogger {
	
	public static final String TAG = Constants.getTag(DevLogger.class);
	
	/** Variable to control the size of the exception log */
	public static final int DEFAULT_EXCEPTION_LOG_SIZE = 32;
	
	private EventLog mLog;
	
	public DevLogger() {
		this(DEFAULT_EXCEPTION_LOG_SIZE);
	}
	
	public DevLogger(int logSize) {
		mLog = new EventLog(logSize);
	}
	
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
		mLog.add(EventLog.TYPE_EXCEPTION, EtaLog.exceptionToJson(tr));
		return Log.e(tag, msg, tr);
	}

	public EventLog getLog() {
		return mLog;
	}
	
}
