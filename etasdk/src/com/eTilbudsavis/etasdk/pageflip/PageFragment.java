package com.eTilbudsavis.etasdk.pageflip;

import java.util.List;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.R;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Page;
import com.eTilbudsavis.etasdk.ImageLoader.BitmapDisplayer;
import com.eTilbudsavis.etasdk.ImageLoader.BitmapProcessor;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.ImageLoader.LoadSource;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.pageflip.ZoomPhotoView.OnZoomChangeListener;

public abstract class PageFragment extends Fragment {
	
	public static final String TAG = PageFragment.class.getSimpleName();
	
	protected static final int FADE_IN_DURATION = 150;
	protected static final float MAX_SCALE = 3.0f;
	
	protected static final String ARG_PAGE = "eta_sdk_pageflip_page_page";
	
	private int[] mPages;
	private ZoomPhotoView mPhotoView;
	private ProgressBar mProgress;
	private TextView mPageNum;
	private PageCallback mCallback;
	private boolean mHasZoomImage = false;
	private boolean mDebugRects = false;
	
	public static PageFragment newInstance(int[] pages) {
		Bundle b = new Bundle();
		b.putIntArray(ARG_PAGE, pages);
		PageFragment f = pages.length == 1 ? new SinglePageFragment() : new DoublePageFragment();
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (getArguments()!=null) {
			mPages = getArguments().getIntArray(ARG_PAGE);
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View v = inflater.inflate(R.layout.etasdk_layout_page, container, false);
		mPhotoView = (ZoomPhotoView) v.findViewById(R.id.etasdk_pageflip_photoview);
		mPageNum = (TextView) v.findViewById(R.id.etasdk_pageflip_pagenum);
		mProgress = (ProgressBar) v.findViewById(R.id.etasdk_pageflip_loader);
		
		mPhotoView.setMaximumScale(MAX_SCALE);
		mPhotoView.setOnZoomListener(new OnZoomChangeListener() {
			
			public void onZoomChange(boolean isZoomed) {
				if (isZoomed && !mHasZoomImage) {
					mHasZoomImage = true;
					loadZoom();
				}
				mCallback.onZoom(mPhotoView, isZoomed);
			}
		});
		mPageNum.setText(PageflipUtils.join("-", mPages));
		int brandingColor = getCallback().getCatalog().getBranding().getColor();
		int complimentColor = PageflipUtils.getTextColor(brandingColor, getActivity());
		mPageNum.setTextColor(complimentColor);
		toggleContentVisibility(true);
		return v;
	}
	
	private void toggleContentVisibility(boolean isLoading) {
		int content = isLoading ? View.GONE : View.VISIBLE;
		int loader = isLoading ? View.VISIBLE : View.INVISIBLE;
		mPhotoView.setVisibility(content);
		mProgress.setVisibility(loader);
		mPageNum.setVisibility(loader);
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
//		outState.putIntArray(ARG_PAGE, mPages);
		super.onSaveInstanceState(outState);
	}
	
	protected void addRequest(ImageRequest ir) {
		ir.setMemoryCache(false);
		ImageLoader.getInstance().displayImage(ir);
	}

	protected void onSingleClick(int page, float x, float y) {
		List<Hotspot> list = mCallback.getCatalog().getHotspots().getHotspots(page, x, y, mCallback.isLandscape());
		getCallback().getWrapperListener().onSingleClick(mPhotoView, page, x, y, list);
	}
	
	protected void onDoubleClick(int page, float x, float y) {
		List<Hotspot> list = mCallback.getCatalog().getHotspots().getHotspots(page, x, y, mCallback.isLandscape());
		getCallback().getWrapperListener().onDoubleClick(mPhotoView, page, x, y, list);
	}
	
	protected void onLongClick(int page, float x, float y) {
		List<Hotspot> list = mCallback.getCatalog().getHotspots().getHotspots(page, x, y, mCallback.isLandscape());
		getCallback().getWrapperListener().onLongClick(mPhotoView, page, x, y, list);
	}
	
	protected ZoomPhotoView getPhotoView() {
		return mPhotoView;
	}
	
	public void setPageCallback(PageCallback callback) {
		mCallback = callback;
	}
	
	public PageCallback getCallback() {
		return mCallback;
	}

	public int[] getPages() {
		return mPages;
	}
	
	protected Page getPage(int page) {
		// Offset the given page number by one. Real-world to array number
		return mCallback.getCatalog().getPages().get(page-1);
	}

	protected Page getFirst() {
		return getPage(getFirstNum());
	}

	protected Page getSecond() {
		return getPage(getSecondNum());
	}

	protected int getFirstNum() {
		return mPages[0];
	}

	protected int getSecondNum() {
		return mPages[1];
	}
	public abstract void loadView();

	public abstract void loadZoom();
	
	@Override
	public void onResume() {
		Bitmap b = mPhotoView.getBitmap();
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
		Bitmap b = mPhotoView.getBitmap();
		if (b != null) {
			b.recycle();
		}
		super.onPause();
	}
	
	public class PageBitmapProcessor implements BitmapProcessor {
		
		int page = 0;
		
		public PageBitmapProcessor(int page) {
			this.page = page;
		}
		
		public Bitmap process(Bitmap b) {
			
			if (mDebugRects) {
				try {
					return PageflipUtils.drawDebugRects(getCallback().getCatalog(), page, getCallback().isLandscape(), b);
				} catch (Exception e) {
					EtaLog.d(TAG, e.getMessage(), e);
				}
			}
			return b;
		}
		
	}
	
	public class PageFadeBitmapDisplayer implements BitmapDisplayer {

		private boolean mFadeFromMemory = true;
		private boolean mFadeFromFile = false;
		private boolean mFadeFromWeb = false;

		public void display(ImageRequest ir) {
			
			if(ir.getBitmap() != null) {
				mPhotoView.setImageBitmap(ir.getBitmap());
			} else if (ir.getPlaceholderError() != 0) {
				mPhotoView.setImageResource(ir.getPlaceholderError());
			}
			
			toggleContentVisibility(false);
			
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
