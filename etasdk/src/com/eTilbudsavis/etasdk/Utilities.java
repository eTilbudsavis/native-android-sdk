/**
 * @fileoverview	Utilities.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * @version			0.0.1
 */
package com.eTilbudsavis.etasdk;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public final class Utilities {
	/**
	 * Set this to true to enable log output. Remember to turn this back off
	 * before realeasing.
	 */
	private static boolean ENABLE_LOG = true;
	
	/**
	 * Create universally unique identifier.
	 *
	 * @return Universally unique identifer (UUID).
	 */
	public static String createUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	/**
	 * A proxy for Log.d API that silences log messages in release.
	 *
	 * @param tag Used to identify the source of a log message. It usually
	 *			identifies the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	public static void logd(String tag, String msg) {
		if (ENABLE_LOG)
			Log.d(tag, msg);
	}
	
	/**
	 * Builds a MD5 checksum of a NameValuePair
	 * of parameters to be included in an API call. 
	 *
	 * @param nameValuePair NameValuePair containing the name/value pairs
	 * @param api_secret The SDK apiSecret
	 * @return
	 */
	public static String buildChecksum(List<NameValuePair> nameValuePair, String api_secret) {

		StringBuilder sb = new StringBuilder();
		for (NameValuePair nvp : nameValuePair) {
			sb.append(nvp.getValue());
		}
		sb.append(api_secret);
		
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(sb.toString().getBytes());
			StringBuffer sbMD5 = new StringBuffer();

			for (int i = 0; i < array.length; ++i) {
				sbMD5.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
						.substring(1, 3));
			}

			return sbMD5.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
		}

		return null;
	}
	
	/**
	 * Add any object to a NameValuePair.
	 * The Object will be cast to string and URLEncoded.
	 * @param nameValuePair NameValuePair to add data to
	 * @param name
	 * @param value
	 * @return
	 */
	public static void putNameValuePair(List<NameValuePair> nameValuePair, String name, Object value) {
		nameValuePair.add(new BasicNameValuePair(name, value.toString()));
	}
	
	/**
	 * Builds the block of JavaScript parameters for injecting into a WebView.
	 *
	 * @param data The LinkedHashMap for processing
	 * @return A JavaScript formatted String ready for insertion
	 */
	@SuppressWarnings("unchecked")
	public static String buildJSString(LinkedHashMap<String, Object> data) {
		boolean first = true;
		Iterator<String> iterator = data.keySet().iterator();

		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		while (iterator.hasNext()) {
			String s = iterator.next();

			if (!first) sb.append(", ");

			if (data.get(s).getClass() == data.getClass()) {
				sb.append(s);
				sb.append(": ");
				sb.append( buildJSString( (LinkedHashMap<String, Object>)data.get(s) ) );
			} else {
				sb.append(s);
				sb.append(": '");
				sb.append(data.get(s).toString());
				sb.append("'");
			}

			first = false;				
		}
		sb.append(" }");
		return sb.toString();
	}
	
	/**
	 * UTC time in seconds (time since 01.01.1970).
	 *
	 * @return int Time in seconds
	 */
	public static int getTime() {
		return Integer.valueOf(String.valueOf(System.currentTimeMillis()/1000));
	}
	
}