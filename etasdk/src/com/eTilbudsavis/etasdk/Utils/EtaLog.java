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
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.FixedArrayList;
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
	
	/**
	 * EventLog class have been created to simplify logging within the ETA SDK.
	 * You will notice that this class is being used extensively throughout the SDK.
	 * And have been especially useful, while debugging networking issues.
	 * 
	 * @author Danny Hvam - danny@etilbudsavis.dk
	 *
	 */
	public static class EventLog {

		public static final String TAG = "EventLog";
		
		public static final String TYPE_REQUEST = "request";
		public static final String TYPE_EXCEPTION = "exception";
		public static final String TYPE_VIEW = "view";
		public static final String TYPE_LOG = "log";
		
		List<Event> mEvents;
		JSONObject mSummary;
		
		/**
		 * Create a new EventLog, with no size limitations. Please be aware that
		 * these logs can grow to a considerable size, and may use an unreasonable
		 * amount of memory, and is therefore only recommended under development.
		 */
		public EventLog() {
			mEvents = Collections.synchronizedList(new ArrayList<EtaLog.EventLog.Event>());
		}
		
		/**
		 * Create a new log with a fixed size. The EventLig will be using FIFO ordering of events.
		 * @param logSize the desired size of the log
		 */
		public EventLog(int logSize) {
			mEvents = Collections.synchronizedList(new FixedArrayList<EtaLog.EventLog.Event>(logSize));
		}
		
		/**
		 * Add a new Event to the log, based simply on a name. This is nice for tracing
		 * when, where and in what order the events have occurred.
		 * @param name of the log entry
		 */
		public void add(String name) {
			add(name, null, null);
		}
		
		/**
		 * Add a new Event to the log. Adding a special type, is nice for saving generic error correcting data
		 * to a log. This can be {@link #TYPE_VIEW view}, {@link #TYPE_EXCEPTION exception}, or {@link #TYPE_REQUEST request} events.
		 * but essentially any string will do.
		 * @param type of event to add
		 * @param data data accompanying the event
		 */
		public void add(String type, JSONObject data) {
			add(type, type, data);
		}
		
		/**
		 * 
		 * @param name
		 * @param type
		 * @param data
		 */
		private void add(String name, String type, JSONObject data) {
			long time = System.currentTimeMillis();
			String user = Eta.getInstance().getUser().getErn();
			String token = Eta.getInstance().getSessionManager().getSession().getToken();
			add(new Event(name, time, type, user, token, data));
		}
		
		/**
		 * Add a new Event to the log.
		 * @param e event to add
		 */
		public void add(Event e) {
			mEvents.add(e);
		}
		
		/**
		 * Get the current list of events
		 * @return
		 */
		public List<Event> getEvents() {
			return mEvents;
		}
		
		/**
		 * Clear the current list of events in the EventLog
		 */
		public void clear() {
			mEvents.clear();
		}
		
		/**
		 * Get all logs of a given type
		 * @param type of event to get
		 * @return a list of events
		 */
		public List<Event> getType(String type) {
			List<Event> tmp = new ArrayList<EtaLog.EventLog.Event>();
			for (Event e : mEvents) {
				if (e.type.equals(type)) {
					tmp.add(e);
				}
			}
			return null;
		}
		
		/**
		 * Summary can be any information you want.
		 * @param summary
		 */
		public void setSummary(JSONObject summary) {
			mSummary = summary;
		}
		
		/**
		 * Returns the EventLog summary, there are no specifics on what a summary can or may contain.
		 * @return
		 */
		public JSONObject getSummary() {
			return mSummary == null ? new JSONObject() : mSummary;
		}

		/**
		 * Print the summary data for this EventLog
		 */
		public void printSummary() {
			d(TAG, getSummary().toString());
		}
		
		/**
		 * Print the summary data for this EventLog
		 * @param indentSpaces the number of spaces to indent for each level of nesting.
		 */
		public void printSummary(int indentSpaces) {
			try {
				d(TAG, getSummary().toString(indentSpaces));
			} catch (JSONException e) {
				e(TAG, e);
			}
		}
		
		/**
		 * Prints the timing of events in this EventLog
		 * @param name to use as print prefix
		 */
		public void printEventLog(String name) {
			
			if (mEvents.isEmpty()) {
				d(TAG, String.format("[%+6d ms] %s", 0, name));
			}
			
			StringBuilder sb = new StringBuilder();
			long prevTime = mEvents.get(0).time;
			sb.append(String.format("     [%+6d ms] %s", getTotalDuration(), name)).append("\n");
			for (int i = 0; i < mEvents.size() ; i++) {
				Event e = mEvents.get(i);
				sb.append(String.format("[%2d] [%+6d ms] %s", i,(e.time - prevTime), e.name)).append("\n");
				prevTime = e.time;
			}
			
			d(TAG, sb.toString());
		}
		
		public JSONArray toJSON(boolean rawTime) {
			return toJSON(mEvents, rawTime);
		}
		
		/**
		 * Create a JSONArray of the currents events
		 * @param events to torn into a JSONArray
		 * @return a JSONArray
		 */
		public static JSONArray toJSON(List<Event> events, boolean rawTime) {
			JSONArray jArray = new JSONArray();
			if (events != null && !events.isEmpty()) {
				for (Event e : events) {
					jArray.put(e.toJSON(rawTime));
				}
			}
			return jArray;
		}
		
		/**
		 * Get the total duration from the first Event were recorded till the last one.
		 * @return a non-negative number
		 */
		public long getTotalDuration() {
			if (mEvents.isEmpty()) {
				return 0;
			}
			
			long first = mEvents.get(0).time;
			long last = mEvents.get(mEvents.size() - 1).time;
			return last - first;
			
		}

		public static Comparator<Event> timestamp  = new Comparator<Event>() {
			
			public int compare(Event e1, Event e2) {
				
				if (e1 == null || e2 == null) {
					return e1 == null ? (e2 == null ? 0 : 1) : -1;
				} else {
					return e1.time < e2.time ? -1 : 1;
				}
				
			}

		};
		
		/**
		 * Simple helper class, for usage in EventLog
		 * @author Danny Hvam - danny@etilbudsavis.dk
		 *
		 */
		public class Event {
			
			public final long time;
			public final String type;
			public final String token;
			public final String user;
			public final String name;
			public final JSONObject data;
			
			public Event(String name, long time, String type, String user, String token, JSONObject data) {
				this.name = name == null ? (type == null ? "unknown" : type) : name;
				this.time = time;
				this.type = type;
				this.user = user;
				this.token = token;
				this.data = data;
			}

			public JSONObject toJSON(boolean rawTime) {
				JSONObject o = new JSONObject();
				try {
					o.put("timestamp", (rawTime ? time : Utils.parseDate(new Date(time))) );
					o.put("type", type);
					o.put("token", token);
					o.put("userid", user);
					o.put("name", name);
					o.put("data", data);
				} catch (JSONException e) {
					e(TAG, e);
					
				}
				return o;
			}
			
		}

	}
}
