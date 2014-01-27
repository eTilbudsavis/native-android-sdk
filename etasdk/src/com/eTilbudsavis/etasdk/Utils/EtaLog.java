package com.eTilbudsavis.etasdk.Utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.SystemClock;
import android.util.Log;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;

public class EtaLog {

	public static final String TAG = "EtaLog";
	
	public static void d(String tag, String message) {
		if (!Eta.DEBUG_LOGD) return;
		Log.d(tag, message);
	}

	public static void d(String tag, Exception e) {
		if (!Eta.DEBUG_LOGD) return;
		e.printStackTrace(); 
	}
	
	public static void d(String tag, Throwable t) {
		if (!Eta.DEBUG_LOGD) return;
		t.printStackTrace(); 
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
	
	public static void printStackTrace() {
		if (!Eta.DEBUG_LOGD) return;
			for (StackTraceElement ste : Thread.currentThread().getStackTrace())
				System.out.println(ste);
	}
	
	public static class EventLog {

		public static final String TAG = "EventLog";
		
		List<Event> mEvents = new ArrayList<Event>(0);

		public void add(String name) {
			add(new Event(name, SystemClock.elapsedRealtime(), Thread.currentThread().getName()));
		}
		
		public void add(Event e) {
			mEvents.add(e);
		}
		
		public void print(String name) {
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