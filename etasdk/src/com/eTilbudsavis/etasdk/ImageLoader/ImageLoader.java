package com.eTilbudsavis.etasdk.ImageLoader;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.eTilbudsavis.etasdk.ImageLoader.Impl.DefaultBitmapDisplayer;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.DefaultFileCache;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.DefaultImageDownloader;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.DefaultThreadFactory;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.LruMemoryCache;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class ImageLoader {

	public static final String TAG = ImageLoader.class.getSimpleName();
	
	private static final int DEFAULT_THREAD_COUNT = 3;
	
	private Map<ImageView, String> mImageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private MemoryCache mMemoryCache;
	private FileCache mFileCache;
	private ExecutorService mExecutorService;
	private ImageDownloader mDownloader;
	private Handler mHandler;
	
	private static ImageLoader mImageloader;
	
	private ImageLoader(Context context) {
		mMemoryCache = new LruMemoryCache();
		mFileCache = new DefaultFileCache(context);
		mExecutorService = Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT, new DefaultThreadFactory());
		mDownloader = new DefaultImageDownloader();
		mHandler = new Handler(Looper.getMainLooper());
	}
	
	public synchronized static void init(Context c) {
		if (mImageloader == null) {
			mImageloader = new ImageLoader(c);
		}
	}
	
	public static ImageLoader getInstance() {
		if (mImageloader == null) {
			throw new IllegalStateException("ImageLoader.init() must be called"
					+ "prior to getting the instance"); 
		}
		return mImageloader;
	}
	
	public void displayImage(ImageRequest ir) {
		
		ir.start();
		ir.imageView.setTag(ir.url);
		mImageViews.put(ir.imageView, ir.url);
		
		ir.bitmap = mMemoryCache.get(ir.url);
		if(ir.bitmap != null) {
			ir.source = LoadSource.MEMORY;
			processAndDisplay(ir);
		} else {
			mExecutorService.submit(new PhotosLoader(ir));
			if (ir.placeholderLoading != 0) {
				ir.imageView.setImageResource(ir.placeholderLoading);
			}
		}
	}
	
	class PhotosLoader implements Runnable {

		ImageRequest ir;

		PhotosLoader(ImageRequest request){
			ir = request;
		}
		
		public void run() {

			if (imageViewReused(ir)) {
				ir.finish();
				return;
			}
			
			try{
				
				ir.bitmap = mFileCache.get(ir.url);
				
				if (ir.bitmap != null) {
					
					ir.source = LoadSource.FILE;
					
				} else {
					
					int retries = 0;
					while (ir.bitmap == null && retries<2) {
						
						retries++;
						try {
							ir.bitmap = mDownloader.getBitmap(ir.url);
						} catch (Throwable t) {
							EtaLog.d(TAG, t.getMessage(), t);
							if (t instanceof OutOfMemoryError) {
								mMemoryCache.clear();
							}
						}
						
					}
					
					if (ir.bitmap != null) {
						ir.source = LoadSource.WEB;
					}
					
				}
				
				addToCache(ir);
				
				processAndDisplay(ir);

			}catch(Throwable th){
				EtaLog.d(TAG, th.getMessage(), th);
			}
		}
	}
	
	private void addToCache(ImageRequest ir) {
		
		if (ir.bitmap == null || ir.source == null) {
			return;
		}
		
		// Add to filecache and/or memorycache depending on the source
		switch (ir.source) {
			case WEB:
				mFileCache.save(ir.url, ir.bitmap);
			case FILE:
				mMemoryCache.put(ir.url, ir.bitmap);
			default:
				break;
		}
		
	}
	
	private void processAndDisplay(final ImageRequest ir) {
		
		if (imageViewReused(ir) || ir.bitmap == null) {
			ir.finish();
			return;
		}
		
		if (ir.processor != null) {
			
			if (Looper.myLooper() == Looper.getMainLooper()) {
				
				mExecutorService.execute(new Runnable() {
					
					public void run() {
						
						ir.bitmap = ir.processor.process(ir.bitmap);
						display(ir);
					}
				});
				
			} else {
				
				ir.bitmap = ir.processor.process(ir.bitmap);
				display(ir);
				
			}
			
		} else {
			display(ir);
		}
		
	}
	
	private void display(final ImageRequest ir) {
		
		Runnable work = new Runnable() {
			
			public void run() {
				
				if (imageViewReused(ir)) {
					ir.finish();
					return;
				}
				
				if (ir.displayer == null) {
					ir.displayer = new DefaultBitmapDisplayer();
				}
				ir.finish();
				ir.displayer.display(ir);
			}
		};
		
		if (Looper.myLooper() == Looper.getMainLooper()) {
			work.run();
		} else {
			mHandler.post(work);
		}
		
	}
	
	/**
	 * Method to check in the ImageView that the ImageRequest references, have been
	 * reused. This can happen in e.g. ListViews, where the adapter reuses views,
	 * while scrolling.
	 * @param ir Request to check
	 * @return true if the View have been reused, false otherwise
	 */
	private boolean imageViewReused(ImageRequest ir) {
		String url = mImageViews.get(ir.imageView);
		return ((url == null || !url.contains(ir.url)));
	}
	
	public void clear() {
		mImageViews.clear();
		mMemoryCache.clear();
		mFileCache.clear();
	}
	
	public MemoryCache getMemoryCache() {
		return mMemoryCache;
	}
	
	public FileCache getFileCache() {
		return mFileCache;
	}
	
}