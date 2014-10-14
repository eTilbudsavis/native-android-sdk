package com.eTilbudsavis.etasdk.ImageLoader;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.DefaultBitmapDecoder;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.DefaultFileCache;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.DefaultImageDownloader;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.LruMemoryCache;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class ImageLoader {

	public static final String TAG = Eta.TAG_PREFIX + ImageLoader.class.getSimpleName();
	
	private Map<ImageView, String> mImageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private MemoryCache mMemoryCache;
	private FileCache mFileCache;
	private ExecutorService mExecutor;
	private ImageDownloader mDownloader;
	private Handler mHandler;
	
	private static ImageLoader mImageloader;
	
	private ImageLoader(Eta e) {
		mMemoryCache = new LruMemoryCache();
		mFileCache = new DefaultFileCache(e.getContext());
		mExecutor = e.getExecutor();
		mDownloader = new DefaultImageDownloader();
		mHandler = new Handler(Looper.getMainLooper());
	}
	
	public synchronized static void init(Eta e) {
		if (mImageloader == null) {
			mImageloader = new ImageLoader(e);
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
		ir.getImageView().setTag(ir.getUrl());
		if (ir.getBitmapDecoder()==null) {
			ir.setBitmapDecoder(new DefaultBitmapDecoder());
		}
		mImageViews.put(ir.getImageView(), ir.getUrl());
		
		ir.setBitmap(mMemoryCache.get(ir.getUrl()));
		if(ir.getBitmap() != null) {
			ir.setLoadSource(LoadSource.MEMORY);
			processAndDisplay(ir);
		} else {
			mExecutor.submit(new PhotosLoader(ir));
			if (ir.getPlaceholderLoading() != 0) {
				ir.getImageView().setImageResource(ir.getPlaceholderLoading());
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

			try {
				
				int retries = 0;
				while (ir.getBitmap() == null && retries<2) {
					
					try {
						
						retries++;
						ir.setBitmap(mFileCache.get(ir));

						if (ir.getBitmap() != null) {

							ir.setLoadSource(LoadSource.FILE);

						} else {
							
							ir.setBitmap(mDownloader.getBitmap(ir.getUrl()));
							if (ir.getBitmap() != null) {
								ir.setLoadSource(LoadSource.WEB);
							}

						}
						
					} catch (OutOfMemoryError t) {
						mMemoryCache.clear();
					}

				}
				
				addToCache(ir);

			} catch (Throwable th){
				EtaLog.d(TAG, th.getMessage(), th);
			}
			
			processAndDisplay(ir);
			
		}
	}
	
	private void addToCache(ImageRequest ir) {
		
		if (ir.getBitmap() == null || ir.getLoadSource() == null) {
			return;
		}
		
		// Add to filecache and/or memorycache depending on the source
		switch (ir.getLoadSource()) {
			case WEB:
				if (ir.useFileCache()) {
					mFileCache.save(ir, ir.getBitmap());
				}
			case FILE:
				if (ir.useMemoryCache()) {
					mMemoryCache.put(ir.getUrl(), ir.getBitmap());
				}
			default:
				break;
		}
		
	}
	
	private void processAndDisplay(final ImageRequest ir) {
		
		if (imageViewReused(ir) || ir.getBitmap() == null) {
			ir.finish();
			return;
		}
		
		if (ir.getBitmapProcessor() != null) {
			
			if (Looper.myLooper() == Looper.getMainLooper()) {
				
				mExecutor.execute(new Runnable() {
					
					public void run() {
						Bitmap tmp = ir.getBitmapProcessor().process(ir.getBitmap());
						ir.setBitmap(tmp);
						display(ir);
					}
				});
				
			} else {
				Bitmap tmp = ir.getBitmapProcessor().process(ir.getBitmap());
				ir.setBitmap(tmp);
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
				
				ir.finish();
				ir.getBitmapDisplayer().display(ir);
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
		String url = mImageViews.get(ir.getImageView());
		return ((url == null || !url.contains(ir.getUrl())));
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