package com.eTilbudsavis.etasdk.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.eTilbudsavis.etasdk.Eta;

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
		
		/**
		 * Prints a human readable string to Log.d<br>
		 * This method can be called multiple times, with different event names
		 * @param eventName of the log or event
		 */
		public void print(String eventName) {
			d(TAG, getString(eventName));
		}
		
		/**
		 * Returns a human readable string containing all events in this log, along with timings
		 * @param eventName of the log or event
		 * @return
		 */
		public String getString(String eventName) {
			if (mEvents.isEmpty()) {
				return String.format("[%+6d ms] %s", 0, eventName);
			}
			
			StringBuilder sb = new StringBuilder();
			long prevTime = mEvents.get(0).time;
			sb.append(String.format("     [%+6d ms] %s", getTotalDuration(), eventName)).append("\n");
			for (int i = 0; i < mEvents.size() ; i++) {
				Event e = mEvents.get(i);
				long thisTime = e.time;
				sb.append(String.format("[%2d] [%+6d ms] %s", i,(thisTime - prevTime), e.name)).append("\n");
				prevTime = thisTime;
			}
			return sb.toString();
		}
		
		private long getTotalDuration() {
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
	
	public static boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    return Environment.MEDIA_MOUNTED.equals(state);
	}
	
	/**
	 * Writes a file to root of external storage <br><br>
	 * 
	 * Requires permission: android.permission.WRITE_EXTERNAL_STORAGE
	 * 
	 * @param fileContents
	 * @param fileName
	 */
	public static void writeToFile(String fileName, String fileContents) {

		if (!isExternalStorageWritable())
			return;
		
        try {
        	File f = new File(android.os.Environment.getExternalStorageDirectory(), fileName);
            FileWriter out = new FileWriter(f);
            out.write(fileContents);
            out.close();
        } catch (IOException e) {
        	d(TAG, e);
        }
    }
	
}