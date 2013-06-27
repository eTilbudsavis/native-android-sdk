package com.eTilbudsavis.sdkdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.util.Log;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.Tools.Utilities;

public final class Tools {

	private static final String	TAG = "Tools";

	private static final boolean mDebug = true;
	
	/**
	 * A proxy for Log.d API that silences log messages in release.
	 *
	 * @param tag Used to identify the source of a log message. It usually
	 *			identifies the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	public static void logd(String tag, String msg) {
		if (mDebug)
			Log.d(tag, msg);
	}	

	public static void logd(String tag, int statusCode, Object object) {
		if (mDebug)
			Log.d(tag, "Status: " + String.valueOf(statusCode) + ", Data: " + object.toString());
	}

	public static void logd(String tag, String name, int statusCode, Object data, EtaError error) {
		if (mDebug) {
			Log.d(tag, name + " - " + (Utilities.isSuccess(statusCode) ? data.toString() : error.toString()));
		}
	}


	
}
