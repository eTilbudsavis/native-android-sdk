package com.eTilbudsavis.etasdk.pageflip;

import android.view.View;

import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class SinglePageFragment extends PageFragment {
	
	public void onResume() {
		super.onResume();
		getPhotoView().setOnPhotoTapListener(new OnPhotoTapListener() {

			public void onPhotoTap(View view, float x, float y) {
				onClick(getFirstNum(), x, y);
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
		addRequest(ir);
	}
	
}
