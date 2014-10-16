package com.eTilbudsavis.etasdk.pageflip;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;
import com.eTilbudsavis.etasdk.request.impl.CatalogObjectRequest.CatalogAutoFill;

public class PageflipFragment extends Fragment implements OnPageChangeListener {
	
	public static final String TAG = PageflipFragment.class.getSimpleName();
	
	private static final String ARG_CATALOG = "eta_sdk_pageflip_catalog";
	private static final String ARG_PAGE = "eta_sdk_pageflip_page";
	
	private PageflipViewPager mPager;
	private PageflipAdapter mAdapter;
	private Catalog mCatalog;
	private int mCurrentPosition = 0;
	private boolean mLandscape = false;
	private FrameLayout mFrame;
	private Handler mHandler;
	
	Listener<Catalog> mFillListener = new Listener<Catalog>() {
		
		public void onComplete(Catalog c, EtaError error) {
			if (c != null) {
				mHandler.post(resetAdapter);
			} else {
				EtaLog.e(TAG, error.getMessage(), error);
			}
		}
	};
	
	public static PageflipFragment newInstance(Catalog c) {
		return newInstance(c, 1);
	}

	public static PageflipFragment newInstance(Catalog c, int page) {
		if (c==null) {
			throw new IllegalArgumentException("Catalog cannot be null");
		}
		if (!PageflipUtils.withInPageRange(c, page)) {
			EtaLog.i(TAG, "Page (" + page + ") not within catalog.pages range (1-" +c.getPageCount()+ "). Setting page to 0");
			page = 0;
		}
		Bundle b = new Bundle();
		b.putSerializable(ARG_CATALOG, c);
		b.putInt(ARG_PAGE, page);
		PageflipFragment f = new PageflipFragment();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		PageflipUtils.test();
		
		if (getArguments() == null || !getArguments().containsKey(ARG_CATALOG)) {
			throw new IllegalArgumentException("No catalog provided");
		}
		
		mHandler = new Handler();
		mLandscape = PageflipUtils.isLandscape(getActivity());
		mCatalog = (Catalog)getArguments().getSerializable(ARG_CATALOG);
		setCurrentPosition(getArguments().getInt(ARG_PAGE, 0));
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		super.onCreateView(inflater, container, savedInstanceState);
		
		if (savedInstanceState != null) {
			setCurrentPosition(savedInstanceState.getInt(ARG_PAGE, mCurrentPosition));
		}
		
		setUpView();
		
		return mFrame;
		
	}
	
	private void setUpView() {
		if (mFrame == null) {
			mFrame = new FrameLayout(getActivity());
			ViewGroup.LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			mFrame.setLayoutParams(lp);
		} else {
			mFrame.removeAllViews();
		}
		resetPager();
		mFrame.addView(mPager);
	}
	
	private void resetPager() {
		mPager = new PageflipViewPager(getActivity());
		mPager.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mPager.setId(0xfedcba);
		mPager.setScrollDurationFactor(0.5);
		mPager.setOnPageChangeListener(this);
	}
	
	Runnable resetAdapter = new Runnable() {
		
		public void run() {
			mAdapter = new PageflipAdapter(getChildFragmentManager(), mCatalog, mLandscape);
			mPager.setAdapter(mAdapter);
			mPager.setCurrentItem(mCurrentPosition);
		}
	};
	
	private void ensureCatalog() {
		
		CatalogAutoFill caf = new CatalogAutoFill();
		caf.setLoadDealer(mCatalog.getDealer()==null);
		caf.setLoadHotspots(mCatalog.getHotspots()==null);
		caf.setLoadPages(mCatalog.getPages()==null);
		caf.setLoadStore(mCatalog.getStore() == null);
		AutoFillParams p = new AutoFillParams();
		caf.prepare(p, mCatalog, null, mFillListener);
		caf.execute(Eta.getInstance().getRequestQueue());
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean land = PageflipUtils.isLandscape(newConfig);
//		EtaLog.d(TAG, "onConfigurationChanged[orientation.landscape[" + mLandscape + "->" + land + "]");
		if (land != mLandscape) {
			// Get the old page
			int page = PageflipUtils.positionToPage(mCurrentPosition, mLandscape);
			// switch to landscape mode
			mLandscape = land;
			// set new current position accordingly
			setCurrentPosition(page);
			setUpView();
			ensureCatalog();
			
		}
	}
	
	private void setCurrentPosition(int page) {
		if (PageflipUtils.withInPageRange(mCatalog, page)) {
			mCurrentPosition = PageflipUtils.pageToPosition(page, mLandscape);
		} else {
			EtaLog.i(TAG, "Page (" + page + ") not within catalog.pages range (1-" +mCatalog.getPageCount()+ "). Setting page to 0");

		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ARG_PAGE, PageflipUtils.positionToPage(mCurrentPosition, mLandscape));
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		ensureCatalog();
	}
	
	@Override
	public void onPause() {
		//TODO collect stats
		super.onPause();
	}
	
	public void onPageSelected(int position) {
		mCurrentPosition = position;
	}
	
	public void onPageScrollStateChanged(int arg0) {
		
	}
	
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		
	}
	
}
