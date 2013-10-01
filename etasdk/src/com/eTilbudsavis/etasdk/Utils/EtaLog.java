package com.eTilbudsavis.etasdk.Utils;

import java.util.ArrayList;
import java.util.List;

import com.eTilbudsavis.etasdk.Eta;

import android.os.SystemClock;
import android.util.Log;

public class EtaLog {
	
	public static final String TAG = "EtaLog";

    public static void d(String tag, String message) {
    	if (Eta.DEBUG) {
            Log.d(tag, message);
    	}
    }

    public static void d(String tag, Exception e) {
    	if (Eta.DEBUG) {
    		e.printStackTrace();
    	}
    }
    
	public class EventLog {

		public static final String TAG = "EventLog";
		
		List<Event> mEvents = new ArrayList<Event>(0);
		
		public void add(String name) {
			mEvents.add(new Event(name, SystemClock.elapsedRealtime()));
		}
		
		public void print(String name) {
			
            long prevTime = mEvents.get(0).time;
            d(TAG, String.format("(%-4d ms) %s", getTotalDuration(), name));
            for (Event e : mEvents) {
                long thisTime = e.time;
                d(TAG, String.format("(+%-4d) %s", (thisTime - prevTime), e.name));
                prevTime = thisTime;
            }
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
			
			public Event(String name, long time) {
				this.name = name;
				this.time = time;
			}
			
		}
		
	}
}
