package com.shopgun.android.sdk.log;

import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.utils.log.LogCatLogger;
import com.shopgun.android.utils.log.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Static class for easy access to logging with {@link Logger}
 */
public class SgnLog {

    private static volatile Logger mLogger = new LogCatLogger(0);

    public static void setLogger(Logger logger) {
        mLogger = (logger == null ? new LogCatLogger(0) : logger);
    }

    public static Logger getLogger() {
        return mLogger;
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

    public static int d(String tag, Request request, List<?> response, List<ShopGunError> errors) {
        return d(tag, request.getUrl(), response, errors);
    }

    public static int d(String tag, String msg, List<?> response, List<ShopGunError> errors) {
        return d(tag, msg, getResponseString(response, errors));
    }

    public static int d(String tag, Request request, JSONObject response, ShopGunError error) {
        return d(tag, request.getUrl(), (response == null ? null : response.toString()), error);
    }

    public static int d(String tag, String msg, JSONObject response, ShopGunError error) {
        return d(tag, msg, (response == null ? null : response.toString()), error);
    }

    public static int d(String tag, Request request, JSONArray response, ShopGunError error) {
        return d(tag, request.getUrl(), (response == null ? null : response.toString()), error);
    }

    public static int d(String tag, String msg, JSONArray response, ShopGunError error) {
        return d(tag, msg, (response == null ? null : response.toString()), error);
    }

    public static int d(String tag, String msg, String response, ShopGunError error) {
        return d(tag, msg, (error == null ? response : error.toString()));
    }

    private static int d(String tag, String msg, String response) {
        return d(tag, msg + " - " + response);
    }

    private static String getResponseString(List<?> response, List<ShopGunError> errors) {
        StringBuilder sb = new StringBuilder();
        sb.append("response: ");
        if (errors.isEmpty()) {
            if (response.isEmpty()) {
                sb.append("no results");
            } else {
                sb.append(response.size());
                sb.append(" ");
                sb.append(response.get(0).getClass().getSimpleName());
                if (response.size() > 1) {
                    sb.append("'s");
                }
            }
        } else {
            for (ShopGunError e : errors) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(e.toString());
            }
        }
        return sb.toString();
    }

}
