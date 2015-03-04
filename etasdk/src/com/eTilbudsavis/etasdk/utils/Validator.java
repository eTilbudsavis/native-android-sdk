package com.eTilbudsavis.etasdk.utils;

import java.util.regex.Pattern;

public class Validator {
	
	public static final String APP_VERSION_FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)([+-][0-9A-Za-z-.]*)?";
	
	public static final String xAPP_VERSION_FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)([-]([0-9A-Za-z-.]+)*)?";
	
	//           \d+\.\d+\.\d+(\-[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?(\+[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?
	
	/**
	 * Checks if a given integer is a valid birth year.<br>
	 * Requirements: birth year is in the span 1900 - 2015.
	 * @param birthyear
	 * @return
	 */
	public static boolean isBirthyearValid(Integer birthyear) {
		return birthyear >= 1900 ? (birthyear <= 2015) : false ;
	}
	
	/**
	 * A very naive implementation of email validation.<br>
	 * Requirement: String must contains a single '@' char, and that there is at least one char before and after the '@'
	 * @param email to check
	 * @return true if email is valid, else false
	 */
	public static boolean isEmailValid(String email) {
		return email != null && email.trim().split("@").length == 2;
	}

	/**
	 * Checks if a given string is a valid gender.<br>
	 * Requirements: String is either 'male' or 'female' (not case sensitive).
	 * @param birthyear
	 * @return
	 */
	public static boolean isGenderValid(String gender) {
		if (gender!=null) {
			String g = gender.toLowerCase().trim();
			return (g.equals("male") || g.equals("female") );
		}
		return false;
	}

	/**
	 * A simple regular expression to check if the app-version string can be accepted by the API
	 * @param version to check
	 * @return true, if the version matched the regex
	 */
	public static boolean isAppVersionValid(String version) {
		if (version == null) {
			return false;
		}
	    return Pattern.compile(APP_VERSION_FORMAT).matcher(version).matches();
	}
	
}
