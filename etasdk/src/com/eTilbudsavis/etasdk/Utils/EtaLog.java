/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.etasdk.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.eTilbudsavis.etasdk.Network.EtaError;

public class EtaLog {

	public static final String TAG = "EtaLog";
	
	/** Variable controlling whether messages are printed. Set to true to print messages */
	public static boolean DEBUG = false;
	
	/** Variable to control the size of the exception log */
	public static final int DEFAULT_EXCEPTION_LOG_SIZE = 16;
	
	/** Variable determining the state of logging */
	private static boolean mEnableLogHistory = false;
	
	/** The log containing all exceptions, that have been printed via {@link #e(String, Exception) } */
	private static final EventLog mExceptionLog = new EventLog(DEFAULT_EXCEPTION_LOG_SIZE);
	
	/**
	 * Print a debug log message to LogCat.
	 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
	 * @param message The message you would like logged.
	 */
	public static void d(String tag, String message) {
		if (!DEBUG) {
			return;
		}
		Log.d(tag, (message == null ? "null" : message) );
	}
	
	/**
	 * Print a debug log message to LogCat.
	 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
	 * @param e The EtaError you would like logged.
	 */
	public static void e(String tag, EtaError e) {
		if (!DEBUG) {
			return;
		}
		if (mEnableLogHistory) {
			addLog(e);
		}
		d(tag, e.toJSON().toString());
	}
	
	/**
	 * Print a debug log message to LogCat.
	 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
	 * @param e The exception you would like logged.
	 */
	public static void e(String tag, Exception e) {
		e(tag, (Throwable)e);
	}
	
	/**
	 * Print a debug log message to LogCat.
	 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
	 * @param t The throwable you would like logged.
	 */
	public static void e(String tag, Throwable t) {
		if (!DEBUG) {
			return;
		}
		if (mEnableLogHistory) {
			addLog(t);
		}
		t.printStackTrace(); 
	}
	
	/**
	 * Adds the throwable to the {@link #mExceptionLog Exception Log}.
	 * @param t The throwable to add
	 */
	private static void addLog(Throwable t) {
		
		if (!mEnableLogHistory) {
			return;
		}
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		String stacktrace = sw.toString();
		JSONObject log = new JSONObject();
		try {
			log.put("exception", t.getClass().getName());
			log.put("stacktrace", stacktrace);
			mExceptionLog.add(EventLog.TYPE_EXCEPTION, log);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Send a DEBUG log message. This method will allow messages above the usual Log.d() limit of 4000 chars.
	 * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
	 * @param message The message you would like logged.
	 */
	public static void dAll(String tag, String message) {
		if (!DEBUG) {
			return;
		}
		
		if (message.length() > 4000) {
		    int chunkCount = message.length() / 4000;     // integer division
	        String chunk = "[chunk %s/%s] %s";
		    for (int i = 0; i <= chunkCount; i++) {
		        int max = 4000 * (i + 1);
		        int end = (max >= message.length()) ? message.length() : max;
		        d(tag, String.format(chunk, i, chunkCount, message.substring(4000 * i, end) ) );
		    }
		} else {
			d(tag,message);
		}
		
	}
	
	/**
	 * Print a debug log message to LogCat.
	 * @param tag A tag
	 * @param name A name identifying this print
	 * @param response A {@link JSONObject} (Eta SDK response), this may be {@code null}
	 * @param error An {@link EtaError}, this may be {@code null}
	 */
	public static void d(String tag, String name, JSONObject response, EtaError error) {
		String resp = response == null ? "null" : response.toString();
		d(tag, name, resp, error);
	}
	
	/**
	 * Print a debug log message to LogCat.
	 * @param tag A tag
	 * @param name A name identifying this print
	 * @param response A {@link JSONArray} (Eta SDK response), this may be {@code null}
	 * @param error An {@link EtaError}, this may be {@code null}
	 */
	public static void d(String tag, String name, JSONArray response, EtaError error) {
		String resp = response == null ? "null" : ("size:" + response.length());
		d(tag, name, resp, error);
	}

	/**
	 * Print a debug log message to LogCat.
	 * @param tag A tag
	 * @param name A name identifying this print
	 * @param response A {@link String} (Eta SDK response), this may be {@code null}
	 * @param error An {@link EtaError}, this may be {@code null}
	 */
	public static void d(String tag, String name, String response, EtaError error) {
		if (!DEBUG) {
			return;
		}
		String e = error == null ? "null" : error.toJSON().toString();
		String s = response == null ? "null" : response;
		Log.d(tag, name + ": Response[" + s + "], Error[" + e + "]");
	}
	
	/**
	 * Enabling of log messages to Log.d(). All SDK error messages will be
	 * printed via {@link #com.eTilbudsavis.etasdk.Utils.EtaLog EtaLog}
	 * and you must therefore enable logging manually under development, to
	 * get relevant messages, and errors. <br><br>
	 * But please be aware to disable this in production, as there is no
	 * guarantee as to what may be printed in this log (e.g.: usernames and passwords)
	 * 
	 * @param enable true to have messages printed to Log.d()
	 */
	public static void enableLogd(boolean enable) {
		DEBUG = enable;
	}
	
	/**
	 * If exception log is enables, all exceptions will be saved, and can be used later for debugging purposes.
	 * This is especially useful under development, as all errors from the SDK can be logged to this log.
	 * @param enable true to save exceptions, else false
	 */
	public static void enableExceptionHistory(boolean enable) {
		mEnableLogHistory = enable;
	}
	
	/**
	 * Get all exceptions posted to the log.
	 * @return the exception log
	 */
	public static EventLog getExceptionLog() {
		return mExceptionLog;
	}
	
	/**
	 * Print a StackTrace from any given point of your source code.
	 */
	public static void printStackTrace() {
		if (!DEBUG) {
			return;
		}
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			System.out.println(ste);
		}
	}
	
	
}
