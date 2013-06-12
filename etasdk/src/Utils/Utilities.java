/**
 * @fileoverview	Utilities.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * @version			0.0.1
 */
package Utils;

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
		if (Eta.DEBUG)
			Log.d(tag, msg);
	}	
	public static void logd(String tag, int statusCode, Object object) {
		if (Eta.DEBUG)
			Log.d(tag, "Status: " + String.valueOf(statusCode) + ", Data: " + object.toString());
	}

	/**
	 * A proxy for Log.i API that silences log messages in release.
	 *
	 * @param tag Used to identify the source of a log message. It usually
	 *			identifies the class or activity where the log call occurs.
	 * @param msg The message you would like logged.
	 */
	public static void logi(String tag, String msg) {
		if (Eta.DEBUG_I)
			Log.i(tag, msg);
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
	 * UTC time in seconds (time since 01.01.1970).
	 *
	 * @return int Time in seconds
	 */
	public static int getTime() {
		return Integer.valueOf(String.valueOf(System.currentTimeMillis()/1000));
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
	public static Boolean isNameValid(String name) {
		return name.length() > 1 ? (name.length() < 81 ? true : false ) : false;
	}

	/**
	 * Checks if a given password fits the requirements of etilbudsavis.dk.<br><br>
	 * 
	 * Requirements: password length from 6 through 39 chars.
	 * @param password
	 * @return
	 */
	public static Boolean isPasswordValid(String password) {
		return 5 < password.length() && password.length() < 40;
	}

	/**
	 * Checks if a given integer is a valid birth year.<br>
	 * 
	 * Requirements: birth year from 1901 through 2011.
	 * @param birthyear
	 * @return
	 */
	public static Boolean isBirthyearValid(Integer birthyear) {
		return birthyear > 1900 ? (birthyear < 2012 ? true : false ) : false ;
	}

	/**
	 * Checks if a given integer is a valid birth year.<br>
	 * 
	 * Requirements: birth year from 1901 through 2011.
	 * @param birthyear
	 * @return
	 */
	public static Boolean isGenderValid(String gender) {
		gender = gender.toLowerCase();
		return (gender.equals("male") || gender.equals("female") );
	}

	
	
}