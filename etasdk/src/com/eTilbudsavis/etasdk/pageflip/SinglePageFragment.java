package com.eTilbudsavis.etasdk.pageflip;

import android.view.View;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoDoubleClickListener;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoLongClickListener;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class SinglePageFragment extends PageFragment {
	
	public static final String TAG = Eta.TAG_PREFIX + SinglePageFragment.class.getSimpleName();
	
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
		load(ir, sampleSize, true);
	}
	
	@Override
	public void loadZoom() {
		getPhotoView().setTag(null);
		String url = getCallback().isLowMemory() ? getFirst().getView() : getFirst().getZoom();
		ImageRequest ir = new ImageRequest(url, getPhotoView());
		load(ir, 0, false);
	}
	
	private void load(ImageRequest ir, int sampleSize, boolean autoScale) {
		ir.setBitmapDisplayer(new PageFadeBitmapDisplayer());
		LowMemoryDecoder lmd = new LowMemoryDecoder(getActivity());
		lmd.setMinimumSampleSize(sampleSize);
		lmd.useAutoScale(autoScale);
		ir.setBitmapDecoder(lmd);
		ir.setBitmapProcessor(new PageBitmapProcessor(getFirstNum()));
		addRequest(ir);
	}
	
}
