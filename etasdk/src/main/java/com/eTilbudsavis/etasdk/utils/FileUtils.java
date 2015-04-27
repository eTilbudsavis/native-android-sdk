/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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
 ******************************************************************************/

package com.eTilbudsavis.etasdk.utils;

import static android.os.Environment.MEDIA_MOUNTED;

import java.io.File;

import android.content.Context;
import android.os.Environment;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;

public class FileUtils {
	
	public static final String TAG = Constants.getTag(FileUtils.class);

	public static final long KB = 1024;
	public static final long MB = KB * KB;
	
	public static File getCacheDirectory(Context context, boolean preferExternal) {
		return getExtDirectory(context, preferExternal, "cache");
	}
	
	public static File getLogDirectory(Context context, boolean preferExternal) {
		return getExtDirectory(context, preferExternal, "log");
	}
	
	public static File getExtDirectory(Context context, boolean preferExternal, String dir) {
		File cacheDir = null;
		if (preferExternal &&
				MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) &&
					PermissionUtils.hasWriteExternalStorage(context)) {
			cacheDir = getExternalDir(context, dir);
		}
		
		if (cacheDir == null) {
			cacheDir = context.getCacheDir();
		}
		
		if (cacheDir == null) {
			String filesDir = context.getFilesDir().getPath();
			cacheDir = new File(filesDir + context.getPackageName() + "/" + dir + "/");
		}
		
		return cacheDir;
	}
	
	private static File getExternalDir(Context context, String dir) {
		File dataDir = new File(Environment.getExternalStorageDirectory(), "Android/data");
		File cacheDir = new File(new File(dataDir, context.getPackageName()), dir);
		if (!cacheDir.exists() && !cacheDir.mkdirs()) {
			EtaLog.w(TAG, "External directory couldn't be created");
			return null;
		}
		return cacheDir;
	}
	
}
