/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import com.eTilbudsavis.etasdk.Eta;

public class Device {
	
	public static final String TAG = Eta.TAG_PREFIX + Device.class.getSimpleName();
	
	public static final long KB = 1024;
	public static final long MB = KB * KB;
	
	@SuppressLint("NewApi")
	public static String getRadio() {
		String radio = "";
		// TODO: this radio code fails horribly on older devices, and causes dalvik to halt... bad bad bad
//		try {
//			radio = (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) ? Build.RADIO : Build.getRadioVersion();
//		} catch (Exception e) {
//			EtaLog.e("Device", e);
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
	
	public static boolean hasLargeHeap(Context c) {
		return (c.getApplicationInfo().flags & ApplicationInfo.FLAG_LARGE_HEAP) != 0;
	}
	
	public static String getHeapInfo(Context c) {
		
		StringBuilder sb = new StringBuilder();
		
		Runtime rt = Runtime.getRuntime();
		long free = rt.freeMemory()/MB;
		long available = rt.totalMemory()/MB;
		long max = rt.maxMemory()/MB;
		
		String format = "Heap[max %smb - currently %smb free, %smb allocated";
		sb.append(String.format(format, max, free, available));
		
		sb.append(", LargeHeap: ").append(hasLargeHeap(c));
		if (hasLargeHeap(c)) {
			
			ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
			try {
				// getLargeMemoryClass will fail on old devices
				int largeMem = am.getLargeMemoryClass();
				sb.append("(").append(largeMem).append(")");
			} catch (Throwable t) {
				sb.append("NoInfo");
			}
			
		}
		
		return sb.append("]").toString();
	}
}
