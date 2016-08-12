/*******************************************************************************
 * Copyright 2015 ShopGun
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
 ******************************************************************************/

package com.shopgun.android.sdk.utils;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SgnThreadFactory implements ThreadFactory {

    public static final String TAG = Constants.getTag(SgnThreadFactory.class);

    private static final String DEFAULT_THREAD_NAME = "sgn-sdk-";
    private static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

    private static final AtomicInteger poolNumber = new AtomicInteger(1);

//	private static UncaughtExceptionHandler mExceptionHandler = new UncaughtExceptionHandler() {
//		
//		public void uncaughtException(Thread thread, Throwable ex) {
//			SgnLog.e(TAG, thread.getName() + " crashed", ex);
//		}
//	};

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final int threadPriority;

    public SgnThreadFactory() {
        this(DEFAULT_THREAD_NAME);
    }

    public SgnThreadFactory(String threadNamePrefix) {
        this(DEFAULT_THREAD_PRIORITY, threadNamePrefix);
    }

    public SgnThreadFactory(int threadPriority, String threadNamePrefix) {
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