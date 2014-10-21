package com.eTilbudsavis.etasdk.pageflip;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.FrameLayout;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request.Method;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.DefaultDebugger;
import com.eTilbudsavis.etasdk.Network.Impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;
import com.eTilbudsavis.etasdk.request.impl.CatalogObjectRequest.CatalogAutoFill;

public class PageflipFragment extends Fragment implements OnPageChangeListener, PageCallback {
	
	public static final String TAG = PageflipFragment.class.getSimpleName();
	
	private static final String ARG_CATALOG = "eta_sdk_pageflip_catalog";
	private static final String ARG_PAGE = "eta_sdk_pageflip_page";
	
	private static final String STATE_CATALOG = "eta_sdk_pageflip_state_catalog";
	private static final String STATE_PAGE = "eta_sdk_pageflip_state_page";
	
	private PageflipViewPager mPager;
	private PageflipAdapter mAdapter;
	private Catalog mCatalog;
	private int mCurrentPosition = 0;
	private boolean mLandscape = false;
	private FrameLayout mFrame;
	private Handler mHandler;
	private PageflipListener mListener;
	
	private long mCollectViewSession = System.currentTimeMillis();
	private long mCollectViewStart = 0;
	private long mCollectZoomStart = 0;
	private long mCollectZoomAccumulated = 0;
	
	Listener<Catalog> mFillListener = new Listener<Catalog>() {
		
		public void onComplete(Catalog c, EtaError error) {
			if (c != null) {
				mHandler.post(mAdapterReset);
			} else {
				EtaLog.e(TAG, error.getMessage(), error);
			}
		}
	};

	Runnable mAdapterReset = new Runnable() {
		
		public void run() {
			mAdapter = new PageflipAdapter(getChildFragmentManager(), PageflipFragment.this);
			mPager.setAdapter(mAdapter);
			mPager.setCurrentItem(mCurrentPosition);
		}
	};
	
	public static PageflipFragment newInstance(Catalog c) {
		return newInstance(c, 1);
	}
	
	public static PageflipFragment newInstance(Catalog c, int page) {
		if (c==null) {
			throw new IllegalArgumentException("Catalog cannot be null");
		}
		if (!PageflipUtils.isValidPage(c, page)) {
			EtaLog.i(TAG, "Page (" + page + ") not within catalog.pages range (1-" +c.getPageCount()+ "). Setting page to 0");
			page = 1;
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
		
		if (getArguments() == null || !getArguments().containsKey(ARG_CATALOG)) {
//			throw new IllegalArgumentException("No catalog provided");
			// TODO: Don't throw exception, need to figure out XML solution
		}

		mCollectViewStart = System.currentTimeMillis();
		mCollectZoomStart = System.currentTimeMillis();
		
		mHandler = new Handler();
		mLandscape = PageflipUtils.isLandscape(getActivity());
		mCatalog = (Catalog)getArguments().getSerializable(ARG_CATALOG);
		setPage(getArguments().getInt(ARG_PAGE, 0));
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		if (savedInstanceState != null) {
			setPage(savedInstanceState.getInt(STATE_PAGE, mCurrentPosition));
			mCatalog = (Catalog) savedInstanceState.getSerializable(STATE_CATALOG);
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
			int[] pages = PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape);
			// switch to landscape mode
			mLandscape = land;
			// set new current position accordingly
			setPage(pages[0]);
			setUpView();
			ensureCatalog();
			
		}
	}
	
	public PageflipListener getListener() {
		return mListener;
	}
	
	public void setPageflipListener(PageflipListener l) {
		mListener = l;
	}
	
	public int[] getPages() {
		return PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape);
	}

	public void setPage(int page) {
		if (PageflipUtils.isValidPage(mCatalog, page)) {
			setPosition(PageflipUtils.pageToPosition(page, mLandscape));
		} else {
			EtaLog.i(TAG, "Page (" + page + ") not within catalog.pages range (0-" + (mCatalog.getPageCount()-1) + "). Setting page to 0");

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
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putIntArray(STATE_PAGE, PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape));
		outState.putSerializable(STATE_CATALOG, mCatalog);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
//		PageflipUtils.test();
		ensureCatalog();
	}
	
	@Override
	public void onPause() {
		//TODO collect stats
		super.onPause();
	}
	
	public void onPageSelected(int position) {
		// call collecct before changing current position
		collectView();
		mCurrentPosition = position;
		if (mListener != null) {
			mListener.onPageChange(PageflipUtils.positionToPages(position, mCatalog.getPageCount(), mLandscape));
		}
		EtaLog.d(TAG, "onPageSelected:"+position);
	}
	
	private void collectView() {
		long now = System.currentTimeMillis();
		long duration = (now - mCollectViewStart) - mCollectZoomAccumulated;
		mCollectViewStart = now;
		mCollectZoomAccumulated = 0;
		collect(true, duration);
	}
	
	public void zoomStart() {
		mCollectZoomStart = System.currentTimeMillis();
		if (mListener!=null) {
			mListener.onZoom(getPages(), true);
		}
	}
	
	public void zoomStop() {
		long duration = System.currentTimeMillis() - mCollectZoomStart;
		mCollectZoomAccumulated += duration;
		collect(false, duration);
		if (mListener!=null) {
			mListener.onZoom(getPages(), false);
		}
	}
	
	private void collect(boolean isView, long duration) {

		JSONObject body = getCollectData(isView, duration, mLandscape, getPages());
		String url = Api.Endpoint.catalogCollect(mCatalog.getId());
		
		EtaLog.d(TAG, url + ", data: " + body.toString());
		
		JsonObjectRequest r = new JsonObjectRequest(Method.POST, url, body, new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {
				if (response!= null) {
//					EtaLog.d(TAG, response.toString());
				} else {
					EtaLog.d(TAG, error.toJSON().toString());
				}
			}
		});
		Eta.getInstance().add(r);
		
	}
	
	private JSONObject getCollectData(boolean isView, long ms, boolean isLandscape, int[] pages) {
		JSONObject o = new JSONObject();
		try {
			o.put("type", isView ? "view" : "zoom");
			o.put("ms", ms);
			o.put("orientation", isLandscape ? "landscape" : "portrait");
			o.put("pages", PageflipUtils.join(",", pages));
			o.put("view_session", mCollectViewSession);
		} catch (JSONException e) {
			EtaLog.d(TAG, e.getMessage(), e);
		}
		return o;
	}
	
	int px = 0;
	int count = 0;
	boolean boundsCalled = false;
	public void onPageScrollStateChanged(int state) {
		if (state == ViewPager.SCROLL_STATE_IDLE) {
			px = 0;
			count = 0;
			boundsCalled = false;
		}
		if (mListener != null) {
			mListener.onDragStateChanged(state);
		}
//		EtaLog.d(TAG, "onPageScrollStateChanged:"+state);
		
	}
	
	
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		boolean isLeft = mCurrentPosition==0;
		if ( !boundsCalled && (isLeft || mCurrentPosition==mAdapter.getCount()-1 ) && count > 3 && px < 2) {
			if (isLeft) {
				EtaLog.d(TAG, "outOfBounds.left");
			} else {
				EtaLog.d(TAG, "outOfBounds.right");
			}
			if (mListener != null) {
				mListener.onOutOfBounds();
			}
			boundsCalled = true;
		}
		count++;
		px += positionOffsetPixels;
//		String text = "onPageScrolled: pos:%s, posOfffset:%.2f, posOffsetPx:%s";
//		EtaLog.d(TAG, String.format(text, position, positionOffset, positionOffsetPixels));
	}

	public Catalog getCatalog() {
		return mCatalog;
	}

	public boolean isLandscape() {
		return mLandscape;
	}
	
}
