package com.eTilbudsavis.etasdk.pageflip;

import java.util.List;

import org.json.JSONObject;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.R;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Branding;
import com.eTilbudsavis.etasdk.model.Catalog;
import com.eTilbudsavis.etasdk.model.Hotspot;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.network.impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.pageflip.utils.PageflipUtils;
import com.eTilbudsavis.etasdk.pageflip.widget.LoadingTextView;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;
import com.eTilbudsavis.etasdk.request.impl.CatalogObjectRequest.CatalogAutoFill;
import com.eTilbudsavis.etasdk.utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.utils.Utils;

public class PageflipFragment extends Fragment implements PageCallback, OnPageChangeListener {
	
	public static final String TAG = Constants.getTag(PageflipFragment.class);
	
	private static final double PAGER_SCROLL_FACTOR = 0.5d;
	
	public static final String ARG_CATALOG = Constants.getArg("pageflipfragment.catalog");
	public static final String ARG_CATALOG_ID = Constants.getArg("pageflipfragment.catalog-id");
	public static final String ARG_PAGE = Constants.getArg("pageflipfragment.page");
//	public static final String ARG_CATALOG_VIEW = Eta.ARG_PREFIX + "pageflipfragment.catalog-view";
	public static final String ARG_VIEWSESSION = Constants.getArg("pageflipfragment.view-session");
	public static final String ARG_BRANDING = Constants.getArg("pageflipfragment.branding");
	
	// Need this
	private Catalog mCatalog;
	private String mCatalogId;
//	private boolean mHasCatalogView = false;
	private Branding mBranding;
	
	// Views
	private LayoutInflater mInflater;
	private ViewGroup mContainer;
	private FrameLayout mFrame;
	private LoadingTextView mLoader;
	private PageflipViewPager mPager;
	private PageAdapter mAdapter;
	
	// State
	private int mCurrentPosition = 0;
	private boolean mLandscape = false;
	private boolean mLowMemory = false;
	private boolean mPagesReady = false;
	private boolean mPageflipStarted = false;
	
	// Callbacks and stats
	private PageflipListenerWrapper mWrapperListener = new PageflipListenerWrapper();
	private String mViewSessionUuid;
	private Handler mHandler;
	
	// Out of bounds detector stuff
	int mOutOfBoundsPX = 0;
	int mOutOfBoundsCount = 0;
	boolean mOutOfBoundsCalled = false;

	/**
	 * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
	 * @param c The catalog to show
	 * @return A Fragment
	 */
	public static PageflipFragment newInstance(Catalog c) {
		return newInstance(c, 1);
	}

	/**
	 * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
	 * @param c The catalog to show
	 * @param page the page number to start at
	 * @return A Fragment
	 */
	public static PageflipFragment newInstance(Catalog c, int page) {
		Bundle b = new Bundle();
		b.putSerializable(ARG_CATALOG, c);
		b.putInt(ARG_PAGE, page);
		return newInstance(b);
	}
	
	/**
	 * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
	 * @param catalogId The id of the catalog to display
	 * @return A Fragment
	 */
	public static PageflipFragment newInstance(String catalogId) {
		return newInstance(catalogId, 1);
	}
	
	/**
	 * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
	 * @param catalogId The is of the catalog to show
	 * @param page the page number to start at
	 * @return A Fragment
	 */
	public static PageflipFragment newInstance(String catalogId, int page) {
		return newInstance(catalogId, page, null);
	}

	/**
	 * Creates a new instance of {@link PageflipFragment}, to replace or insert into a current layout.
	 * @param catalogId The is of the catalog to show
	 * @param page the page number to start at
	 * @return A Fragment
	 */
	public static PageflipFragment newInstance(String catalogId, int page, Branding initialBranding) {
		Bundle b = new Bundle();
		b.putString(ARG_CATALOG_ID, catalogId);
		b.putInt(ARG_PAGE, page);
		b.putParcelable(ARG_BRANDING, initialBranding);
		return newInstance(b);
	}
	
	public static PageflipFragment newInstance(Bundle args) {
		PageflipFragment f = new PageflipFragment();
		f.setArguments(args);
		if (!f.getArguments().containsKey(ARG_VIEWSESSION)) {
			f.getArguments().putString(ARG_VIEWSESSION, Utils.createUUID());
		}
		return f;
	}
	
	private static void throwNoCatalogException() {
		String err = "No catalog or catalog-id given as argument to PageflipFragment. See PageflipFragment.newInstance()";
		throw new IllegalArgumentException(err);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mHandler = new Handler();
		mLowMemory = PageflipUtils.hasLowMemory(getActivity());
		mLandscape = PageflipUtils.isLandscape(getActivity());
		setUp(savedInstanceState == null ? getArguments() : savedInstanceState);
		super.onCreate(savedInstanceState);
	}
	
	private void setUp(Bundle args) {
		
		setPage(args.getInt(ARG_PAGE, 1));
		if (args.containsKey(ARG_CATALOG)) {
			setCatalog((Catalog) args.getSerializable(ARG_CATALOG));
			mBranding = mCatalog.getBranding();
		} else if (args.containsKey(ARG_CATALOG_ID)) {
			setCatalogId(args.getString(ARG_CATALOG_ID));
			mBranding = args.getParcelable(ARG_BRANDING);
		} else {
			throwNoCatalogException();
		}
		
//		mHasCatalogView = b.getBoolean(ARG_CATALOG_VIEW, false);
		mViewSessionUuid = args.getString(ARG_VIEWSESSION);
		
		if (mViewSessionUuid == null) {
			mViewSessionUuid = Utils.createUUID();
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		mInflater = inflater;
		mContainer = container;
		setUpView(true);
		return mFrame;
		
	}
	
	/**
	 * Called to setup the view, on create and resume events.
	 * @param removeParent Whether to remove the View from the parent view (on e.g. configuration changes)
	 */
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
		
		mLoader = (LoadingTextView) mFrame.findViewById(R.id.etasdk_layout_pageflip_loader);
		mPager = (PageflipViewPager) mFrame.findViewById(R.id.etasdk_layout_pageflip_viewpager);
		mPager.setScrollDurationFactor(PAGER_SCROLL_FACTOR);
		mPager.setOnPageChangeListener(this);
		mPager.setPageflipListener(mWrapperListener);
		
		mLoader.setVisibility(View.VISIBLE);
		mPager.setVisibility(View.INVISIBLE);
		setBranding(mBranding);
		mLoader.start();
		
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
			pause();
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
	
	/**
	 * Get the {@link PageflipListener}.
	 * @return The listener, or <code>null</code>.
	 */
	public PageflipListener getListener() {
		return mWrapperListener.getListener();
	}
	
	/**
	 * Get the {@link PageflipFragment} wrapper listener, primarily used for debugging.
	 * @return The wrapper listener
	 */
	public PageflipListener getWrapperListener() {
		return mWrapperListener;
	}
	
	/**
	 * Set a listener to call on {@link PageflipFragment} events.
	 * @param l The listener
	 */
	public void setPageflipListener(PageflipListener l) {
		mWrapperListener.setListener(l);;
	}
	
	/**
	 * Get the pages currently being displayed in the {@link PageflipFragment}.
	 * @return An array of pages being displayed
	 */
	public int[] getPages() {
		return PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape);
	}
	
	/**
	 * Set the {@link PageflipFragment} to show the given page number in the catalog.
	 * Note that page number doesn't directly correlate to the position of the {@link PageflipViewPager}.
	 * @param page The page to turn to
	 */
	public void setPage(int page) {
		if (PageflipUtils.isValidPage(mCatalog, page)) {
			setPosition(PageflipUtils.pageToPosition(page, mLandscape));
		}
	}
	
	/**
	 * Get the current position of the {@link PageflipViewPager}.
	 * @return The current position
	 */
	public int getPosition() {
		return mCurrentPosition;
	}
	
	/**
	 * Set the position of the {@link PageflipViewPager}. 
	 * Note that this does not correlate directly to the catalog page number.
	 * @param position A position
	 */
	public void setPosition(int position) {
		mCurrentPosition = position;
		if (mPager != null) {
			mPager.setCurrentItem(mCurrentPosition);
		}
	}
	
	/**
	 * Go to the next page in the catalog
	 */
	public void nextPage() {
		mPager.setCurrentItem(mCurrentPosition+1, true);
	}
	
	/**
	 * Go to the previous page in the catalog
	 */
	public void previousPage() {
		mPager.setCurrentItem(mCurrentPosition-1, true);
	}
	
	private void setBranding(Branding b) {
		
		if (b==null) {
			return;
		}
		
		mBranding = b;
		mLoader.setLoadingText(mBranding.getName());
		int text = PageflipUtils.getTextColor(mBranding.getColor(), getActivity());
		mLoader.setTextColor(text);
		mFrame.setBackgroundColor(mBranding.getColor());
		mContainer.setBackgroundColor(mBranding.getColor());
		
	}
	
	/**
	 * Return the current branding, used by {@link PageflipFragment}.
	 * @return A {@link Branding}
	 */
	public Branding getBranding() {
		return mBranding;
	}
	
	/**
	 * Method for determining if the {@link PageflipFragment} is ready.
	 * It checks if the {@link PageflipViewPager} has an {@link PageAdapter} attached.
	 * @return true if the fragment if ready, else false.
	 */
	public boolean isReady() {
		return mAdapter != null;
	}

	private boolean isHotspotsReady() {
		return mCatalog != null && mCatalog.getHotspots() != null;
	}
	
	private boolean isPagesReady() {
		return mCatalog != null && mCatalog.getPages() != null && !mCatalog.getPages().isEmpty();
	}
	
	/**
	 * Method for determining if the caralog is ready;
	 * @return true, if the catalog is fully loaded, including pages and hotspots
	 */
	public boolean isCatalogReady() {
		return isPagesReady() && isHotspotsReady();
	}
	
	/**
	 * Set the {@link Catalog} that you want to display.
	 * This is unnecessary if you have created the fragment with one of the provided 
	 * {@link PageflipFragment} newInstance methods.
	 * @param c A catalog to display
	 */
	public void setCatalog(Catalog c) {
		if (c != null) {
			mCatalog = c;
			mCatalogId = mCatalog.getId();
		}
	}
	
	/**
	 * Set the id of the {@link Catalog#getId() catalog} that you want to display.
	 * This is unnecessary if you have created the fragment with one of the provided 
	 * {@link PageflipFragment} newInstance methods.
	 * @param catalogId A catalog id
	 */
	public void setCatalogId(String catalogId) {
		mCatalogId = catalogId;
	}
	
	/**
	 * Method for instantiating the {@link PageflipFragment}. 
	 * This will perform all needed actions in order to get the show started. 
	 */
	public void start() {
		
		synchronized (this) {
			if (mPageflipStarted) {
				return;
			}
			mPageflipStarted = true;
		}
		ensureCatalog();
	}

	private void ensureCatalog() {
		
		if (mCatalog!=null) {
			setBrandingAndFillCatalog();
			return;
		}
		
		Listener<JSONObject> l = new Listener<JSONObject>() {
			
			public void onComplete(JSONObject response, EtaError error) {
				
//				mHasCatalogView = response != null;
				if (!isAdded()) {
					// Ignore callback
					return;
				}
					
				if (response != null) {
					setCatalog(Catalog.fromJSON(response));
					setBrandingAndFillCatalog();
				} else {
					mLoader.error();
				}
				
			}
		};
		
		String url = Endpoint.catalogId(mCatalogId);
		JsonObjectRequest r = new JsonObjectRequest(url, l);
		r.setIgnoreCache(true);
		Eta.getInstance().add(r);
		EtaLog.d(TAG, "getting catalog");
		
	}
	
	private void setBrandingAndFillCatalog() {
		
		if (mCatalog != null) {
			setBranding(mCatalog.getBranding());
			runCatalogFiller();
		}
		
	}
	
	private void runCatalogFiller() {
		
		CatalogAutoFill caf = new CatalogAutoFill();
		caf.setLoadHotspots(!isHotspotsReady());
		caf.setLoadPages(!isPagesReady());
		caf.prepare(new AutoFillParams(), mCatalog, null, mCatListener);
		caf.execute(Eta.getInstance().getRequestQueue());
		
	}

	Listener<Catalog> mCatListener = new Listener<Catalog>() {
		
		public void onComplete(Catalog c, EtaError error) {

			if (!isAdded()) {
				return;
			}
			
			if ( c!=null && c.getPages()!=null && c.getHotspots()!=null ) {
				
				getActivity().runOnUiThread(mOnCatalogComplete);
				
			} else if (error != null ){
				
				EtaLog.d(TAG, error.toJSON().toString());
				// TODO improve error stuff 1 == network error
				mLoader.error();
				mWrapperListener.onError(error);
				
			}
			
			if (!isCatalogReady()) {
				// If a catalog object have been given, that have expired we won't be able to get any data
				mLoader.error();
				mWrapperListener.onError(error);
			}
			
		}
	};
	
	Runnable mOnCatalogComplete = new Runnable() {
		
		public void run() {
			
			mAdapter = new PageAdapter(getChildFragmentManager(), PageflipFragment.this);
			mPager.setAdapter(mAdapter);
			
			// force the first page change if needed
			boolean doPageChange = (mPager.getCurrentItem()!=mCurrentPosition);
			if (doPageChange) {
				mPager.setCurrentItem(mCurrentPosition);
			} else {
				mWrapperListener.onPageChange(PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape));
			}
			mLoader.setVisibility(View.GONE);
			mPager.setVisibility(View.VISIBLE);
			
			mWrapperListener.onReady();
			
		}
		
	};
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		int[] pages = PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape);
		outState.putInt(ARG_PAGE, pages[0]);
		outState.putSerializable(ARG_CATALOG, mCatalog);
		outState.putSerializable(ARG_CATALOG_ID, mCatalogId);
//		outState.putBoolean(ARG_CATALOG_VIEW, mHasCatalogView);
		outState.putString(ARG_VIEWSESSION, mViewSessionUuid);
		outState.putParcelable(ARG_BRANDING, mBranding);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		start();
	}
	
	@Override
	public void onPause() {
		pause();
		super.onPause();
	}
	
	private void pause() {
		removeRunners();
		mPagesReady = false;
		mPageflipStarted = false;
	}
	
	/**
	 * Method for letting the fragment know of backpressed events.
	 * Currently this does nothing.
	 * @return false
	 */
	public boolean onBackPressed() {
		return false;
	}
	
	private PageFragment getPage(int position) {
		return (PageFragment)mAdapter.instantiateItem(mContainer, position);
	}
	
	public void onReady(int position) {
		if (position==mCurrentPosition) {
			PageFragment old = getPage(position);
			old.onVisible();
			mPagesReady = true;
		}
	}
	
	public void onPageSelected(int position) {
		int oldPos = mCurrentPosition;
		mCurrentPosition = position;
		if (mPagesReady) {
			PageFragment old = getPage(oldPos);
			old.onInvisible();
			PageFragment current = getPage(mCurrentPosition);
			current.onVisible();
		}
		mWrapperListener.onPageChange(PageflipUtils.positionToPages(mCurrentPosition, mCatalog.getPageCount(), mLandscape));
	}
	
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		
	}
	
	public void onPageScrollStateChanged(int state) {
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
	
	public String getViewSession() {
		return mViewSessionUuid;
	}
	
	/**
	 * A wrapper class for the users {@link PageflipListener}. Used to do some debugging.
	 */
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
		
		public void onReady() {
			log("onReady");
			if (post()) mListener.onReady();
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
			log("onError: " + (error == null ? "null" : error.toJSON().toString()));
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
