package com.eTilbudsavis.etasdk.imageloader.impl;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.imageloader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;
import com.eTilbudsavis.etasdk.imageloader.LoadSource;

public class FadeBitmapDisplayer implements BitmapDisplayer {
	
	public static final String TAG = Constants.getTag(FadeBitmapDisplayer.class);
	
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
		
		if(ir.getBitmap() != null) {
			ir.getImageView().setImageBitmap(ir.getBitmap());
		} else if (ir.getPlaceholderError() != 0) {
			ir.getImageView().setImageResource(ir.getPlaceholderError());
		}
		
		if ( (ir.getLoadSource() == LoadSource.WEB && mFadeFromWeb) ||
				(ir.getLoadSource() == LoadSource.FILE && mFadeFromFile) ||
				(ir.getLoadSource() == LoadSource.MEMORY && mFadeFromMemory) ) {
			
			AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
			fadeImage.setDuration(mDuration);
			fadeImage.setInterpolator(new DecelerateInterpolator());
			ir.getImageView().startAnimation(fadeImage);
			
		}
	}
	
}
