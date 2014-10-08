package com.eTilbudsavis.etasdk.pageflip;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class PageflipSinglePage extends PageflipPage {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		ImageLoader.getInstance().displayImage(new ImageRequest(getLeftPage().getThumb(), getPhotoView()));
		return v;
		
	}
}
