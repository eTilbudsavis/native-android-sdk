package com.eTilbudsavis.etasdk.ImageLoader;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.etilbudsavis.etasdk.R;

/**
 * The class for requesting images via the ImageLoader.
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public class ImageRequest {
	
	public static final String TAG = ImageRequest.class.getSimpleName();
	
	public String url;
	public ImageView imageView;
	public BitmapProcessor processor;
	public Bitmap bitmap;
	public int placeholderLoading;
	public int placeholderError;
	public BitmapDisplayer displayer;
	public LoadSource source;
	public int downloadAttempts = 0;
	private long timeStart = 0L;
	private long timeComplete = 0L;
	
	public ImageRequest(String url, ImageView iv) {
		this(url, iv, 0, 0, null);
	}
	
	public ImageRequest(String url, ImageView iv, int loadingResId) {
		this(url, iv, loadingResId, 0, null);
	}
	
	public ImageRequest(String url, ImageView iv, int loadingResId, int errorResId, BitmapProcessor processor) {
		this.url = url;
		this.imageView = iv;
		this.placeholderLoading = loadingResId;
		this.placeholderError = errorResId;
		this.processor = processor;
	}
	
	public void start() {
		timeStart = System.currentTimeMillis();
	}
	
	public void finish() {
		timeComplete = System.currentTimeMillis() - timeStart;
	}
	
}
