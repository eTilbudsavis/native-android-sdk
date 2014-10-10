package com.eTilbudsavis.etasdk.pageflip;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.ImageLoader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.ImageLoader.BitmapProcessor;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class PageflipDoublePage extends PageflipPage {

	public static PageflipPage newInstance(Catalog c, int page) {
		Bundle b = new Bundle();
		b.putSerializable(CATALOG, c);
		b.putInt(PAGE, page);
		PageflipPage f = new PageflipDoublePage();
		f.setArguments(b);
		return f;
	}
	
	Object LOCK = new Object();
	private ImageView mIVLeft;
	private ImageView mIVRight;
	private Bitmap mBitmapLeft;
	private Bitmap mBitmapRight;
	private Bitmap mBitmapMerged;
	
	@Override
	public void onAttach(Activity activity) {
		mIVLeft = new ImageView(activity);
		mIVRight = new ImageView(activity);
		super.onAttach(activity);
	}
	
	private void merge() {

		synchronized (LOCK) {
			if (mBitmapLeft != null && mBitmapRight != null) {
				mBitmapMerged = PageflipUtils.mergeImage(mBitmapLeft, mBitmapRight);
			}
		}
	}

	BitmapProcessor left = new BitmapProcessor() {
		
		public Bitmap process(Bitmap b) {

			synchronized (LOCK) {
				mBitmapLeft = b;
				merge();
			}
			return b;
		}
	};

	BitmapProcessor right = new BitmapProcessor() {
		
		public Bitmap process(Bitmap b) {

			synchronized (LOCK) {
				mBitmapRight = b;
				merge();
			}
			return b;
		}
	};
	
	BitmapDisplayer displayer = new BitmapDisplayer() {
		
		public void display(ImageRequest ir) {
			
			synchronized (LOCK) {
				if (mBitmapMerged!=null) {
					getPhotoView().setImageBitmap(mBitmapMerged);
				}
			}
			
		}
	};
	
	@Override
	public void loadPages() {
		if (getPhotoView().getDrawable() == null) {
			runImageloader(getPageLeft().getThumb(), mIVLeft, left);
			runImageloader(getPageRight().getThumb(), mIVRight, right);
		}
	}
	
	private void runImageloader(String url, ImageView i,BitmapProcessor p) {
		ImageRequest r = new ImageRequest(url, i);
		r.setBitmapDisplayer(displayer);
		r.setBitmapProcessor(p);
		ImageLoader.getInstance().displayImage(r);
	}
}
