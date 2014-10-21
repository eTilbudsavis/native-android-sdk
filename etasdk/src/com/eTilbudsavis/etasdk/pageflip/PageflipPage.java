package com.eTilbudsavis.etasdk.pageflip;

import java.util.Set;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.R;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Page;
import com.eTilbudsavis.etasdk.ImageLoader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.ImageLoader.LoadSource;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.photoview.PhotoView;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnMatrixChangedListener;

public abstract class PageflipPage extends Fragment {
	
	public static final String TAG = PageflipPage.class.getSimpleName();
	
	protected static final int FADE_IN_DURATION = 150;
	protected static final float MAX_SCALE = 3.0f;
	
	protected static final String ARG_CATALOG = "eta_sdk_pageflip_page_catalog";
	protected static final String ARG_PAGE = "eta_sdk_pageflip_page_page";
	protected static final String ARG_LANDSCAPE = "eta_sdk_pageflip_page_landscape";
	
//	private Catalog mCatalog;
	private int[] mPages;
	private PhotoView mPhotoView;
	private ProgressBar mProgress;
//	private boolean mLandscape = false;
	protected boolean debug = true;
	private boolean mIsZoomed = false;
	private PageCallback mCallback;

	OnMatrixChangedListener mMatrixChangedListener = new OnMatrixChangedListener() {
		
		public void onMatrixChanged(RectF rect) {
			boolean isMinScale = PageflipUtils.almost(mPhotoView.getScale(), mPhotoView.getMinimumScale(), 0.001f);
			mPhotoView.setAllowParentInterceptOnEdge(isMinScale);
//			EtaLog.d(TAG, "page: " + PageflipUtils.join(",", getPages()) + ", isMinScale:" + isMinScale + ", isZoomed:" + mIsZoomed);
			if (isMinScale && mIsZoomed) {
				EtaLog.d(TAG, "zoom.stop");
				mIsZoomed = false;
				mCallback.zoomStop();
			} else if (!mIsZoomed && !isMinScale) {
				EtaLog.d(TAG, "zoom.start");
				mIsZoomed = true;
				mCallback.zoomStart();
			}
		}
		
	};
	
	public static PageflipPage newInstance(Catalog c, int[] pages, boolean landscape) {
		Bundle b = new Bundle();
		b.putSerializable(ARG_CATALOG, c);
		b.putIntArray(ARG_PAGE, pages);
		b.putBoolean(ARG_LANDSCAPE, landscape);
		PageflipPage f = pages.length == 1 ? new PageflipSinglePage() : new PageflipDoublePage();
		EtaLog.d(TAG, "l:"+pages.length+", f:"+f.getClass().getSimpleName());
		f.setArguments(b);
		return f;
	}
	
	public abstract void loadPages();
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getArguments()!=null) {
//			mCatalog = (Catalog)getArguments().getSerializable(ARG_CATALOG);
			mPages = getArguments().getIntArray(ARG_PAGE);
//			mLandscape = getArguments().getBoolean(ARG_LANDSCAPE, false);
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		if (savedInstanceState != null) {
//			mCatalog = (Catalog) savedInstanceState.getSerializable(ARG_CATALOG);
			mPages = savedInstanceState.getIntArray(ARG_PAGE);
//			mLandscape = savedInstanceState.getBoolean(ARG_LANDSCAPE);
		}
		
		View v = inflater.inflate(R.layout.etasdk_layout_page, container, false);
		mPhotoView = (PhotoView) v.findViewById(R.id.etasdk_pageflip_photoview);
		mPhotoView.setMaximumScale(MAX_SCALE);
		mPhotoView.setOnMatrixChangeListener(mMatrixChangedListener);
		mProgress = (ProgressBar) v.findViewById(R.id.etasdk_pageflip_loader);
		
		mPhotoView.setVisibility(View.GONE);
		mProgress.setVisibility(View.VISIBLE);
		
		return v;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
//		outState.putSerializable(ARG_CATALOG, mCatalog);
		outState.putIntArray(ARG_PAGE, mPages);
//		outState.putBoolean(ARG_LANDSCAPE, mLandscape);
		super.onSaveInstanceState(outState);
	}
	
	private void debug() {
		
		EtaLog.d(TAG, "pages[" + PageflipUtils.join(",", getPages()) + "]");
//		List<Hotspot> spots = getCatalog().getHotspots().get(getPage());
//		if (spots == null) {
//			return;
//		}
//		for(Hotspot h : spots) {
//			if (h.isAreaSignificant(mLandscape)) {
//				EtaLog.d(TAG, h.getOffer().getHeading() + ", " + h.getArea());
//			}
//		}
	}
	
	protected void addRequest(ImageRequest ir) {
		ir.setMemoryCache(false);
		ImageLoader.getInstance().displayImage(ir);
	}
	
	protected void click(int page, float x, float y) {
		
//		EtaLog.d(TAG, String.format("click(p:%s x:%.2f , y:%.2f)", page, x, y));
		
		Set<Hotspot> list = getCatalog().getHotspots().getHotspots(page, x, y, isLandscape());
		for (Hotspot h : list) {
			Toast.makeText(getActivity(), h.getOffer().getHeading(), Toast.LENGTH_SHORT).show();
		}
		
	}
	
	protected PhotoView getPhotoView() {
		return mPhotoView;
	}
	
	public void setPageCallback(PageCallback callback) {
		mCallback = callback;
	}
	
	public Catalog getCatalog() {
		return mCallback.getCatalog();
	}

	public int[] getPages() {
		return mPages;
	}
	
	public boolean isLandscape() {
		return mCallback.isLandscape();
	}
	
	protected Page getPage(int page) {
		// Offset the given page number by one. Real-world to array number 
		return getCatalog().getPages().get(page-1);
	}
	
	@Override
	public void onResume() {
//		EtaLog.d(TAG, "onResume[pages:" + PageflipUtils.join(",", getPages()) + "]");
		loadPages();
		super.onResume();
	}
	
	@Override
	public void onPause() {
//		EtaLog.d(TAG, "onPause[pages:" + PageflipUtils.join(",", getPages()) + "]");
		// Reset the scale of PhotoView (implicitly stopping any zoom collect view)
		if (mPhotoView.getScale() != mPhotoView.getMinimumScale()) {
			mPhotoView.setScale(mPhotoView.getMinimumScale());
		}
		BitmapDrawable d = (BitmapDrawable)mPhotoView.getDrawable();
		if (d != null) {
			Bitmap b = d.getBitmap();
			if (b != null) {
				b.recycle();
			}
		}
		super.onPause();
	}
	
	public class PageflipBitmapDisplayer implements BitmapDisplayer {
		
		private boolean mFadeFromMemory = true;
		private boolean mFadeFromFile = false;
		private boolean mFadeFromWeb = false;
		
		public void display(ImageRequest ir) {
			
			if(ir.getBitmap() != null) {
				mPhotoView.setImageBitmap(ir.getBitmap());
			} else if (ir.getPlaceholderError() != 0) {
				mPhotoView.setImageResource(ir.getPlaceholderError());
			}
			
			mPhotoView.setVisibility(View.VISIBLE);
			mProgress.setVisibility(View.INVISIBLE);
			
			if ( (ir.getLoadSource() == LoadSource.WEB && mFadeFromWeb) ||
					(ir.getLoadSource() == LoadSource.FILE && mFadeFromFile) ||
					(ir.getLoadSource() == LoadSource.MEMORY && mFadeFromMemory) ) {
				
				AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
				fadeImage.setDuration(FADE_IN_DURATION);
				fadeImage.setInterpolator(new DecelerateInterpolator());
				mPhotoView.startAnimation(fadeImage);
				
			}
		}
		
	}
	
}
