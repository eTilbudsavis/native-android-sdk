package com.eTilbudsavis.etasdk.pageflip;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eTilbudsavis.etasdk.R;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.photoview.PhotoView;

public class SinglePage extends PageflipPage {
	
	public static final String TAG = SinglePage.class.getSimpleName();
	
	private PhotoView mPhotoView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		EtaLog.d(TAG, "onCreateView");
		mPhotoView = (PhotoView) inflater.inflate(R.layout.singlepage, container);
		return mPhotoView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		ImageLoader.getInstance().displayImage(new ImageRequest(getPage().getView(), mPhotoView));
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
//		outState.putFloat(ZOOM, mPhotoView.getScale());
	}
	
}
