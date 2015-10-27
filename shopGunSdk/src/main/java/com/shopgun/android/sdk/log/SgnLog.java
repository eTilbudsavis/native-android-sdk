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

package com.shopgun.android.sdk.log;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;


public class SgnLog {

    public static final String TAG = Constants.getTag(SgnLog.class);

    private static final String LOG_D_CHUNK = "[chunk %s/%s] %s";

    private static SgnLogger mLogger = new DefaultLogger();

    public static SgnLogger getLogger() {
        return mLogger;
    }

    public static void setLogger(SgnLogger l) {
        mLogger = l;
    }

    public static int v(String tag, String msg) {
        return mLogger.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return mLogger.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return mLogger.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return mLogger.d(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return mLogger.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return mLogger.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return mLogger.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return mLogger.w(tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        return mLogger.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return mLogger.e(tag, msg, tr);
    }

    /**
     * Send a DEBUG log message. This method will allow messages above the usual Log.d() limit of 4000 chars.
     *
     * @param tag     Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The message you would like logged.
     */
    public static void dAll(String tag, String message) {

        if (message.length() > 4000) {
            int chunkCount = message.length() / 4000;     // integer division

            for (int i = 0; i <= chunkCount; i++) {
                int max = 4000 * (i + 1);
                int end = (max >= message.length()) ? message.length() : max;
                d(tag, String.format(LOG_D_CHUNK, i, chunkCount, message.substring(4000 * i, end)));
            }
        } else {
            d(tag, message);
        }

    }

    /**
     * Print a StackTrace from any given point of your source code.
     * @param tag     Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     */
    public static void printStackTrace(String tag) {
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            d(tag, String.valueOf(ste));
        }
    }

    /**
     * Print a StackTrace from any given point of your source code.
     * @param tag     Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     */
    public static void printParentMethod(String tag) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        d(tag, String.valueOf(trace[4]));
    }

    /**
     * Convert an {@link Throwable} to a {@link JSONObject}
     * @param t The throwable to add
     * @return A {@link JSONObject} representation of the given {@link Throwable}
     */
    public static JSONObject exceptionToJson(Throwable t) {

        JSONObject log = new JSONObject();
        try {
            log.put("exception", t.getClass().getName());
            log.put("stacktrace", Utils.exceptionToString(t));
            return log;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }


}
