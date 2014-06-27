package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.ImageLoader.LoadSource;

public class FadeBitmapDisplayer implements BitmapDisplayer {
	
	int mDuration = 100;
	private boolean mFadeFromMemory = true;
	private boolean mFadeFromFile = true;
	private boolean mFadeFromWeb = true;

	public FadeBitmapDisplayer() {
		this(100);
	}

	public FadeBitmapDisplayer(int durationInMillis) {
		this(durationInMillis, true, true, true);
	}

	public FadeBitmapDisplayer(int durationInMillis, boolean fadeFromMemory, boolean fadeFromFile, boolean fadeFromWeb) {
		mDuration = durationInMillis;
		mFadeFromMemory = fadeFromMemory;
		mFadeFromFile = fadeFromFile;
		mFadeFromWeb = fadeFromWeb;
	}
	
	public void display(ImageRequest ir) {
		
		if(ir.bitmap != null) {
			ir.imageView.setImageBitmap(ir.bitmap);
		} else if (ir.placeholderError != 0) {
			ir.imageView.setImageResource(ir.placeholderError);
		}
		
		
		if ( (ir.source == LoadSource.WEB && mFadeFromWeb) ||
				(ir.source == LoadSource.FILE && mFadeFromFile) ||
				(ir.source == LoadSource.MEMORY && mFadeFromMemory) ) {
			
			AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
			fadeImage.setDuration(mDuration);
			fadeImage.setInterpolator(new DecelerateInterpolator());
			ir.imageView.startAnimation(fadeImage);
			
		}
	}
	
}
