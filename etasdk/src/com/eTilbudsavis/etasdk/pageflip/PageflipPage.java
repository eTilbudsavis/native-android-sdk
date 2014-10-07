package com.eTilbudsavis.etasdk.pageflip;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Page;

public class PageflipPage extends Fragment {
	
	protected static final String CATALOG = "catalog";
	protected static final String PAGE = "page_num";
	protected static final String ZOOM = "zoom";
	
	private Catalog mCatalog;
	private int mPageNum = 0;
	
	public static PageflipPage newInstance(Catalog c, int position, boolean landscape) {
		Bundle b = new Bundle();
		b.putSerializable(CATALOG, c);
		b.putInt(PAGE, landscape ? PageflipUtils.positionToPage(position) : position);
		PageflipPage f = landscape ? new DoublePage() : new SinglePage();
		f.setArguments(b);
		return f;
	}
	
	protected Catalog getCatalog() {
		return mCatalog;
	}
	
	protected Page getPage() {
		return mCatalog.getPages().get(mPageNum-1);
	}
	
	protected int getPageNum() {
		return mPageNum;
	}
	
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

	protected boolean isLandscape() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
//		outState.putFloat(ZOOM, mPhotoView.getScale());
	}
	
}
