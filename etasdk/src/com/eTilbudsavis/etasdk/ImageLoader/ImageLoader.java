package com.eTilbudsavis.etasdk.ImageLoader;

import java.io.IOException;
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
		mExecutor = e.getExecutor();
		mMemoryCache = new LruMemoryCache();
		mFileCache = new DefaultFileCache(e.getContext(), mExecutor);
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
		ir.add("start-image-request");
		if (ir.getImageView().getTag()==null) {
			ir.getImageView().setTag(ir.getUrl());
		}
		
		if (ir.getBitmapDecoder()==null) {
			ir.setBitmapDecoder(new DefaultBitmapDecoder());
		}
		mImageViews.put(ir.getImageView(), ir.getUrl());
		
		ir.setBitmap(mMemoryCache.get(ir.getUrl()));
		if(ir.getBitmap() != null) {
			ir.setLoadSource(LoadSource.MEMORY);
			ir.add("loaded-from-" + ir.getLoadSource());
			processAndDisplay(ir);
		} else {
			mExecutor.execute(new PhotosLoader(ir));
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
			ir.add("running-on-executor");
			if (imageViewReused(ir)) {
				ir.finish("imageview-reused");
				return;
			}
			
			int retries = 0;
			while (ir.getBitmap() == null && retries<2) {
				
				ir.add("retries-"+retries);
				byte[] image = null;
				
				try {
					
					retries++;
					ir.add("trying-file-cache");
					image = mFileCache.getByteArray(ir);
					
					if (image != null) {
						
						ir.setLoadSource(LoadSource.FILE);
						
					} else {
						
						ir.add("trying-download");
						image = mDownloader.getByteArray(ir);
						if (image != null) {
							ir.setLoadSource(LoadSource.WEB);
						}
						
					}
					
					if (image != null) {
						Bitmap b = ir.getBitmapDecoder().decode(ir, image);
						ir.setBitmap(b);
						ir.add("loaded-from-" + ir.getLoadSource());
					} else {
						ir.add("no-image-loaded");
					}
					
				} catch (OutOfMemoryError t) {
					ir.add("out-of-memory");
					mMemoryCache.clear();
				} catch (IOException e) {
					ir.add("download-failed");
					EtaLog.e(TAG, "Download error", e);
				}
				addToCache(ir, image);
				
			}
			
			processAndDisplay(ir);
			
		}
	}
	
	
	private void addToCache(ImageRequest ir, byte[] image) {
		
		// This is a bit messy, but i'll cleanup later
		
		if (image == null || ir.getBitmap() == null || ir.getLoadSource() == null) {
			ir.add("cannot-cache-request");
			return;
		}
		
		LoadSource s = ir.getLoadSource();
		if (s==LoadSource.WEB) {
			
			if (ir.useFileCache()) {
				ir.add("adding-to-file-cache");
				mFileCache.save(ir, image);
			}
			if (ir.useMemoryCache()) {
				ir.add("adding-to-memory-cache");
				mMemoryCache.put(ir.getUrl(), ir.getBitmap());
			}
			
		} else if (s==LoadSource.FILE) {
			
			if (ir.useMemoryCache()) {
				ir.add("adding-to-memory-cache");
				mMemoryCache.put(ir.getUrl(), ir.getBitmap());
			}
			
		}
		
	}
	
	private void processAndDisplay(final ImageRequest ir) {
		
		if (imageViewReusedOrBitmapNull(ir)) {
			return;
		}
		
		if (ir.getBitmapProcessor() != null) {
			

			Runnable processPoster = new Runnable() {
				
				public void run() {
					
					try {
						ir.add("processing-bitmap");
						Bitmap tmp = ir.getBitmapProcessor().process(ir.getBitmap());
						ir.setBitmap(tmp);
						display(ir);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			
			if (Looper.myLooper() == Looper.getMainLooper()) {
				mExecutor.execute(processPoster);
			} else {
				processPoster.run();
			}
			
		} else {
			display(ir);
		}
		
	}
	
	private void display(final ImageRequest ir) {
		
		Runnable work = new Runnable() {
			
			public void run() {
				
//				ir.isAlive("run-display");
				if (imageViewReusedOrBitmapNull(ir)) {
					return;
				}
				
				ir.finish("display-on-UI-thread");
				ir.getBitmapDisplayer().display(ir);
			}
		};
		
		if (Looper.myLooper() == Looper.getMainLooper()) {
//			ir.isAlive("just run");
			work.run();
		} else {
//			ir.isAlive("post run");
			ir.add("posting-to-UI-thread");
			mHandler.post(work);
		}
		
	}
	
	private boolean imageViewReusedOrBitmapNull(ImageRequest ir) {
		if (imageViewReused(ir)) {
			ir.finish("imageview-reused");
			return true;
		}

		if (ir.getBitmap()==null) {
			ir.finish("bitmap-is-null");
			return true;
		}
		return false;
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