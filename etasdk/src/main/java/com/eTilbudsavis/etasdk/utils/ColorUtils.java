package com.eTilbudsavis.etasdk.utils;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;

public class ColorUtils {
	
	public static final String TAG = Constants.getTag(ColorUtils.class);
	
	private ColorUtils() {
		// empty
	}
	
	/**
	 * Apply an alpha channel to a color. Any old alpha on the given color will be removed.
	 * the alpha int will be shifted 24 bits left, so only the first 8 bits (range 0-255) will be used.
	 * 
	 * <table border=1 summary="">
	 *   <tr><td>Color</td><td>Alpha</td><td>Result</td><td>Remark</td></tr>
	 *   <tr><td>null</td><td>{any}</td><td>null</td><td>none</td></tr>
	 *   <tr><td>0x00FFFFFF</td><td>0</td><td>0x00FFFFFF</td><td>none</td></tr>
	 *   <tr><td>0x00FFFFFF</td><td>160</td><td>0xA0FFFFFF</td><td>none</td></tr>
	 *   <tr><td>0x00FFFFFF</td><td>255</td><td>0xFFFFFFFF</td><td>none</td></tr>
	 *   <tr><td>0x00FFFFFF</td><td>256</td><td>0x00FFFFFF</td><td>notice only the first 8 bits are used</td></tr>
	 * </table>
	 * @param color A color
	 * @param alpha The alpha to apply
	 * @return A new color, or <code>null</code> if input color is null
	 */
	public static Integer applyAlpha(Integer color, int alpha) {
		return color == null ? null : (color & 0xffffff) | (alpha << 24);
	}
	
	/**
	 * Remove any transparency from a color. The eTilbudsavis v2 doesn't support alpha channel in colors.
	 * @param color A color
	 * @return A new color, or <code>null</code> if input color is null
	 */
	public static Integer stripAlpha(Integer color) {
		return stripAlpha(color, true);
	}
	
	/**
	 * Remove any transparency from a color. The eTilbudsavis v2 doesn't support alpha channel in colors.
	 * @param color A color
	 * @param showWarning <code>true</code> to send warnings to {@link EtaLog}
	 * @return A new color, or <code>null</code> if input color is null
	 */
	public static Integer stripAlpha(Integer color, boolean showWarning) {
		if (color!=null) {
			if (showWarning && Color.alpha(color)<255) {
				EtaLog.w(TAG, "eTilbudsavis API v2, doesn't support alpha colors - alpha will be stripped");
			}
			color |= 0x00000000ff000000;
		}
		return color;
	}
	
	/**
	 * Method returns a eTilbudsavis API friendly color string.<br>
	 * Alpha isn't supported, and will be stripped. Warnings will be logged to console 
	 * <li>Color.WHITE = "FFFFFF"</li>
	 * <li>Color.BLACK = "000000"</li>
	 * <li>Color.BLUE = "0000FF"</li>
	 * @param color The color to parse
	 * @return A string, or null
	 */
	public static String toString(Integer color) {
		return toString(color, true);
	}

	/**
	 * Method returns a eTilbudsavis API friendly color string.<br>
	 * Alpha isn't supported, and will be stripped. This will be logged as a warning in console 
	 * if ignoreWarnings is false.
	 * <li>Color.WHITE = "FFFFFF"</li>
	 * <li>Color.BLACK = "000000"</li>
	 * <li>Color.BLUE = "0000FF"</li>
	 * 
	 * @param color The color to parse
	 * @param ignoreWarnings - set to true to ignore alpha warnings
	 * @return A string, or <code>null</code> if input color is null
	 */
	public static String toString(Integer color, boolean showWarning) {
		return toString(color, showWarning, true);
	}

	/**
	 * Convert a color into a String.
	 * <br/>
	 * <br/>
	 * You can create two types of Strings:
	 * <ol>
	 * 	<li> A regular #ARGB string (e.g.: {@link Color#BLACK} = "#FF000000"), or
	 * 	<li> An eTilbudsavis API v2 compliant string, where alpha and '#' isn't allowed, e.g.: {@link Color#BLACK} = "000000")
	 * </ol>
	 * 
	 * <li>Color.WHITE = "FFFFFF"</li>
	 * <li>Color.BLACK = "000000"</li>
	 * <li>Color.BLUE = "0000FF"</li>
	 * 
	 * @param color The color to parse
	 * @param ignoreWarnings - set to true to ignore alpha warnings
	 * @param apiCompliant <code>true</code> to create a eTilbudsavis API v2 compliant string like "RGB".
	 * <code>false</code> to get an "#ARGB"
	 * @return A string, or <code>null</code> if input color is null
	 */
	public static String toString(Integer color, boolean showWarning, boolean apiCompliant) {
		if (color==null) {
			return null;
		}
		color = stripAlpha(color, showWarning);
		if (apiCompliant) {
			return String.format("%06X", 0xFFFFFF & color);
		} else {
			// not eta api valid string
			return String.format("#%08X", 0xFFFFFFFF & color);
		}
	}
	
	/**
	 * Converts a given string to a color
	 * <li>"FFFFFF" = Color.WHITE</li>
	 * <li>"000000" = Color.BLACK</li>
	 * <li>"0000FF" = Color.BLUE</li>
	 * @return A string, or <code>null</code> if input color is null
	 */
	public static Integer toColor(String color) {
		if (color==null) {
			return null;
		}
		if (!color.startsWith("#")) {
			color = "#" + color;
		}
		try {
			return Color.parseColor(color);
		} catch (NumberFormatException e) {
//			EtaLog.e(TAG, e.getMessage(), e);
		}
		return null;
	}
	
}
