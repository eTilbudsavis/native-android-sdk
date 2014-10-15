package com.eTilbudsavis.etasdk.pageflip;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.OnHierarchyChangeListener;
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
	
	public static final String STATE_CURRENT_POSITION = "eta_sdk_pageflip_current_position";
	
	private static final String CATALOG = "catalog";
	protected static final String PAGE = "page_num";
	
	private ViewPager mPager;
	private PageflipAdapter mAdapter;
	private Catalog mCatalog;
	private int mCurrentPosition = 0;
	private boolean mLandscape = false;
	private FrameLayout mFrame;
	private Handler mHandler;
	
	Listener<Catalog> mFillListener = new Listener<Catalog>() {
		
		public void onComplete(Catalog c, EtaError error) {
			if (c != null) {
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
	public void onCreate(Bundle savedInstanceState) {
		
		if (getArguments() == null || !getArguments().containsKey(CATALOG)) {
			throw new IllegalArgumentException("No catalog provided");
		}
		
		mHandler = new Handler();
		mLandscape = PageflipUtils.isLandscape(getActivity());
		mCatalog = (Catalog)getArguments().getSerializable(CATALOG);
		int page = getArguments().getInt(PAGE);
		mCurrentPosition = PageflipUtils.pageToPosition(page, mLandscape);;
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		super.onCreateView(inflater, container, savedInstanceState);
		
		if (savedInstanceState != null) {
			mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_POSITION, mCurrentPosition);
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
		mPager = getPager();
		mFrame.addView(mPager);
	}
	
	private ViewPager getPager() {
		ViewPager p = new ViewPager(getActivity());
		ViewGroup.LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		p.setLayoutParams(lp);
		p.setId(View.NO_ID);
		p.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
			
			public void onChildViewRemoved(View parent, View child) {
				EtaLog.d(TAG, "onChildViewRemoved");
			}
			
			public void onChildViewAdded(View parent, View child) {
				EtaLog.d(TAG, "onChildViewRemoved");
			}
		});
		p.setOnPageChangeListener(this);
		return p;
	}
	
	private void resetAdapter() {
		EtaLog.d(TAG, "resetAdapter");
		mAdapter = new PageflipAdapter(getChildFragmentManager(), mCatalog, mLandscape);
		mPager.setAdapter(mAdapter);
		mHandler.postDelayed(new Runnable() {
			
			public void run() {
				EtaLog.d(TAG, "current: " + mCurrentPosition);
				mPager.setCurrentItem(mCurrentPosition, false);
			}
		}, 5000);
	}

	private void ensureCatalog() {
		
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean land = PageflipUtils.isLandscape(newConfig);
		EtaLog.d(TAG, "onConfigurationChanged[orientation.landscape[" + mLandscape + "->" + land + "]");
		if (land != mLandscape) {
			mLandscape = land;
			setUpView();
			ensureCatalog();
			
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_CURRENT_POSITION, mCurrentPosition);
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
	
	public interface PageflipListener {
		
		public void onPageChange(PageflipFragment f, int newPage);
		
		public void onOutOfBounds();
		
		public void onHotspotClick(String offerId);
		
		
	}
	
}
