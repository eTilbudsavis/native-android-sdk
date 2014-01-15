package com.eTilbudsavis.etasdk.Utils;

import android.annotation.SuppressLint;
import android.os.Build;

public class Device {

	@SuppressLint("NewApi")
	public static String getRadio() {
		String radio = "";
		try {
			radio = (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) ? Build.RADIO : Build.getRadioVersion();
		} catch (Exception e) { }
		return radio;
	}

	public static String getBuildVersion() {
		return Build.VERSION.RELEASE;
	}
	
	public static String getKernel() {
		return System.getProperty("os.version");
	}
	
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
	
	public static String getDeviceInfo() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("Model: ").append(getModel())
		.append(", Android: ").append(getBuildVersion())
		.append(", BaseBand: ").append(getRadio())
		.append(", Kernel: ").append(getKernel());
		return sb.toString();
	}
	
}
