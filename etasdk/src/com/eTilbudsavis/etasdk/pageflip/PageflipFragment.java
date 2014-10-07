package com.eTilbudsavis.etasdk.pageflip;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.R;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Impl.DefaultDebugger;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;
import com.eTilbudsavis.etasdk.request.impl.CatalogObjectRequest.CatalogAutoFill;

public class PageflipFragment extends Fragment implements OnPageChangeListener {
	
	public static final String TAG = PageflipFragment.class.getSimpleName();
	private static final String CATALOG = "catalog";
	protected static final String PAGE = "page_num";
	
	private ViewPager mPager;
	private PageflipAdapter mAdapter;
	private Catalog mCatalog;
	private int mCurrentPosition = 0;
	private boolean mLandscape = false;
	CatalogAutoFill caf;
	
	Listener<Catalog> mFillListener = new Listener<Catalog>() {
		
		public void onComplete(Catalog response, EtaError error) {
			EtaLog.d(TAG, "Request complete");
			if (response != null) {
				resetAdapter();
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
		if (page <=0 || c.getPageCount() < page) {
			page = 1;
//			throw new IllegalArgumentException(
//					c.getPageCount() + " not allowed, page must be with in the range of the catalog pages (1-" +c.getPageCount()+ ")");
		}
		Bundle b = new Bundle();
		b.putSerializable(CATALOG, c);
		b.putInt(PAGE, page);
		PageflipFragment f = new PageflipFragment();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mLandscape = isLandscape();
		if (getArguments() != null) {
			
			if(getArguments().containsKey(CATALOG)) {
				mCatalog = (Catalog)getArguments().getSerializable(CATALOG);
				int page = getArguments().getInt(PAGE);
				mCurrentPosition = (mLandscape ? PageflipUtils.pageToPosition(mCurrentPosition) : page);
			} else {
				EtaLog.w(TAG, "No catalog provided");
			}
			
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View v = inflater.inflate(R.layout.pageflip, container);
		mPager = (ViewPager) v.findViewById(R.id.viewpager);
//		mPager = new ViewPager(getActivity());
//		ViewGroup.LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//		mPager.setLayoutParams(lp);
		
		mPager.setOnPageChangeListener(this);
		return v;
		
	}
	
	private void resetAdapter() {
		EtaLog.d(TAG, "resetAdapter");
		mAdapter = new PageflipAdapter(getChildFragmentManager(), mCatalog, mLandscape);
		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(mCurrentPosition);
	}

	private void ensureCatalog() {
		EtaLog.d(TAG, "ensureCatalog:" + mCatalog.getBranding().getName());
		CatalogAutoFill caf = new CatalogAutoFill();
		caf.setLoadDealer(mCatalog.getDealer()==null);
		caf.setLoadHotspots(mCatalog.getHotspots()==null);
		caf.setLoadPages(mCatalog.getPages()==null);
		caf.setLoadStore(mCatalog.getStore() == null);
		AutoFillParams p = new AutoFillParams();
//		p.setDebugger(new DefaultDebugger());
		caf.prepare(p, mCatalog, null, mFillListener);
		caf.execute(Eta.getInstance().getRequestQueue());
		
	}
	
	private boolean isLandscape() {
		return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean land = isLandscape();
		if (land != mLandscape) {
			mLandscape = land;
			ensureCatalog();
		}
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
	
	public interface PageflipListener {
		
		public void onPageChange(PageflipFragment f, int newPage);
		
		public void onOutOfBounds();
		
		public void onHotspotClick(String offerId);
		
		
	}
	
}
