package com.eTilbudsavis.etasdk;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.eTilbudsavis.etasdk.log.EtaLog;

public class DefaultThreadFactory implements ThreadFactory {
	
	public static final String TAG = Eta.TAG_PREFIX + DefaultThreadFactory.class.getSimpleName();
	
	private static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY-1;
	
	private static final AtomicInteger poolNumber = new AtomicInteger(1);
	
	private static UncaughtExceptionHandler mExceptionHandler = new UncaughtExceptionHandler() {
		
		public void uncaughtException(Thread thread, Throwable ex) {
			EtaLog.e(TAG, thread.getName() + " crashed", ex);
		}
	};
	
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;
	private final int threadPriority;
	
	public DefaultThreadFactory(String threadNamePrefix) {
		this(DEFAULT_THREAD_PRIORITY, threadNamePrefix);
	}
	
	public DefaultThreadFactory(int threadPriority, String threadNamePrefix) {
		this.threadPriority = threadPriority;
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + "-thread-";
	}
	
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon()) t.setDaemon(false);
		t.setPriority(threadPriority);
//		t.setUncaughtExceptionHandler(mExceptionHandler);
		return t;
	}
	
}