package com.eTilbudsavis.etasdk.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemClock;
import android.util.Log;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;

public class EtaLog {

	public static final String TAG = "EtaLog";
	
	private static boolean mEnableLogHistory = false;
	private static final List<JSONObject> mExceptionHistory = Collections.synchronizedList(new ArrayList<JSONObject>());
	
	public static void d(String tag, String message) {
		if (!Eta.DEBUG_LOGD) return;
		Log.d(tag, message);
	}

	public static void d(String tag, Exception e) {
		d(tag, (Throwable)e);
	}
	
	public static void d(String tag, Throwable t) {
		if (!Eta.DEBUG_LOGD) return;
		addLog(t);
		t.printStackTrace(); 
	}
	
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
			mExceptionHistory.add(log);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void dAll(String tag, String message) {
		if (!Eta.DEBUG_LOGD) return;
		
		if (message.length() > 4000) {
		    int chunkCount = message.length() / 4000;     // integer division
		    for (int i = 0; i <= chunkCount; i++) {
		        int max = 4000 * (i + 1);
		        if (max >= message.length()) {
		            d(tag, "chunk " + i + " of " + chunkCount + ":" + message.substring(4000 * i));
		        } else {
		            d(tag, "chunk " + i + " of " + chunkCount + ":" + message.substring(4000 * i, max));
		        }
		    }
		} else {
			d(tag,message);
		}
		
	}
	
	public static void d(String tag, String name, JSONObject response, EtaError error) {
		if (!Eta.DEBUG_LOGD) return;
		Log.d(tag, name + ": Response: " + (response == null ? "null" : "Success") + ", Error: " + (error == null ? "null" : error.toJSON().toString()));
	}

	public static void d(String tag, String name, JSONArray response, EtaError error) {
		if (!Eta.DEBUG_LOGD) return;
		Log.d(tag, name + ": Response: " + (response == null ? "null" : "Success") + ", Error: " + (error == null ? "null" : error.toJSON().toString()));
	}
	
	public static void enableExceptionHistory(boolean enable) {
		mEnableLogHistory = enable;
	}
	
	public static List<JSONObject> getExceptionHistory() {
		return mExceptionHistory;
	}
	
	public static void printStackTrace() {
		if (!Eta.DEBUG_LOGD) return;
			for (StackTraceElement ste : Thread.currentThread().getStackTrace())
				System.out.println(ste);
	}
	
	public static class EventLog {

		public static final String TAG = "EventLog";
		
		List<Event> mEvents = new ArrayList<Event>(0);
		JSONObject mSummary;
		
		public void add(String name) {
			add(new Event(name, SystemClock.elapsedRealtime(), Thread.currentThread().getName()));
		}
		
		public void add(Event e) {
			mEvents.add(e);
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
		
		public void printSummary() {
			d(TAG, getSummary().toString());
		}
		
		public void printEventLog(String name) {
			d(TAG, getString(name));
		}
		
		public String getString(String name) {
			if (mEvents.isEmpty())
				return String.format("[%+6d ms] %s", 0, name);
			
			StringBuilder sb = new StringBuilder();
			long prevTime = mEvents.get(0).time;
			sb.append(String.format("     [%+6d ms] %s", getTotalDuration(), name)).append("\n");
			for (int i = 0; i < mEvents.size() ; i++) {
				Event e = mEvents.get(i);
				long thisTime = e.time;
				sb.append(String.format("[%2d] [%+6d ms] %s", i,(thisTime - prevTime), e.name)).append("\n");
				prevTime = thisTime;
			}
			return sb.toString();
		}
		
		public long getTotalDuration() {
			if (mEvents.size() == 0) {
				return 0;
			}

			long first = mEvents.get(0).time;
			long last = mEvents.get(mEvents.size() - 1).time;
			return last - first;
		}
		
		public class Event {

			public final String name;
			public final long time;
			public final String thread;

			public Event(String name, long time, String thread) {
				this.name = name;
				this.time = time;
				this.thread = thread;
			}

		}

	}
}