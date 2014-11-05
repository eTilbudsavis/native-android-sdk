package com.eTilbudsavis.etasdk.pageflip;

import java.util.List;

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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.R;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;
import com.eTilbudsavis.etasdk.request.impl.CatalogObjectRequest.CatalogAutoFill;

public class PageflipFragment extends Fragment implements PageCallback, OnPageChangeListener {
	
	public static final String TAG = PageflipFragment.class.getSimpleName();
	
	private static final double PAGER_SCROLL_FACTOR = 0.5d;
	
	private static final String ARG_CATALOG = "eta_sdk_pageflip_catalog";
	private static final String ARG_PAGE = "eta_sdk_pageflip_page";
	
	private static final String STATE_CATALOG = "eta_sdk_pageflip_state_catalog";
	private static final String STATE_PAGE = "eta_sdk_pageflip_state_page";
	
	// Need this
	private Catalog mCatalog;
	
	// Views
	private LayoutInflater mInflater;
	private ViewGroup mContainer;
	private FrameLayout mFrame;
	private TextView mProgress;
	private PageflipViewPager mPager;
	private PageflipAdapter mAdapter;
	
	// State
	private int mCurrentPosition = 0;
	private boolean mLandscape = false;
	private boolean mLowMemory = false;
	
	// Callbacks and stats
	private PageflipListenerWrapper mWrapperListener = new PageflipListenerWrapper();
	private StatsCollect mCollector;
	private TextAnimLoader mLoader;
	private Handler mHandler;
	
	// Out of bounds detector stuff
	int mOutOfBoundsPX = 0;
	int mOutOfBoundsCount = 0;
	boolean mOutOfBoundsCalled = false;
	
	Listener<Catalog> mCatListener = new Listener<Catalog>() {
		
		public void onComplete(Catalog c, EtaError error) {
			if (!isAdded()) {
				return;
			}
			if ( c!=null && c.getPages()!=null && c.getHotspots()!=null ) {
				Looper main = Looper.getMainLooper();
				if (main == Looper.myLooper()) {
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
	};
	
	Runnable mOnCatalogComplete = new Runnable() {
		
		public void run() {
			setBranding();
			mCollector = new StatsCollectImpl(mCatalog);
			mAdapter = new PageflipAdapter(getChildFragmentManager(), PageflipFragment.this);
			mPager.setAdapter(mAdapter);
			mPager.setCurrentItem(mCurrentPosition);
			mProgress.setVisibility(View.GONE);
			mPager.setVisibility(View.VISIBLE);
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
	public void onCreate(Bundle savedInstanceState) {
		mHandler = new Handler();
		mLowMemory = PageflipUtils.hasLowMemory(getActivity());
		mLandscape = PageflipUtils.isLandscape(getActivity());
		if (getArguments()!=null) {
			setPage(getArguments().getInt(ARG_PAGE, 1));
			mCatalog = (Catalog)getArguments().getSerializable(ARG_CATALOG);
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mInflater = inflater;
		mContainer = container;
		setUpView(true);
		
		if (savedInstanceState != null) {
			setPage(savedInstanceState.getInt(STATE_PAGE, mCurrentPosition));
			mCatalog = (Catalog) savedInstanceState.getSerializable(STATE_CATALOG);
		}
		
		return mFrame;
		
	}
	
	private void setUpView(boolean removeParent) {
		
		if (mFrame == null) {
			mFrame = (FrameLayout) mInflater.inflate(R.layout.etasdk_layout_pageflip, mContainer, false);
		} else {
			// Remove self from parent view, to avoid attaching to two different vie
			ViewGroup parent = (ViewGroup) mFrame.getParent();
			if (parent!=null && removeParent) {
				parent.removeView(mFrame);
			}
		}
		mProgress = (TextView) mFrame.findViewById(R.id.etasdk_layout_pageflip_loader);
		mPager = (PageflipViewPager) mFrame.findViewById(R.id.etasdk_layout_pageflip_viewpager);
		mPager.setScrollDurationFactor(PAGER_SCROLL_FACTOR);
		mPager.setOnPageChangeListener(this);
		mProgress.setVisibility(View.VISIBLE);
		mPager.setVisibility(View.INVISIBLE);
		setBranding();
		startLoading();
	}
	
	private static int pageValidator(Catalog c, int page) {
		if (!PageflipUtils.isValidPage(c, page)) {
			EtaLog.i(TAG, "Page (" + page + ") not within catalog.pages range (1-" +c.getPageCount()+ "). Setting page to 1");
			return 1;
		}
		return page;
	}
	
	private void startLoading() {
		mLoader = new TextAnimLoader(mProgress);
		if (mCatalog!=null) {
			int branding = mCatalog.getBranding().getColor();
			int text = PageflipUtils.getTextColor(branding, getActivity());
			mProgress.setTextColor(text);
			mLoader.setText(mCatalog.getBranding().getName());
		}
		mLoader.run();
	}
	private void setBranding() {
		if (mCatalog!= null) {
			mFrame.setBackgroundColor(mCatalog.getBranding().getColor());
		}
	}
	
	private void ensureCatalog() {
		boolean needHotspots = mCatalog.getHotspots()==null;
		boolean needPages = mCatalog.getPages()==null;
		CatalogAutoFill caf = new CatalogAutoFill();
		caf.setLoadHotspots(needHotspots);
		caf.setLoadPages(needPages);
		caf.prepare(new AutoFillParams(), mCatalog, null, mCatListener);
		caf.execute(Eta.getInstance().getRequestQueue());
	}
	
	private void removeRunners() {
		mLoader.stop();
		mHandler.removeCallbacks(mOnCatalogComplete);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		boolean land = PageflipUtils.isLandscape(newConfig);
		if (land != mLandscape) {
			EtaLog.d(TAG, "onConfigurationChanged[orientation.landscape[" + mLandscape + "->" + land + "]");
			removeRunners();
			// Get the old page
			int[] pages = PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape);
			// switch to landscape mode
			mLandscape = land;
			// set new current position accordingly
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
		if (mCatalog!=null) {
			start();
		}
	}
	
	@Override
	public void onPause() {
		removeRunners();
		//TODO collect stats - discuss with Morten how to
		super.onPause();
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
		return mLowMemory;
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
