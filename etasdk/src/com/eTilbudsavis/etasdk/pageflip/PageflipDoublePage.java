package com.eTilbudsavis.etasdk.pageflip;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class PageflipDoublePage extends PageflipPage {
	
	private Bitmap mTmpBitmap;
	
	BitmapDisplayer mLeftListener = new BitmapDisplayer() {
		
		public void display(ImageRequest ir) {
			if (mTmpBitmap == null) {
				mTmpBitmap = ir.getBitmap();
			} else {
				getPhotoView().setImageBitmap(PageflipUtils.mergeImage(ir.getBitmap(), mTmpBitmap));
				mTmpBitmap = null;
			}
		}
	};
	
	BitmapDisplayer mRightListener = new BitmapDisplayer() {
		
		public void display(ImageRequest ir) {
			if (mTmpBitmap == null) {
				mTmpBitmap = ir.getBitmap();
			} else {
				getPhotoView().setImageBitmap(PageflipUtils.mergeImage(mTmpBitmap, ir.getBitmap()));
				mTmpBitmap = null;
			}
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		
		if (getPhotoView().getDrawable() == null) {
			ImageLoader l = ImageLoader.getInstance();
			l.displayImage(new ImageRequest(getLeftPage().getThumb(), getPhotoView()).setBitmapDisplayer(mLeftListener));
			l.displayImage(new ImageRequest(getRightPage().getThumb(), getPhotoView()).setBitmapDisplayer(mRightListener));
		}
		
		return v;
		
	}
	
}
