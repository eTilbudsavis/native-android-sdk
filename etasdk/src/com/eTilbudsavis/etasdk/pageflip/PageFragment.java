package com.eTilbudsavis.etasdk.pageflip;

import java.util.Set;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.R;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Page;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.ImageLoader.Impl.FadeBitmapDisplayer;
import com.eTilbudsavis.etasdk.pageflip.PageflipPhotoView.OnZoomChangeListener;

public abstract class PageFragment extends Fragment {
	
	public static final String TAG = PageFragment.class.getSimpleName();
	
	protected static final int FADE_IN_DURATION = 150;
	protected static final float MAX_SCALE = 3.0f;
	
	protected static final String ARG_PAGE = "eta_sdk_pageflip_page_page";
	
	private int[] mPages;
	private PageflipPhotoView mPhotoView;
	private ProgressBar mProgress;
	private PageCallback mCallback;
	private boolean mHasZoomImage = false;
	
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
		mPhotoView = (PageflipPhotoView) v.findViewById(R.id.etasdk_pageflip_photoview);
		mPhotoView.setMaximumScale(MAX_SCALE);
		mPhotoView.setOnZoomListener(new OnZoomChangeListener() {
			
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
		});
		
		mProgress = (ProgressBar) v.findViewById(R.id.etasdk_pageflip_loader);
		
		mPhotoView.setVisibility(View.GONE);
		mProgress.setVisibility(View.VISIBLE);
		
		return v;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
//		outState.putIntArray(ARG_PAGE, mPages);
		super.onSaveInstanceState(outState);
	}
	
	protected void addRequest(ImageRequest ir) {
		ir.setMemoryCache(false);
		ir.setDebugger(PU.getSimpleDebugger(TAG));
		ImageLoader.getInstance().displayImage(ir);
	}
	
	protected void onClick(int page, float x, float y) {
		Set<Hotspot> list = mCallback.getCatalog().getHotspots().getHotspots(page, x, y, mCallback.isLandscape());
		for (Hotspot h : list) {
			Toast.makeText(getActivity(), h.getOffer().getHeading(), Toast.LENGTH_SHORT).show();
		}
	}
	
	protected PageflipPhotoView getPhotoView() {
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
	
	public class PageFadeBitmapDisplayer extends FadeBitmapDisplayer {
		
		public PageFadeBitmapDisplayer() {
			super(FADE_IN_DURATION);
		}
	}
	
}
