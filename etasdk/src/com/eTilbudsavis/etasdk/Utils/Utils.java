/**
 * @fileoverview	Utilities.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * @version			0.0.1
 */
package com.eTilbudsavis.etasdk.Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;

public final class Utils {
	
	public static final String TAG = "Utils";
	
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
	
	public static String bundleToQueryString( Bundle apiParams) {
		StringBuilder sb = new StringBuilder();
		for (String key : apiParams.keySet()) {
			Object o = apiParams.get(key);
			if (o instanceof Bundle) {
				EtaLog.d(TAG, "Nested parameters not allowed.");
			} else {
				if (sb.length() > 0) sb.append("&");
				sb.append(key).append("=").append(valueIsNull(o));
			}
		}
		return sb.toString();
	}
	
	public static List<NameValuePair> bundleToNameValuePair( Bundle apiParams) {
		List<NameValuePair> list = new ArrayList<NameValuePair>(apiParams.size());
		for (String key : apiParams.keySet()) {
			if (apiParams.get(key) instanceof Bundle) {
				EtaLog.d(TAG, "Nested parameters not allowed.");
			} else {
				list.add(new BasicNameValuePair(key, valueIsNull(apiParams.get(key))));
			}
		}
		return list;
	}

	private static String valueIsNull(Object value) {
		String s = value == null ? "" : value.toString();
		return s;
	}

	/**
	 * Builds JSONObject from a given Bundle.
	 *
	 * @param b - the Bundle to convert
	 * @return A JSONObject containing all key-value pairs from the bundle.
	 */
	public static JSONObject createJSON(Bundle b) {
		return new JSONObject(createMap(b));
	}
	
	/**
	 * Builds Map<String, Object> from a given Bundle.
	 * 
	 * @param b - the Bundle to convert
	 * @return A Map<String, Object> containing all key-value pairs from the bundle.
	 */
	public static Map<String, Object> createMap(Bundle b) {
		Map<String, Object> map = new HashMap<String, Object>();
//		try {
			for (String s : b.keySet()) {
				Object o = b.get(s);
				map.put(s, (o instanceof Bundle) ? createMap((Bundle)o) : 0);
			}
//		} catch (Exception e) {
//			EtaLog.d(TAG, e);
//		}
		return map;
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
	 * 
	 * Requirements: birth year from 1901 through 2011.
	 * @param birthyear
	 * @return
	 */
	public static boolean isBirthyearValid(Integer birthyear) {
		return birthyear > 1900 ? (birthyear < 2013 ? true : false ) : false ;
	}
	
	/**
	 * A very naive implementation of email validation.<br>
	 * 
	 * @param email
	 * @return
	 */
	public static boolean isEmailValid(String email) {
		return email.contains("@") && email.split("@").length > 1; 
	}

	/**
	 * Checks if a given integer is a valid birth year.<br>
	 * 
	 * Requirements: birth year from 1901 through 2011.
	 * @param birthyear
	 * @return
	 */
	public static boolean isGenderValid(String gender) {
		gender = gender.toLowerCase();
		return (gender.equals("male") || gender.equals("female") );
	}
	
	public static Date parseDate(String date) {
		Date d = null;
		try {
			d = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	public static String formatDate(Date date) {
		return new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date);
	}
	

	public static void sortItems(List<ShoppinglistItem> items) {
		int size = items.size();
		
		HashSet<String> allId = new HashSet<String>(size);
		for (ShoppinglistItem sli : items) {
			allId.add(sli.getId());
		}
		
		List<ShoppinglistItem> first = new ArrayList<ShoppinglistItem>(size);
		List<ShoppinglistItem> nil = new ArrayList<ShoppinglistItem>(size);
		List<ShoppinglistItem> orphan = new ArrayList<ShoppinglistItem>(size);
		HashMap<String, ShoppinglistItem> prevItems = new HashMap<String, ShoppinglistItem>(size);
		
		for (ShoppinglistItem sli : items) {
			
			String prev = sli.getPreviousId();
			
			if (prev == null) {
				nil.add(sli);
			} else if (prev.equals(ShoppinglistItem.FIRST_ITEM)) {
				first.add(sli);
			} else if ( !prevItems.containsKey(prev) && allId.contains(prev)) {
				prevItems.put(prev, sli);
			} else {
				orphan.add(sli);
			}
			
		}
		
		// Clear the original items list, items in this list is to be restored shortly
		items.clear();
		
		Collections.sort(first, ShoppinglistItem.TitleComparator);
		Collections.sort(nil, ShoppinglistItem.TitleComparator);
		Collections.sort(orphan, ShoppinglistItem.TitleComparator);
		
		List<ShoppinglistItem> newItems = new ArrayList<ShoppinglistItem>(size);
		newItems.addAll(nil);
		newItems.addAll(first);
		newItems.addAll(orphan);
		
		ShoppinglistItem next;
		String id;
		for (ShoppinglistItem sli : newItems) {
			next = sli;
			while (next != null) {
				id = next.getId();
				items.add(next);
				next = prevItems.get(id);
				prevItems.remove(id);
			}
		}
		
		for (ShoppinglistItem s : prevItems.values())
			items.add(s);
		
	}

	public static boolean isSuccess(int statusCode) {
		return 200 <= statusCode && statusCode < 300 || statusCode == 304;
	}
	
	public static boolean validVersion(String version) {
		
	    String FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)([+-][0-9A-Za-z-.]*)?";
	    Pattern PATTERN = Pattern.compile(FORMAT);
	    return PATTERN.matcher(version).matches();
		
	}
	
}