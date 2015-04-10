/**
 * @fileoverview	Utilities.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * @version			0.0.1
 */
package com.eTilbudsavis.etasdk.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.network.Request;

public final class Utils {
	
	public static final String TAG = Constants.getTag(Utils.class);
	
	/** A second in milliseconds */
	public static final long SECOND_IN_MILLIS = 1000;

	/** A minute in milliseconds */
	public static final long MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;

	/** A hour in milliseconds */
	public static final long HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;

	/** A day in milliseconds */
	public static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;

	/** A week in milliseconds */
	public static final long WEEK_IN_MILLIS = DAY_IN_MILLIS * 7;

	/** A month in milliseconds */
	public static final long MONTH_IN_MILLIS = DAY_IN_MILLIS * 30;

	/** A year in milliseconds */
	public static final long YEAR_IN_MILLIS = WEEK_IN_MILLIS * 52;
	
	/** The date format as returned from the server */
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ";
	
	/** String representation of epoc */
	public static final String DATE_EPOC = "1970-01-01T00:00:00+0000";

	/** Single instance of SimpleDateFormat to save time and memory */
	private static SimpleDateFormat mSdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

	static {
		
	}
	
	private Utils() {
	}
	
	/**
	 * Create universally unique identifier.
	 *
	 * @return Universally unique identifier (UUID).
	 */
	public static String createUUID() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * Builds a url + query string.<br>
	 * e.g.: https://api.etilbudsavis.dk/v2/catalogs?order_by=popular
	 * @param r to build from
	 * @return
	 */
	public static String requestToUrlAndQueryString(Request<?> r) {
		if (r==null || r.getUrl()==null) {
			return null;
		}
		if (r.getParameters() == null || r.getParameters().isEmpty()) {
			return r.getUrl();
		}
		return r.getUrl() + "?" + mapToQueryString(r.getParameters(), r.getParamsEncoding());
	}

	/**
	 * Returns a string of parameters, ordered alfabetically (for better cache performance)
	 * @param apiParams to convert into query parameters
	 * @return a string of parameters
	 * @deprecated Method is depricated, refer to {@link Utils#mapToQueryString(Map, String)} instead.
	 */
	public static String buildQueryString(Bundle apiParams, String encoding) {
		StringBuilder sb = new StringBuilder();
		List<String> keys = new ArrayList<String>();
		keys.addAll(apiParams.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			Object o = apiParams.get(key);
			if (isAllowed(o)) {
				if (sb.length() > 0) sb.append("&");
				String value = valueIsNull(o);
				sb.append(encode(key, encoding)).append("=").append(encode(value, encoding));
			} else {
				
				EtaLog.w(TAG, String.format("Key: %s with value-type: %s is not allowed", 
						key, o.getClass().getSimpleName()));
			}
		}
		
		String query = sb.toString();
		
		return query;
	}
	
	/**
	 * Returns a string of parameters, ordered alfabetically (for better cache performance)
	 * @param apiParams to convert into query parameters
	 * @return a string of parameters
	 */
	public static String mapToQueryString(Map<String, String> apiParams, String encoding) {
		if (apiParams==null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		LinkedList<String> keys = new LinkedList<String>(apiParams.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			String value = valueIsNull(apiParams.get(key));
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(encode(key, encoding)).append("=").append(encode(value, encoding));

		}
		return sb.toString();
	}
	
	/**
	 * Returns a string of parameters, ordered alfabetically (for better cache performance)
	 * @param apiParams to convert into query parameters
	 * @return a string of parameters
	 */
	public static Map<String, String> bundleToMap(Bundle b) {
		Map<String, String> map = new HashMap<String, String>();
		for (String key : b.keySet()) {
			Object o = b.get(key);
			if (o instanceof Bundle) {
				throw new IllegalArgumentException("Type Bundle not allowed");
			} else {
				map.put(key, String.valueOf(o));
			}
		}
		
		return map;
	}
	
	/**
	 * Checks the type of an object, to see if it fits the requirement of a query bundle
	 * @param o Object to check
	 * @return true if type is allowed
	 */
	private static boolean isAllowed(Object o) {
		return o == null || o instanceof Integer || o instanceof Long 
				|| o instanceof Double || o instanceof String||o instanceof Boolean
				|| o instanceof Float || o instanceof Short || o instanceof Character;
	}
	
	/**
	 * Method for handling null-values
	 * @param value to check
	 * @return s string where the empty string "" represents null
	 */
	private static String valueIsNull(Object value) {
		String s = value == null ? "" : value.toString();
		return s;
	}
	
	/**
	 * URL encoding of strings
	 * @param value to encode
	 * @param encoding encoding to use
	 * @return an URL-encoded string
	 */
	@SuppressWarnings("deprecation")
	private static String encode(String value, String encoding) {
		try {
			value = URLEncoder.encode(value, encoding);
		} catch (NullPointerException e) {
			// Happens on older devices (HTC Sense)?
			value = URLEncoder.encode(value);
		} catch (UnsupportedEncodingException e) {
			value = URLEncoder.encode(value);
		} catch (IllegalCharsetNameException e) {
			value = URLEncoder.encode(value);
		}
		return value;
	}
	
	/**
	 * Convert an API date of the format "2013-03-03T13:37:00+0000" into a Date object.
	 * @param date to convert
	 * @return a Date object
	 */
	public static Date stringToDate(String date) {
		synchronized (mSdf) {
			try {
				return mSdf.parse(date);
			} catch (ParseException e) {
				return new Date(0);
			}
		}
	}
	
	/**
	 * Convert a Date object into a date string, that will be accepted by the API.
	 * <p>The format for an API date is {@link #DATE_FORMAT}</p>
	 * @param date to convert
	 * @return a string
	 */
	public static String dateToString(Date date) {
		synchronized (mSdf) {
			try {
				return mSdf.format(date);
			} catch (NullPointerException e) {
				return DATE_EPOC;
			}
		}
	}
	
	/**
	 * Checks a given status code, is in the range from (including) 200 to (not including) 300, or 304
	 * @param statusCode to check
	 * @return true is is success, else false
	 */
	public static boolean isSuccess(int statusCode) {
		return 200 <= statusCode && statusCode < 300 || statusCode == 304;
	}
	
	/**
	 * <p>Method for rounding the time (date in milliseconds) down to the nearest second. This is necessary when 
	 * comparing timestamps between the server and client, as the server uses seconds, and timestamps will rarely match
	 * as expected otherwise.<p>
	 * 
	 * 1394021345625 -> 1394021345000
	 * @param date
	 */
	public static Date roundTime(Date date) {
		if (date != null) {
			long t = date.getTime()/1000;
			date.setTime( 1000 * t );
		}
		return date;
	}
	
	/**
	 * <p>Method for converting a size (in bytes) into a human readable format.</p>
	 * 
	 * <table style="text-align: right; border: #000000 solid 1px ">
	 * <tr><th>input</th>	<th>SI</th>			<th>BINARY</th></tr>
	 * <tr><td>0</td>		<td>0 B</td>		<td>0 B</td></tr>
	 * <tr><td>27</td>		<td>27 B</td>		<td>27 B</td></tr>
	 * <tr><td>1023</td>	<td>1.0 kB</td>		<td>1023 B</td></tr>
	 * <tr><td>1024</td>	<td>1.0 kB</td>		<td>1.0 KiB</td></tr>
	 * </table>
	 * 
	 * <p>Same system as above for larger values.</p>
	 * @param bytes A number of bytes to convert
	 * @param si Use SI units, or binary form
	 * @return A human readable string of the byte-size
	 */
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	/**
	 * Takes an exception and returns it as a String.
	 * @param t An exception
	 * @return The string representation of the exception
	 */
	public static String exceptionToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
	
	/**
	 * 
	 * @param iter
	 * @return
	 */
	public static <T> List<T> copyIterator(Iterator<T> iter) {
	    List<T> copy = new ArrayList<T>();
	    while (iter.hasNext()) {
	        copy.add(iter.next());
	    }
	    return copy;
	}

	/**
	 * This method converts device independent pixels (dp) to the equivalent pixels (px) .
	 * @param dp A value in device independent pixels, to convert.
	 * @param c Context to get device specifications from.
	 * @return The value in px representing the equivalent value given in dp
	 */
	public static int convertDpToPx(int dp, Context c){
	    float px = (float)dp * c.getResources().getDisplayMetrics().density;
	    return (int)px;
	}

	/**
	 * This method converts pixels (px) to the equivalent device independent pixels (dp).
	 * @param px A value in pixels, to convert.
	 * @param c Context to get device specifications from.
	 * @return The value in dp representing the equivalent value given in px
	 */
	public static int convertPxToDp(int px, Context c){
	    float dp = (float)px / c.getResources().getDisplayMetrics().density;
	    return (int)dp;
	}
	
	/**
	 * Get the version name contained in the AndroidManifest
	 * @param c A {@link Context} to get the the info from
	 * @return A version name string, or <code>null</code>
	 */
	public static String getAppVersion(Context c) {
		
		try {
			return c.getPackageManager().getPackageInfo(c.getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException e) {
			EtaLog.e(TAG, null, e);
		}
		return null;
	}
	
}