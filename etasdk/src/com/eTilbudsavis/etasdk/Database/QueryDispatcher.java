package com.eTilbudsavis.etasdk.Database;

import java.util.concurrent.BlockingQueue;

import android.os.Process;

@SuppressWarnings("rawtypes")
public class QueryDispatcher extends Thread {
	
	private final BlockingQueue<DbQuery> mQueue;
	private boolean mQuit = false;
	
	public QueryDispatcher(BlockingQueue<DbQuery> queue) {
		mQueue = queue;
	}

	@Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        DbQuery query;
        while (true) {
            try {
                // Take a request from the queue.
            	query = mQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }
            
            query.run();
            
        }
    }
	
}
