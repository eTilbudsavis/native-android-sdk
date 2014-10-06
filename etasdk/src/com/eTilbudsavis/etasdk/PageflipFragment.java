package com.eTilbudsavis.etasdk;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class PageflipFragment extends Fragment {
	
	public static final String TAG = PageflipFragment.class.getSimpleName();
	private static final String CATALOG = "catalog";
	
	private ViewPager mPager;
	private PageflipAdapter mAdapter;
	private Catalog mCatalog;
	private int mCurrentPage = 0;
	
	public static PageflipFragment newInstance(Catalog c) {
		Bundle b = new Bundle();
		b.putSerializable(CATALOG, c);
		PageflipFragment f = new PageflipFragment();
		f.setArguments(b);
		return f;
	}
	
	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
			
			public void onPageSelected(int arg0) {
				
			}
			
			public void onPageScrollStateChanged(int arg0) {
				
			}
			
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
	};
			
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getArguments() != null) {
			
			if(getArguments().containsKey(CATALOG)) {
				mCatalog = (Catalog)getArguments().getSerializable(CATALOG);
			} else {
				EtaLog.w(TAG, "No catalog provided");
			}
			
		}
	}

	int fakeLayout = 0;
	int fakePager = 0;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View v = inflater.inflate(fakeLayout, container);
		mPager = (ViewPager) getView().findViewById(fakePager);
		
		mAdapter = new PageflipAdapter(getChildFragmentManager());
		mPager.setAdapter(mAdapter);
		
		mPager.setOnPageChangeListener(mPageChangeListener);
		
		return getView();
		
	}
	
}
