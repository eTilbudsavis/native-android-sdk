package com.eTilbudsavis.etasdk.imageloader.impl;
import com.eTilbudsavis.etasdk.Constants;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;

import android.content.Context;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.imageloader.FileCache;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.utils.FileUtils;

public class DefaultFileCache implements FileCache {

	public static final String TAG = Constants.getTag(DefaultFileCache.class);
	
	private File mCacheDir;
	private ExecutorService mExecutor;
	
	public DefaultFileCache(Context context, ExecutorService executor){
		mCacheDir = FileUtils.getCacheDirectory(context, true);
		mExecutor = executor;
		EtaLog.v(TAG, "CacheDir: " + mCacheDir.getAbsolutePath());
		cleanup();
	}
	
	public void save(final ImageRequest ir, final byte[] b) {
		
		Runnable r = new Runnable() {
			
			public void run() {
				
				File f = new File(mCacheDir, ir.getFileName());
				FileOutputStream fos = null;
				if (f.exists()) {
					f.delete();
				}
				try {
					fos = new FileOutputStream(f);
					fos.write(b);
				} catch (IOException e) {
					
				} finally {
					try{
						fos.close();
					} catch(Throwable t) {
						
					}
				}
//				EtaLog.d(TAG, "SaveByteArray: " + (System.currentTimeMillis()-start));
			}
		};
		
		mExecutor.execute(r);
		
	}
	
	public byte[] getByteArray(ImageRequest ir) {
		File f = new File(mCacheDir, ir.getFileName());
		byte[] b = null;
		if (f.exists()) {
			try {
				b = readFile(f);
			} catch (FileNotFoundException e) {
				
			} catch ( IOException  e2) {
				
			}
		}
		return b;
	}
	
	public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
	private static final long WEEK_IN_MILLIS = 1000 * 60 * 60 * 24 * 7;
	
	Runnable cleaner = new Runnable() {
		
		public void run() {
			File[] files = mCacheDir.listFiles();
			if( files == null ) {
				return;
			}
			int count = 0;
			for(File f:files) {
				boolean recentlyModified = (System.currentTimeMillis()-f.lastModified()) < WEEK_IN_MILLIS;
				if ( !recentlyModified ) {
					count++;
					f.delete();
				}
			}
			if (count > 0) {
				EtaLog.v(TAG, "Deleted " + count + " files from " + getClass().getSimpleName());
			}
		}
	};
	
	public void cleanup(){
		
		mExecutor.execute(cleaner);
		
	}
	
	public void clear(){
		File[] files = mCacheDir.listFiles();
		if( files == null ) {
			return;
		}
		for(File f:files) {
			f.delete();
		}
	}
	
}
