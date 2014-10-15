package com.eTilbudsavis.etasdk.pageflip;

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

import com.eTilbudsavis.etasdk.R;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
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
	
	protected static final String CATALOG = "eta_sdk_pageflip_page_catalog";
	protected static final String PAGE = "eta_sdk_pageflip_page_page";
	
	private Catalog mCatalog;
	private int mPage = -1;
	private PhotoView mPhotoView;
	private ProgressBar mProgress;
	
	public abstract void loadPages();
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getArguments()!=null) {
			mCatalog = (Catalog)getArguments().getSerializable(CATALOG);
			mPage = getArguments().getInt(PAGE);
		}
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState != null) {
			mCatalog = (Catalog) savedInstanceState.getSerializable(CATALOG);
			mPage = savedInstanceState.getInt(PAGE);
		}
		
		View v = inflater.inflate(R.layout.etasdk_layout_page, container, false);
		mPhotoView = (PhotoView) v.findViewById(R.id.etasdk_pageflip_photoview);
		mPhotoView.setMaximumScale(MAX_SCALE);
		mPhotoView.setOnMatrixChangeListener(new OnMatrixChangedListener() {
			
			public void onMatrixChanged(RectF rect) {
				boolean intercept = almost(mPhotoView.getScale(), mPhotoView.getMinimumScale());
				mPhotoView.setAllowParentInterceptOnEdge(intercept);
			}
			
		});
		mProgress = (ProgressBar) v.findViewById(R.id.etasdk_pageflip_loader);
		
		mPhotoView.setVisibility(View.INVISIBLE);
		mProgress.setVisibility(View.VISIBLE);
		
		return v;
	}
	
	private boolean almost(float first, float second) {
		return Math.abs(first-second)<0.1;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(CATALOG, mCatalog);
		outState.putInt(PAGE, mPage);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onResume() {
		loadPages();
		super.onResume();
	}
	
	protected void addRequest(ImageRequest ir) {
		ir.setMemoryCache(false);
		ImageLoader.getInstance().displayImage(ir);
	}
	
	protected PhotoView getPhotoView() {
		return mPhotoView;
	}
	
	protected Page getPageLeft() {
		return mCatalog.getPages().get(mPage);
	}
	
	protected Page getPageRight() {
		try {
			return mCatalog.getPages().get(mPage+1);
		} catch (IndexOutOfBoundsException e) {
			EtaLog.i(TAG, "No more pages");
			return null;
		}
	}
	
	@Override
	public void onPause() {
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
