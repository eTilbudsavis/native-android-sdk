package com.eTilbudsavis.etasdk.Utils;

import android.annotation.SuppressLint;
import android.os.Build;

public class Device {

	@SuppressLint("NewApi")
	public static String getRadio() {
		String radio = "";
		// TODO: this radio code fails horribly on older devices, and causes dalvik to halt... bad bad bad
//		try {
//			radio = (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) ? Build.RADIO : Build.getRadioVersion();
//		} catch (Exception e) {
//			EtaLog.d("Device", e);
//		}
		return radio;
	}
	
	/**
	 * Get the build version of the device.<br>
	 * @see {@link #Build.VERSION.RELEASE Build.VERSION.RELEASE}
	 * @return build version
	 */
	public static String getBuildVersion() {
		return Build.VERSION.RELEASE;
	}
	
	/**
	 * Get the kernel version of this device.<br>
	 * @see {@link #System.getProperty() System.getProperty("os.version")}
	 * @return kernel version
	 */
	public static String getKernel() {
		return System.getProperty("os.version");
	}
	
	/**
	 * Get the device model. e.g.: "Samsung GT-P3110"
	 * @return phone model
	 */
	public static String getModel() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			model = capitalize(model);
		} else {
			model = capitalize(manufacturer) + " " + model;
		}
		return model;
	}
	
	private static String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}
	
	/**
	 * Get a concatenated version of:
	 * {@link #getModel() getModel()}, {@link #getBuildVersion() getBuildVersion()}, {@link #getRadio() getRadio()}, {@link #getKernel() getKernel()}
	 * @return a readable string with device info
	 */
	public static String getDeviceInfo() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("model[").append(getModel()).append("]")
		.append(", android[").append(getBuildVersion()).append("]")
		.append(", baseBand[").append(getRadio()).append("]")
		.append(", kernel[").append(getKernel()).append("]");
		return sb.toString();
	}
	
}
