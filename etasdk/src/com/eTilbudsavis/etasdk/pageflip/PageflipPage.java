package com.eTilbudsavis.etasdk.pageflip;

import android.app.Activity;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eTilbudsavis.etasdk.R;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Page;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.photoview.PhotoView;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnMatrixChangedListener;

public abstract class PageflipPage extends Fragment {
	
	public static final String TAG = PageflipPage.class.getSimpleName();
	
	protected static final String CATALOG = "catalog";
	protected static final String PAGE = "page_num";
	
	private Catalog mCatalog;
	private int mPageNum = 0;
	private PhotoView mPhotoView;
	
	public abstract void loadPages();
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getArguments()!=null) {
			mCatalog = (Catalog)getArguments().getSerializable(CATALOG);
			mPageNum = getArguments().getInt(PAGE);
		}
		if (mCatalog==null) {
			throw new IllegalStateException("Catalog, and page number must be provided as argument");
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.etasdk_layout_page, container, false);
		mPhotoView = (PhotoView) v.findViewById(R.id.etasdk_pageflip_left_page);
		mPhotoView.setOnMatrixChangeListener(new OnMatrixChangedListener() {
			
			public void onMatrixChanged(RectF rect) {
				boolean intercept = almost(mPhotoView.getScale(), mPhotoView.getMinimumScale());
				mPhotoView.setAllowParentInterceptOnEdge(intercept);
			}
		});
		return v;
	}
	
	private boolean almost(float first, float second) {
		return Math.abs(first-second)<0.1;
	}
	
	@Override
	public void onResume() {
		
		loadPages();
		super.onResume();
	}
	
	protected PhotoView getPhotoView() {
		return mPhotoView;
	}
	
	protected Page getPageLeft() {
		return mCatalog.getPages().get(mPageNum);
	}
	
	protected Page getPageRight() {
		try {
			return mCatalog.getPages().get(mPageNum+1);
		} catch (IndexOutOfBoundsException e) {
			EtaLog.i(TAG, "No more pages");
			return null;
		}
	}
	
}
