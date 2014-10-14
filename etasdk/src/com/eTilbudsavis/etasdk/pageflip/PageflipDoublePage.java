package com.eTilbudsavis.etasdk.pageflip;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.widget.ImageView;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.ImageLoader.BitmapProcessor;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

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
	private Bitmap mLeft;
	private Bitmap mRight;
	private Bitmap mPage;
	
	@Override
	public void onAttach(Activity activity) {
		mIVLeft = new ImageView(activity);
		mIVRight = new ImageView(activity);
		super.onAttach(activity);
	}
	
	private void merge() {
		
		if (mLeft == null || mRight == null) {
			return;
		}
		int w = mLeft.getWidth()*2;
		int h = mLeft.getHeight();
		mPage = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(mPage);
		canvas.drawColor(0, Mode.CLEAR);
		canvas.drawBitmap(mLeft, 0, 0, null);
		canvas.drawBitmap(mRight, (w/2), 0, null);
		clean();
		
	}
	
	private void clean() {
		mLeft.recycle();
		mRight.recycle();
		mLeft = null;
		mRight = null;
	}
	
	BitmapProcessor left = new BitmapProcessor() {
		
		public Bitmap process(Bitmap b) {
			
			synchronized (LOCK) {
				mLeft = b;
				merge();
			}
			return b;
			
		}
	};

	BitmapProcessor right = new BitmapProcessor() {
		
		public Bitmap process(Bitmap b) {

			synchronized (LOCK) {
				mRight = b;
				merge();
			}
			return b;
		}
	};
	
	PageflipBitmapDisplayer mDisplayer = new PageflipBitmapDisplayer() {
		
		public void display(ImageRequest ir) {
			if (mPage!=null) {
				ir.setBitmap(mPage);
				super.display(ir);
			}
		};
	};
	
	@Override
	public void loadPages() {
		if (getPhotoView().getDrawable() == null) {
			runImageloader(getPageLeft().getView(), mIVLeft, left);
			runImageloader(getPageRight().getView(), mIVRight, right);
		}
	}
	
	private void runImageloader(String url, ImageView i,BitmapProcessor p) {
		ImageRequest r = new ImageRequest(url, i);
		r.setBitmapDisplayer(mDisplayer);
		r.setBitmapProcessor(p);
		addRequest(r);
	}
	
}
