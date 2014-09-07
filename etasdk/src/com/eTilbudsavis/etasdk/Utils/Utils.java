/**
 * @fileoverview	Utilities.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * @version			0.0.1
 */
package com.eTilbudsavis.etasdk.Utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.Request;

public final class Utils {
	
	public static final String TAG = Eta.TAG_PREFIX + Utils.class.getSimpleName();
	
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

	public static final String APP_VERSION_FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)([+-][0-9A-Za-z-.]*)?";
	
	/** Single instance of SimpleDateFormat to save time and memory */
	private static SimpleDateFormat mSdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
	
	static {
		
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
     * Generate a SHA256 checksum of a string.
     * 
     * @param string to SHA256
     * @return A SHA256 string
     */
	public static String generateSHA256(String string) {
		
	    MessageDigest digest=null;
	    String hash = "";
	    try {
	        digest = MessageDigest.getInstance("SHA-256");
	        digest.update(string.getBytes());
	        byte[] bytes = digest.digest();
	        
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < bytes.length; i++) {
	            String hex = Integer.toHexString(0xFF & bytes[i]);
	            if (hex.length() == 1) {
	                sb.append('0');
	            }
	            sb.append(hex);
	        }
	        hash = sb.toString();

	    } catch (NoSuchAlgorithmException e1) {
	        e1.printStackTrace();
	    }
	    return hash;
	}
	
	/**
	 * Builds a url + query string.<br>
	 * e.g.: https://api.etilbudsavis.dk/v2/catalogs?order_by=popular
	 * @param r to build from
	 * @return
	 */
	public static String buildQueryString(Request<?> r) {
		return r.getQueryParameters().isEmpty() ? r.getUrl() : r.getUrl() + "?" + buildQueryString(r.getQueryParameters(), r.getParamsEncoding());
	}

	/**
	 * Returns a string of parameters, ordered alfabetically (for better cache performance)
	 * @param apiParams to convert into query parameters
	 * @return a string of parameters
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
		} catch (UnsupportedEncodingException e) {
			EtaLog.e(TAG, null, e);
			value = URLEncoder.encode(value);
		}
		return value;
	}
	
	/**
	 * Builds the block of JavaScript parameters for injecting into a WebView.
	 *
	 * @param data The Map to process
	 * @return A String in JavaScript format
	 */
	@SuppressWarnings("unchecked")
	public static String mapToJavaScript(Map<String, Object> data) {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		boolean firstTime = true;
		for (String s : data.keySet()) {
			if (firstTime) {
				firstTime = false;
			} else {
				sb.append(", ");
			}
			sb.append(s);
			if (data.get(s) instanceof Map<?, ?>) {
				sb.append(": ");
				sb.append( mapToJavaScript( (Map<String, Object>)data.get(s) ) );
			} else {
				sb.append(": '");
				sb.append(data.get(s).toString());
				sb.append("'");
			}
		}
		return sb.append(" }").toString();
	}
	
	/**
	 * Checks if a given integer is a valid birth year.<br>
	 * Requirements: birth year is in the span 1900 - 2013.
	 * @param birthyear
	 * @return
	 */
	public static boolean isBirthyearValid(Integer birthyear) {
		return birthyear > 1900 ? (birthyear < 2013) : false ;
	}
	
	/**
	 * A very naive implementation of email validation.<br>
	 * Requirement: String must contains a '@', and that there is at least one char before and after the '@'
	 * @param email to check
	 * @return true if email is valid, else false
	 */
	public static boolean isEmailValid(String email) {
		return email.contains("@") && email.split("@").length > 1; 
	}

	/**
	 * Checks if a given string is a valid gender.<br>
	 * Requirements: String is either 'male' or 'female' (not case sensitive).
	 * @param birthyear
	 * @return
	 */
	public static boolean isGenderValid(String gender) {
		gender = gender.toLowerCase();
		return (gender.equals("male") || gender.equals("female") );
	}
	
	/**
	 * Convert an API date of the format "2013-03-03T13:37:00+0000" into a Date object.
	 * @param date to convert
	 * @return a Date object
	 */
	public static Date parseDate(String date) {
		synchronized (mSdf) {
			try {
				return mSdf.parse(date);
			} catch (ParseException e) {
				EtaLog.e(TAG, null, e);
				return new Date(1000);
			}
		}
	}
	
	/**
	 * Convert a Date object into a date string, that will be accepted by the API.
	 * <p>The format for an API date is {@link #DATE_FORMAT}</p>
	 * @param date to convert
	 * @return a string
	 */
	public static String parseDate(Date date) {
		synchronized (mSdf) {
			try {
				return mSdf.format(date);
			} catch (NullPointerException e) {
				return null;
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
	 * A simple regular expression to check if the app-version string can be accepted by the API
	 * @param version to check
	 * @return true, if the version matched the regex
	 */
	public static boolean validVersion(String version) {
	    return Pattern.compile(APP_VERSION_FORMAT).matcher(version).matches();
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
	
}