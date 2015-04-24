package com.eTilbudsavis.etasdk.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.eTilbudsavis.etasdk.Constants;

public class PermissionUtils {
	
	public static final String TAG = Constants.getTag(PermissionUtils.class);
	
	private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
	
	public static boolean hasWriteExternalStorage(Context c) {
		return c.checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
	}
	
}
