package com.eTilbudsavis.etasdk.Utils;

import com.eTilbudsavis.etasdk.Eta;

import android.content.Context;
import android.content.pm.PackageManager;

public class PermissionUtils {
	
	public static final String TAG = Eta.TAG_PREFIX + PermissionUtils.class.getSimpleName();
	
	private static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
	
	public static boolean hasWriteExternalStorage(Context c) {
		return c.checkCallingOrSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
	}
	
}
