package com.eTilbudsavis.etasdk.ImageLoader;

import java.util.List;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.DefaultBitmapDisplayer;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Log.EventLog;
import com.eTilbudsavis.etasdk.Log.EventLog.Event;

/**
 * The class for requesting images via the ImageLoader.
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public class ImageRequest {
	
	public static final String TAG = Eta.TAG_PREFIX + ImageRequest.class.getSimpleName();
	
	private final String mUrl;
	private final ImageView mImageView;
	private BitmapProcessor mPostProcessor;
	private Bitmap mBitmap;
	private int mPlaceholderLoading;
	private int mPlaceholderError;
	private BitmapDisplayer mDisplayer;
	private LoadSource mLoadSource;
	private boolean mFileCache = true;
	private boolean mMemoryCache = true;
	private BitmapDecoder mDecoder;
	private EventLog mLog = new EventLog();
	private ImageDebugger mDebugger;
	
	@SuppressWarnings("unused")
	private ImageRequest() {
		this(null, null);
	}

	public ImageRequest(String url, ImageView iv) {
		this.mUrl = url;
		this.mImageView = iv;
	}
	
	/**
	 * This method is invoked when bitmap loading is complete
	 */
	public void finish(String event) {
		add(event);
	}
	
	public void add(String event) {
		mLog.add(event);
	}
	
	/**
	 * Get the source url og the bitmap
	 * @return A url
	 */
	public String getUrl() {
		return mUrl;
	}
	
	/**
	 * Get the ImageView the bitmap is/will be displayed in
	 * @return An ImageView
	 */
	public ImageView getImageView() {
		return mImageView;
	}
	
	/**
	 * Get the BitmapProcessor that will perform operationa on the bitmap after
	 * ImageLoader have loaded the bitmap from a source
	 * @return A BitmapProcessor or {@code null}
	 */
	public BitmapProcessor getBitmapProcessor() {
		return mPostProcessor;
	}
	
	/**
	 * Set the BitmapProcessor that will perform operations to the bitmap after
	 * Ithe ImageLoader have loaded the bitmap from a source.
	 * @param processor
	 */
	public ImageRequest setBitmapProcessor(BitmapProcessor processor) {
		this.mPostProcessor = processor;
		return this;
	}

	/**
	 * Get the bitmap that hace been loaded
	 * @return A bitmap if one could be loaded, else {@code null}
	 */
	public Bitmap getBitmap() {
		return mBitmap;
	}
	
	/**
	 * Set a bitmap
	 * @param bitmap A bitmap
	 */
	public ImageRequest setBitmap(Bitmap bitmap) {
		this.mBitmap = bitmap;
		return this;
	}
	
	/**
	 * Get the decoder for this request
	 * @return A BitmapDecoder
	 */
	public BitmapDecoder getBitmapDecoder() {
		return mDecoder;
	}
	
	/**
	 * Set the BitmapDecoder for decoding the data from this request
	 * @param decoder
	 * @return
	 */
	public ImageRequest setBitmapDecoder(BitmapDecoder decoder) {
		this.mDecoder = decoder;
		return this;
	}
	
	public EventLog getLog() {
		return mLog;
	}
	
	public void isAlive(String msg) {
		if (mDebugger != null) {
			String[] parts = mUrl.split("/");
			List<Event> e = mLog.getEvents();
			EtaLog.d(TAG, "alive[" + parts[parts.length-1] + ", msg:" + msg + ", log:" + e.get(e.size()-1).name + "]");
		}
		
	}
	
	public ImageDebugger getDebugger() {
		return mDebugger;
	}
	
	public ImageRequest setDebugger(ImageDebugger d) {
		this.mDebugger = d;
		return this;
	}
	
	public boolean useFileCache() {
		return mFileCache;
	}
	
	public void setFileCache(boolean usrFileCache) {
		mFileCache = usrFileCache;
	}
	
	public boolean useMemoryCache() {
		return mMemoryCache;
	}
	
	public void setMemoryCache(boolean useMemoryCache) {
		mMemoryCache = useMemoryCache;
	}
	
	public int getPlaceholderLoading() {
		return mPlaceholderLoading;
	}
	
	public ImageRequest setPlaceholderLoading(int placeholderLoading) {
		this.mPlaceholderLoading = placeholderLoading;
		return this;
	}

	public int getPlaceholderError() {
		return mPlaceholderError;
	}

	public ImageRequest setPlaceholderError(int placeholderError) {
		this.mPlaceholderError = placeholderError;
		return this;
	}
	
	/**
	 * Get the BitmapDisplayer for displaying the bitmap in the imageview
	 * @return A BitmapDisplayer
	 */
	public BitmapDisplayer getBitmapDisplayer() {
		return (mDisplayer == null) ? new DefaultBitmapDisplayer() : mDisplayer;
	}
	
	/**
	 * Set the BitmapDisplayer for displaying the bitmap in the ImageView
	 * @param displayer A BitmapDisplayer
	 */
	public ImageRequest setBitmapDisplayer(BitmapDisplayer displayer) {
		this.mDisplayer = displayer;
		return this;
	}
	
	/**
	 * Get the source og the loaded bitmap
	 * @return A {@link LoadSource} or {@code null}
	 */
	public LoadSource getLoadSource() {
		return mLoadSource;
	}
	
	/**
	 * Set a {@link LoadSource} git the given bitmap.
	 * <p>This is set by {@link ImageLoader}</p>
	 * @param source A {@link LoadSource}
	 */
	public ImageRequest setLoadSource(LoadSource source) {
		this.mLoadSource = source;
		return this;
	}
	
	public static class Builder {
		
		private String url;
		private ImageView imageView;
		private BitmapProcessor processor;
		private int placeholderLoading = 0;
		private int placeholderError = 0;
		private BitmapDisplayer displayer;
		
		public Builder(String url, ImageView iv) {
			this.url = url;
			this.imageView = iv;
		}
		
		public ImageRequest create() {
			ImageRequest r = new ImageRequest(url, imageView);
			r.mPostProcessor = processor;
			r.mPlaceholderLoading = placeholderLoading;
			r.mPlaceholderError = placeholderError;
			r.mDisplayer = (displayer == null) ? new DefaultBitmapDisplayer() : displayer;
			return r;
		}
		
	}
	
}
