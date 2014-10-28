package com.eTilbudsavis.etasdk.pageflip;

import java.util.Set;

import android.app.Activity;
import android.graphics.Bitmap;
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
import com.eTilbudsavis.etasdk.ImageLoader.Impl.DefaultDebugger;
import com.eTilbudsavis.etasdk.pageflip.PageflipPhotoView.OnZoomChangeListener;
import com.eTilbudsavis.etasdk.photoview.PhotoView;

public abstract class PageflipPage extends Fragment {
	
	public static final String TAG = PageflipPage.class.getSimpleName();
	
	protected static final int FADE_IN_DURATION = 150;
	protected static final float MAX_SCALE = 3.0f;
	
	protected static final String ARG_PAGE = "eta_sdk_pageflip_page_page";
	
	private int[] mPages;
	private PageflipPhotoView mPhotoView;
	private ProgressBar mProgress;
	protected boolean debug = true;
	private PageCallback mCallback;
	private boolean mHasZoomImage = false;
	
	OnZoomChangeListener mZoomChangeListener = new OnZoomChangeListener() {
		
		public void onZoomChange(boolean isZoomed) {
			if (isZoomed) {
				if (!mHasZoomImage) {
					mHasZoomImage = true;
					loadZoom();
				}
				mCallback.zoomStart();
			} else {
				mCallback.zoomStop();
			}
		}
	};
	
	public static PageflipPage newInstance(Catalog c, int[] pages, boolean landscape) {
		Bundle b = new Bundle();
		b.putIntArray(ARG_PAGE, pages);
		PageflipPage f = pages.length == 1 ? new PageflipSinglePage() : new PageflipDoublePage();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getArguments()!=null) {
			mPages = getArguments().getIntArray(ARG_PAGE);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
//			mCatalog = (Catalog) savedInstanceState.getSerializable(ARG_CATALOG);
			mPages = savedInstanceState.getIntArray(ARG_PAGE);
//			mLandscape = savedInstanceState.getBoolean(ARG_LANDSCAPE);
		}
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View v = inflater.inflate(R.layout.etasdk_layout_page, container, false);
		mPhotoView = (PageflipPhotoView) v.findViewById(R.id.etasdk_pageflip_photoview);
		mPhotoView.setMaximumScale(MAX_SCALE);
		mPhotoView.setOnZoomListener(mZoomChangeListener);
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
	
	protected void addRequest(ImageRequest ir) {
		ir.setMemoryCache(false);
		ir.setDebugger(new DefaultDebugger());
		ImageLoader.getInstance().displayImage(ir);
	}
	
	protected void click(int page, float x, float y) {
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
		
		Bitmap b = getBitmap();
		if ( (b == null || b.isRecycled() ) && mCallback.isPositionSet() ) {
			loadView();
		}
		
		super.onResume();
	}
	
	@Override
	public void onPause() {
		
		if (mPhotoView.getScale() != mPhotoView.getMinimumScale()) {
			mPhotoView.setScale(mPhotoView.getMinimumScale());
		}
		Bitmap b = getBitmap();
		if (b != null) {
			b.recycle();
		}
		super.onPause();
	}
	
	private Bitmap getBitmap() {
		BitmapDrawable d = (BitmapDrawable)mPhotoView.getDrawable();
		if (d != null) {
			return d.getBitmap();
		}
		return null;
	}

	public abstract void loadView();

	public abstract void loadZoom();
	
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
