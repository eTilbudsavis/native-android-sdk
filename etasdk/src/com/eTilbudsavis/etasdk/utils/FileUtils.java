package com.eTilbudsavis.etasdk.utils;

import static android.os.Environment.MEDIA_MOUNTED;

import java.io.File;

import android.content.Context;
import android.os.Environment;

import com.eTilbudsavis.etasdk.log.EtaLog;

public class FileUtils {
	
	public static final String TAG = FileUtils.class.getSimpleName();
	
	public static File getCacheDirectory(Context context, boolean preferExternal) {
		File cacheDir = null;
		if (preferExternal &&
				MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
					PermissionUtils.hasWriteExternalStorage(context)) {
			cacheDir = getExternalCacheDir(context);
		}
		
		if (cacheDir == null) {
			cacheDir = context.getCacheDir();
		}
		
		if (cacheDir == null) {
			String filesDir = context.getFilesDir().getPath();
			cacheDir = new File(filesDir + context.getPackageName() + "/cache/");
		}
		
		return cacheDir;
	}
	
	private static File getExternalCacheDir(Context context) {
		File dataDir = new File(Environment.getExternalStorageDirectory(), "Android/data");
		File cacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
		if (!cacheDir.exists() && !cacheDir.mkdirs()) {
			EtaLog.w(TAG, "External directory couldn't be created");
			return null;
		}
		return cacheDir;
	}
	
}
