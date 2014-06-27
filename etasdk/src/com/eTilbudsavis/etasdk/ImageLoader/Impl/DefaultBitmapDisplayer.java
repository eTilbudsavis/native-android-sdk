package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class DefaultBitmapDisplayer implements BitmapDisplayer {
	
	public void display(ImageRequest ir) {
		
		if(ir.bitmap != null) {
			ir.imageView.setImageBitmap(ir.bitmap);
		} else if (ir.placeholderError != 0) {
			ir.imageView.setImageResource(ir.placeholderError);
		}
		
	}

}
