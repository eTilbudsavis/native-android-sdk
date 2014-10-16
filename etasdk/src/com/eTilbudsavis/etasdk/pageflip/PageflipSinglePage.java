package com.eTilbudsavis.etasdk.pageflip;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class PageflipSinglePage extends PageflipPage {
	
	public static PageflipPage newInstance(Catalog c, int page) {
		Bundle b = new Bundle();
		b.putSerializable(ARG_CATALOG, c);
		b.putInt(ARG_PAGE, page);
		PageflipPage f = new PageflipSinglePage();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View v = super.onCreateView(inflater, container, savedInstanceState);
		
		getPhotoView().setOnPhotoTapListener(new OnPhotoTapListener() {

			public void onPhotoTap(View view, float x, float y) {
				
				click(getPage(), x, y);
			}
		});

		return v;
	}
	
	@Override
	public void loadPages() {
		ImageRequest ir = new ImageRequest(getPageLeft().getView(), getPhotoView());
		ir.setBitmapDisplayer(new PageflipBitmapDisplayer());
		addRequest(ir);
	}
	
}
