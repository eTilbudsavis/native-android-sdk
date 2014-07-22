package com.eTilbudsavis.etasdk;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultThreadFactory implements ThreadFactory {
	
	public static final String TAG = Eta.TAG_PREFIX + DefaultThreadFactory.class.getSimpleName();
	
	private static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY-1;
	
	private static final AtomicInteger poolNumber = new AtomicInteger(1);
	
	private final ThreadGroup group;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private final String namePrefix;
	private final int threadPriority;
	
	private DefaultThreadFactory() {
		this(DEFAULT_THREAD_PRIORITY, "eta-");
	}
	
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
		return t;
	}
	
}