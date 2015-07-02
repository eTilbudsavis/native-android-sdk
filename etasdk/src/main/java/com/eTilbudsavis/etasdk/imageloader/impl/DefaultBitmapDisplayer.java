package com.eTilbudsavis.etasdk.imageloader.impl;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.imageloader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;

public class DefaultBitmapDisplayer implements BitmapDisplayer {

    public static final String TAG = Constants.getTag(DefaultBitmapDisplayer.class);

    public void display(ImageRequest ir) {

        ir.isAlive("def-displayer");

        if (ir.getBitmap() != null) {
            ir.getImageView().setImageBitmap(ir.getBitmap());
        } else if (ir.getPlaceholderError() != 0) {
            ir.getImageView().setImageResource(ir.getPlaceholderError());
        }

    }

}
