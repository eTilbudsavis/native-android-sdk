package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request;

class SyncLog {

    private static final boolean LOG_SYNC = true;
    private static final boolean LOG = false;

    public static int syncLooper(String tag, int count, String msg) {
        return (LOG_SYNC ? SgnLog.v(tag, "SyncManager(" + count + ") - " + msg) : 0);
    }

    public static int sync(String tag, String msg) {
        return (LOG_SYNC ? SgnLog.v(tag, msg) : 0);
    }

    public static int log(String tag, String msg) {
        return (LOG ? SgnLog.v(tag, msg) : 0);
    }

    public static boolean isPull(Request r) {
        return r.getUrl().contains("modified") || r.getUrl().endsWith("shoppinglists") || r.getUrl().endsWith("items");
    }

}
