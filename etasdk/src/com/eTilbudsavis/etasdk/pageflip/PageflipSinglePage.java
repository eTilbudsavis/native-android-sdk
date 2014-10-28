package com.eTilbudsavis.etasdk.pageflip;

import android.view.View;

import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.DefaultBitmapDecoder;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class PageflipSinglePage extends PageflipPage {
	
	private OnPhotoTapListener mTapListener = new OnPhotoTapListener() {

		public void onPhotoTap(View view, float x, float y) {
			click(getPageNum(), x, y);
		}
	};
	
	public void onResume() {
		super.onResume();
		getPhotoView().setOnPhotoTapListener(mTapListener);
	};
	
	private int getPageNum() {
		return getPages()[0];
	}
	
	@Override
	public void loadView() {
		getPhotoView().setTag(null);
		load(new ImageRequest(getPage(getPageNum()).getView(), getPhotoView()));
	}
	
	@Override
	public void loadZoom() {
		getPhotoView().setTag(null);
		load(new ImageRequest(getPage(getPageNum()).getZoom(), getPhotoView()));
	}
	
	private void load(ImageRequest ir) {
		ir.setBitmapDisplayer(new PageflipBitmapDisplayer());
		ir.setBitmapProcessor(new PageflipBitmapProcessor(getCatalog(), getPageNum(), isLandscape(), debug));
		DefaultBitmapDecoder bd = new DefaultBitmapDecoder();
		ir.setBitmapDecoder(bd);
		addRequest(ir);
	}
	
}
