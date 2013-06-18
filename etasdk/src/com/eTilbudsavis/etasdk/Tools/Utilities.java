/**
 * @fileoverview	Utilities.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * @version			0.0.1
 */
package com.eTilbudsavis.etasdk.Tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;

import android.util.Log;

public final class Utilities {
	
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

	/**
	 * Create universally unique identifier.
	 *
	 * @return Universally unique identifier (UUID).
	 */
	public static String createUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * A proxy for Log.d API that silences log messages in release.
	 *
	 * @param tag Used to identify the source of a log message. It usually
	 *			identifies the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	public static void logd(String tag, String msg) {
		if (Eta.mDebug)
			Log.d(tag, msg);
	}	

	public static void logd(String tag, int statusCode, Object object) {
		if (Eta.mDebug)
			Log.d(tag, "Status: " + String.valueOf(statusCode) + ", Data: " + object.toString());
	}

	public static void logd(String tag, int statusCode, Object data, EtaError error) {
		if (Eta.mDebug) {
			Log.d(tag, isSuccess(statusCode) ? data.toString() : error.toString());
		}
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

			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			
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

			
		}
		sb.append(" }");
		return sb.toString();
	}
	
	/**
	 * Method checks is a given e-mail is of a valid format.<br>
	 * e.g.: danny@etilbudsavis.dk
	 * 
	 * @param email to check
	 * @return boolean true for valid, false for invalid
	 */
	public static boolean isEmailValid(String email) {

		if (email.length() == 0)
			return false;

	    boolean isValid = false;

	    String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	    CharSequence inputStr = email;

	    Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
	    Matcher matcher = pattern.matcher(inputStr);
	    if (matcher.matches()) {
	        isValid = true;
	    }
	    return isValid;
	}

	/**
	 * Checks if the name is a valid user name for eta.dk <br><br>
	 * 
	 * Requirements: length > 2 chars.
	 * @param name to check
	 * @return boolean, true if name if valid
	 */
	public static boolean isNameValid(String name) {
		return name.length() > 1 ? (name.length() < 81 ? true : false ) : false;
	}

	/**
	 * Checks if a given password fits the requirements of etilbudsavis.dk.<br><br>
	 * 
	 * Requirements: password length from 6 through 39 chars.
	 * @param password
	 * @return
	 */
	public static boolean isPasswordValid(String password) {
		return 5 < password.length() && password.length() < 40;
	}

	/**
	 * Checks if a given integer is a valid birth year.<br>
	 * 
	 * Requirements: birth year from 1901 through 2011.
	 * @param birthyear
	 * @return
	 */
	public static boolean isBirthyearValid(Integer birthyear) {
		return birthyear > 1900 ? (birthyear < 2012 ? true : false ) : false ;
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

	public static boolean isSuccess(int statusCode) {
		return 200 <= statusCode && statusCode < 300;
	}

	public static boolean isRedirection(int statusCode) {
		return 300 <= statusCode && statusCode < 400;
	}

	public static boolean isClientError(int statusCode) {
		return 400 <= statusCode && statusCode < 500;
	}

	public static boolean isServerError(int statusCode) {
		return 500 <= statusCode && statusCode < 600;
	}
	
	
	
}