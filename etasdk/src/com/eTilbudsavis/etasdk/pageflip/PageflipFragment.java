package com.eTilbudsavis.etasdk.pageflip;

import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;
import com.eTilbudsavis.etasdk.request.impl.CatalogObjectRequest.CatalogAutoFill;

public class PageflipFragment extends Fragment implements PageCallback, OnPageChangeListener {
	
	public static final String TAG = PageflipFragment.class.getSimpleName();
	
	private static final long LOW_MEMORY_BOUNDARY = 42 * 1024 * 1024;
	private static final double PAGER_SCROLL_FACTOR = 0.5d;
	private static final int PAGER_ID = 0xfedcba;

	private static final String ARG_CATALOG = "eta_sdk_pageflip_catalog";
	private static final String ARG_PAGE = "eta_sdk_pageflip_page";
	
	private static final String STATE_CATALOG = "eta_sdk_pageflip_state_catalog";
	private static final String STATE_PAGE = "eta_sdk_pageflip_state_page";
	
	private PageflipViewPager mPager;
	private PageflipAdapter mAdapter;
	private Catalog mCatalog;
	private int mStartPage = 0;
	private int mCurrentPosition = 0;
	private boolean mLandscape = false;
	private FrameLayout mFrame;
	private Handler mHandler;
	private PageflipListenerWrapper mWrapperListener = new PageflipListenerWrapper();
	private boolean mLowMemoryDevice = false;
	private StatsCollect mCollector;

	int mOutOfBoundsPX = 0;
	int mOutOfBoundsCount = 0;
	boolean mOutOfBoundsCalled = false;
	
	Runnable mOnCatalogComplete = new Runnable() {
		
		public void run() {
			setBranding();
			mCollector = new StatsCollectImpl(mCatalog);
			mAdapter = new PageflipAdapter(getChildFragmentManager(), PageflipFragment.this);
			mPager.setAdapter(mAdapter);
			mPager.setCurrentItem(mCurrentPosition);
		}
		
	};
	
	public static PageflipFragment newInstance(Catalog c) {
		return newInstance(c, 1);
	}

	public static PageflipFragment newInstance(Catalog c, int page) {
		page = pageValidator(c, page);
		Bundle b = new Bundle();
		b.putSerializable(ARG_CATALOG, c);
		b.putInt(ARG_PAGE, page);
		PageflipFragment f = new PageflipFragment();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onAttach(Activity activity) {
		EtaLog.d(TAG, "onAttach");
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		mLowMemoryDevice = Runtime.getRuntime().maxMemory() < LOW_MEMORY_BOUNDARY;
		
		mHandler = new Handler();
		mLandscape = PageflipUtils.isLandscape(getActivity());
		if (getArguments()!=null) {
			mCatalog = (Catalog)getArguments().getSerializable(ARG_CATALOG);
			mStartPage = getArguments().getInt(ARG_PAGE, 1);
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		setUpView(true);
		
		if (savedInstanceState != null) {
			setPage(savedInstanceState.getInt(STATE_PAGE, mCurrentPosition));
			mCatalog = (Catalog) savedInstanceState.getSerializable(STATE_CATALOG);
		}
		
		return mFrame;
		
	}
	
	private void setUpView(boolean removeParent) {
		if (mFrame == null) {
			mFrame = new FrameLayout(getActivity());
			ViewGroup.LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			mFrame.setLayoutParams(lp);

			createPager();
			
		} else {
			ViewGroup parent = (ViewGroup) mFrame.getParent();
			if (parent!=null && removeParent) {
				parent.removeView(mFrame);
			}
			mFrame.removeAllViews();
		}
		
		// TODO add a loader view
		
		mFrame.addView(mPager);
	}
	
	private void createPager() {
		mPager = new PageflipViewPager(getActivity());
		mPager.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mPager.setId(PAGER_ID);
		mPager.setScrollDurationFactor(PAGER_SCROLL_FACTOR);
		mPager.setOnPageChangeListener(this);
	}
	
	private static int pageValidator(Catalog c, int page) {
		if (!PageflipUtils.isValidPage(c, page)) {
			EtaLog.i(TAG, "Page (" + page + ") not within catalog.pages range (1-" +c.getPageCount()+ "). Setting page to 1");
			return 1;
		}
		return page;
	}
	
	private void setBranding() {
		mFrame.setBackgroundColor(mCatalog.getBranding().getColor());
	}
	
	private void ensureCatalog() {
		
		CatalogAutoFill caf = new CatalogAutoFill();
		caf.setLoadDealer(mCatalog.getDealer()==null);
		caf.setLoadHotspots(mCatalog.getHotspots()==null);
		caf.setLoadPages(mCatalog.getPages()==null);
		caf.setLoadStore(mCatalog.getStore() == null);
		AutoFillParams p = new AutoFillParams();
		caf.prepare(p, mCatalog, null, new Listener<Catalog>() {
			
			public void onComplete(Catalog c, EtaError error) {
				if (!isAdded()) {
					return;
				}
				if ( c!=null && c.getPages()!=null && c.getHotspots()!=null ) {
					if (Looper.getMainLooper() == Looper.myLooper()) {
						mOnCatalogComplete.run();
					} else {
						mHandler.post(mOnCatalogComplete);
					}
				} else {
					
					EtaLog.d(TAG, error.toJSON().toString());
					// TODO improve error stuff 1 == network error
					mWrapperListener.onError(error);
					
				}
			}
		});
		caf.execute(Eta.getInstance().getRequestQueue());
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean land = PageflipUtils.isLandscape(newConfig);
		if (land != mLandscape) {
			EtaLog.d(TAG, "onConfigurationChanged[orientation.landscape[" + mLandscape + "->" + land + "]");
			// Get the old page
			int[] pages = PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape);
			// switch to landscape mode
			mLandscape = land;
			// set new current position accordingly
//			setPage(pages[0]);
			mCurrentPosition = PageflipUtils.pageToPosition(pages[0], mLandscape);
			setUpView(false);
			start();
		}
	}
	
	public PageflipListener getListener() {
		return mWrapperListener.getListener();
	}
	
	public PageflipListener getWrapperListener() {
		return mWrapperListener;
	}
	
	public void setPageflipListener(PageflipListener l) {
		mWrapperListener.setListener(l);;
	}
	
	public int[] getPages() {
		return PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape);
	}
	
	public void setPage(int page) {
		if (PageflipUtils.isValidPage(mCatalog, page)) {
			setPosition(PageflipUtils.pageToPosition(page, mLandscape));
		} else {
			EtaLog.i(TAG, "Not a valid page number");
		}
	}
	
	public int getPosition() {
		return mCurrentPosition;
	}
	
	public void setPosition(int position) {
		mCurrentPosition = position;
		if (mPager != null) {
			mPager.setCurrentItem(mCurrentPosition);
		}
	}

	public void nextPage() {
		mPager.setCurrentItem(mCurrentPosition+1, true);
	}

	public void previousPage() {
		mPager.setCurrentItem(mCurrentPosition-1, true);
	}
	
	public boolean isReady() {
		return mAdapter != null;
	}
	
	public void setCatalog(Catalog c) {
		mCatalog = c;
	}
	
	public void start() {
		if (mCatalog!=null) {
			setBranding();
			ensureCatalog();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putIntArray(STATE_PAGE, PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape));
		outState.putSerializable(STATE_CATALOG, mCatalog);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		EtaLog.d(TAG, "onResume");
		if (mCatalog!=null) {
			start();
		}
	}
	
	@Override
	public void onPause() {
		//TODO collect stats
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		EtaLog.d(TAG, "onDestroy");
		super.onDestroy();
	}
	
	public boolean onBackPressed() {
		return false;
	}
	
	public void onPageSelected(int position) {
		mCollector.collectView(mLandscape, getPages());
		mCurrentPosition = position;
		mWrapperListener.onPageChange(PageflipUtils.positionToPages(position, mCatalog.getPageCount(), mLandscape));
	}
	
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		boolean isLeft = mCurrentPosition==0;
		if ( isReady() && !mOutOfBoundsCalled && (isLeft || mCurrentPosition==mAdapter.getCount()-1 ) && mOutOfBoundsCount > 3 && mOutOfBoundsPX < 2) {
			mWrapperListener.onOutOfBounds(isLeft);
			mOutOfBoundsCalled = true;
		}
		mOutOfBoundsCount++;
		mOutOfBoundsPX += positionOffsetPixels;
	}
	
	public String tag() {
		if (mCatalog==null) {
			return TAG;
		} else {
			return mCatalog.getId();
		}
	}
	
	public void onPageScrollStateChanged(int state) {
		if (state == ViewPager.SCROLL_STATE_IDLE) {
			mOutOfBoundsPX = 0;
			mOutOfBoundsCount = 0;
			mOutOfBoundsCalled = false;
		}
		mWrapperListener.onDragStateChanged(state);
	}
	
	public Catalog getCatalog() {
		return mCatalog;
	}

	public boolean isLandscape() {
		return mLandscape;
	}
	
	public boolean isPositionSet() {
		return mPager.getCurrentItem() == mCurrentPosition;
	}
	
	public boolean isLowMemory() {
		return mLowMemoryDevice;
	}
	
	public void onZoom(View v, boolean zoomIn) {
		mCollector.collectZoom(zoomIn, mLandscape, getPages());
		mWrapperListener.onZoom(v, getPages(), zoomIn);
	}
	
	protected class PageflipListenerWrapper implements PageflipListener {
		
		protected PageflipListener mListener;
		private static final boolean LOG = false;
		
		private boolean post() {
			return mListener != null;
		}
		
		public void setListener(PageflipListener l) {
			mListener = l;
		}
		
		public PageflipListener getListener() {
			return mListener;
		}

		public void onPageChange(int[] pages) {
			log("onPageChange: " + PageflipUtils.join(",", pages));
			if (post()) mListener.onPageChange(pages);
		}
		
		public void onOutOfBounds(boolean left) {
			log("onOutOfBounds." + (left ? "left" : "right"));
			if (post()) mListener.onOutOfBounds(left);
		}
		
		public void onError(EtaError error) {
			log("onError: " + error.toJSON().toString());
			if (post()) mListener.onError(error);
		}
		
		public void onDragStateChanged(int state) {
			log("onDragStateChanged: " + state);
			if (post()) mListener.onDragStateChanged(state);
		}

		public void onSingleClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
			log("single", page, x, y, hotspots);
			if (post()) mListener.onSingleClick(v, page, x, y, hotspots);
		}
		
		long s = -1;
		public void onDoubleClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
			log("double", page, x, y, hotspots);
			if (post()) mListener.onDoubleClick(v, page, x, y, hotspots);
		}

		public void onLongClick(View v, int page, float x, float y, List<Hotspot> hotspots) {
			log("long", page, x, y, hotspots);
			if (post()) mListener.onLongClick(v, page, x, y, hotspots);
		}

		public void onZoom(View v, int[] pages, boolean zoonIn) {
			log("onZoom.pages: " + PageflipUtils.join(",", pages) + ", zoomIn: " + zoonIn);
			if (post()) mListener.onZoom(v, pages, zoonIn);
		}
		
		private void log(String method, int page, float x, float y, List<Hotspot> hotspots) {
			StringBuilder sb = new StringBuilder();
			sb.append(method).append("[");
			sb.append("page").append(page);
//			sb.append(", x:").append(x).append(", y:").append(y);
			sb.append(", hotspot:");
			boolean first = true;
			for(Hotspot h : hotspots) {
				if (!first) {
					sb.append(", ");
				}
				first = false;
				sb.append(h.getOffer().getHeading());
			}
			sb.append("]");
			String msg = sb.toString();
			log(msg);
			if (LOG) {
				Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
			}
		}
		
		private void log(String message) {
			if (LOG) {
				EtaLog.d(TAG, message);
			}
		}
		
	}

}
