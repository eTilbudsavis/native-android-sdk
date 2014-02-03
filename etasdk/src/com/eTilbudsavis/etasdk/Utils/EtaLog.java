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
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Param;

public class EtaLog {

	public static final String TAG = "EtaLog";
	
	public static final int DEFAULT_EXCEPTION_LOG_SIZE = 64;
	
	private static boolean mEnableLogHistory = false;
	private static final EventLog mExceptionLog = new EventLog(DEFAULT_EXCEPTION_LOG_SIZE);
	
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
			mExceptionLog.add(EventLog.TYPE_EXCEPTION, log);
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
		d(tag, name, (response == null ? "null" : response.toString()), error);
	}

	public static void d(String tag, String name, JSONArray response, EtaError error) {
		
		if (response == null) {
			d(tag, name, "null", error);
			return;
		}
		
		if (response.length() == 0) {
			d(tag, name, response.toString(), error);
			return;
		}
		
		String data = null;

		try {
			
			if (response.get(0) instanceof JSONObject && response.getJSONObject(0).has(Param.ERN) ) {
	
				JSONArray tmp = new JSONArray();
				for (int i = 0 ; i < response.length() ; i++ ) {
					JSONObject o = response.getJSONObject(i);
					if (o.has(Param.ERN)) {
						tmp.put(o.getString(Param.ERN));
					} else {
						tmp.put("non-ern-object-in-list");
					}
				}
				data = tmp.toString();
	
			} else {
				data = response.toString();
			}
		

		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
		d(tag, name, data, error);
		
	}
	
	public static void d(String tag, String name, String response, EtaError error) {
		if (!Eta.DEBUG_LOGD) return;
		String e = error == null ? "null" : error.toJSON().toString();
		String s = response == null ? "null" : response;
		Log.d(tag, name + ": Response: " + s + ", Error: " + e );
	}
	
	public static void enableExceptionHistory(boolean enable) {
		mEnableLogHistory = enable;
	}
	
	public static EventLog getExceptionLog() {
		return mExceptionLog;
	}
	
	public static void printStackTrace() {
		if (!Eta.DEBUG_LOGD) return;
			for (StackTraceElement ste : Thread.currentThread().getStackTrace())
				System.out.println(ste);
	}
	
	public static class EventLog {
		
		public static final String TYPE_REQUEST = "request";
		public static final String TYPE_EXCEPTION = "exception";
		public static final String TYPE_VIEW = "view";
		public static final String TYPE_LOG = "log";
		
		public static final String TAG = "EventLog";
		
		List<Event> mEvents;
		JSONObject mSummary;
		
		public EventLog() {
			mEvents = Collections.synchronizedList(new ArrayList<EtaLog.EventLog.Event>());
		}
		
		public EventLog(int logSize) {
			mEvents = Collections.synchronizedList(new FixedArrayList<EtaLog.EventLog.Event>(logSize));
		}
		
		public void add(String name) {
			add(name, null, null);
		}
		
		public void add(String type, JSONObject data) {
			add(type, type, data);
		}
		
		private void add(String name, String type, JSONObject data) {
			long time = System.currentTimeMillis();
			String user = Eta.getInstance().getUser().getErn();
			String token = Eta.getInstance().getSessionManager().getSession().getToken();
			add(new Event(name, time, type, user, token, data));
		}
		
		public void add(Event e) {
			mEvents.add(e);
		}
		
		public List<Event> getEvents() {
			return mEvents;
		}
		
		public void clear() {
			mEvents.clear();
		}
		
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
		
		public void printSummary() {
			d(TAG, getSummary().toString());
		}
		
		/**
		 * Prints the timing of events.<br>
		 * This is 
		 * @param name
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
		
		public JSONArray toJSON() {
			return toJSON(mEvents);
		}
		
		public static JSONArray toJSON(List<Event> events) {
			JSONArray jArray = new JSONArray();
			if (events != null && !events.isEmpty()) {
				for (Event e : events) {
					jArray.put(e.toJSON());
				}
			}
			return jArray;
		}
		
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
			
			public JSONObject toJSON() {
				JSONObject o = new JSONObject();
				try {
					o.put("timestamp", Utils.formatDate(new Date(time)));
					o.put("type", type);
					o.put("token", token);
					o.put("userid", user);
					o.put("name", name);
					o.put("data", data);
				} catch (JSONException e) {
					d(TAG, e);
					
				}
				return o;
			}
			
		}

	}
}