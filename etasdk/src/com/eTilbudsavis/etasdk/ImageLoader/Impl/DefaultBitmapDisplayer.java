package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class DefaultBitmapDisplayer implements BitmapDisplayer {
	
	public void display(ImageRequest ir) {
		
		if(ir.getBitmap() != null) {
			ir.getImageView().setImageBitmap(ir.getBitmap());
		} else if (ir.getPlaceholderError() != 0) {
			ir.getImageView().setImageResource(ir.getPlaceholderError());
		}
		
	}

}
