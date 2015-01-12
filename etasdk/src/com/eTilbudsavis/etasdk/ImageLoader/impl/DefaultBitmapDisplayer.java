package com.eTilbudsavis.etasdk.imageloader.impl;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.imageloader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;

public class DefaultBitmapDisplayer implements BitmapDisplayer {
	
	public static final String TAG = Eta.TAG_PREFIX + DefaultBitmapDisplayer.class.getSimpleName();
	
	public void display(ImageRequest ir) {
		
		ir.isAlive("def-displayer");
		
		if(ir.getBitmap() != null) {
			ir.getImageView().setImageBitmap(ir.getBitmap());
		} else if (ir.getPlaceholderError() != 0) {
			ir.getImageView().setImageResource(ir.getPlaceholderError());
		}
		
	}

}
