package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class DefaultBitmapDisplayer implements BitmapDisplayer {
	
	public void display(ImageRequest ir) {
		
		if(ir.mBitmap != null) {
			ir.getImageView().setImageBitmap(ir.mBitmap);
		} else if (ir.mPlaceholderError != 0) {
			ir.getImageView().setImageResource(ir.mPlaceholderError);
		}
		
	}

}
