package com.eTilbudsavis.etasdk.pageflip;

import android.graphics.Bitmap;
import android.view.View;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapProcessor;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoDoubleClickListener;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoLongClickListener;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class SinglePageFragment extends PageFragment {
	
	public void onResume() {
		super.onResume();
		getPhotoView().setOnPhotoTapListener(new OnPhotoTapListener() {

			public void onPhotoTap(View view, float x, float y) {
				onSingleClick(getFirstNum(), x, y);
			}
		});
		getPhotoView().setOnPhotoDoubleClickListener(new OnPhotoDoubleClickListener() {
			
			public void onPhotoTap(View view, float x, float y) {
				onDoubleClick(getFirstNum(), x, y);
			}
		});
		getPhotoView().setOnPhotoLongClickListener(new OnPhotoLongClickListener() {
			
			public void onPhotoTap(View view, float x, float y) {
				onLongClick(getFirstNum(), x, y);
			}
		});
	};
	
	@Override
	public void loadView() {
		getPhotoView().setTag(null);
		int sampleSize = getCallback().isLowMemory() ? 2 : 0;
		ImageRequest ir = new ImageRequest(getFirst().getView(), getPhotoView());
		load(ir, sampleSize);
	}
	
	@Override
	public void loadZoom() {
		getPhotoView().setTag(null);
		String url = getCallback().isLowMemory() ? getFirst().getView() : getFirst().getZoom();
		ImageRequest ir = new ImageRequest(url, getPhotoView());
		load(ir, 0);
	}
	
	private void load(ImageRequest ir, int sampleSize) {
		ir.setBitmapDisplayer(new PageFadeBitmapDisplayer());
		ir.setBitmapDecoder(new LowMemoryDecoder(sampleSize));
		ir.setBitmapProcessor(new PageBitmapProcessor(getFirstNum()));
		addRequest(ir);
	}
	
}
