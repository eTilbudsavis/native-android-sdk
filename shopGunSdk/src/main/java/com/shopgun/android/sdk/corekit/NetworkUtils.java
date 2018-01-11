package com.shopgun.android.sdk.corekit;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class NetworkUtils {

    public static void cancelCallWithTag(OkHttpClient client, Object tag) {
        // A call may transition from queue -> running. Remove queued Calls first.
        for(Call call : client.dispatcher().queuedCalls()) {
            if(call.request().tag().equals(tag)) {
                call.cancel();
            }
        }
        for(Call call : client.dispatcher().runningCalls()) {
            if(call.request().tag().equals(tag)) {
                call.cancel();
            }
        }
    }

    public static void cancelAll(OkHttpClient client) {
        // A call may transition from queue -> running. Remove queued Calls first.
        for(Call call : client.dispatcher().queuedCalls()) {
            call.cancel();
        }
        for(Call call : client.dispatcher().runningCalls()) {
            call.cancel();
        }
    }

}
