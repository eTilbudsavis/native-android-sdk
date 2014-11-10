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

	protected static final String ARG_PAGE = "com.eTilbudsavis.etasdk.pageflip.pageFragment.page";
	protected static final String ARG_POSITION = "com.eTilbudsavis.etasdk.pageflip.pageFragment.position";
	
	
	private int[] mPages;
	private ZoomPhotoView mPhotoView;
	private TextView mPageNum;
	private PageCallback mCallback;
	private boolean mHasZoomImage = false;
	private boolean mDebugRects = false;
	private TextAnimLoader mTextLoader;
	private PageStat mStats;
	boolean mPageVisible = false;
	
	private void updateBranding() {
		if (getCallback().getCatalog()!=null) {
			int brandingColor = getCallback().getCatalog().getBranding().getColor();
			int complimentColor = PageflipUtils.getTextColor(brandingColor, getActivity());
			mPageNum.setTextColor(complimentColor);
		}
	}
	
	private void runLoader() {
		
		updateBranding();
		
		if (mTextLoader==null) {
			mTextLoader = new TextAnimLoader(mPageNum);
		}
		mTextLoader.stop();
		mTextLoader.setText(PageflipUtils.join("-", mPages));
		mTextLoader.run();
	}
	
	public static PageFragment newInstance(int position, int[] pages) {
		Bundle b = new Bundle();
		b.putIntArray(ARG_PAGE, pages);
		b.putInt(ARG_POSITION, position);
		PageFragment f = pages.length == 1 ? new SinglePageFragment() : new DoublePageFragment();
		f.setArguments(b);
		return f;
	}
	
	private int mPosition = -1;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (getArguments()!=null) {
			mPages = getArguments().getIntArray(ARG_PAGE);
			mPosition = getArguments().getInt(ARG_POSITION, 0);
		}
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View v = inflater.inflate(R.layout.etasdk_layout_page, container, false);
		mPhotoView = (ZoomPhotoView) v.findViewById(R.id.etasdk_layout_page_photoview);
		mPageNum = (TextView) v.findViewById(R.id.etasdk_layout_page_pagenum);
		
		mPhotoView.setMaximumScale(MAX_SCALE);
		mPhotoView.setOnZoomListener(new OnZoomChangeListener() {
			
			public void onZoomChange(boolean isZoomed) {
				if (isZoomed && !mHasZoomImage) {
					mHasZoomImage = true;
					loadZoom();
				}
				
				if (isZoomed) {
					getStat().startZoom();
				} else {
					getStat().collectZoom();
				}
				
				getCallback().getWrapperListener().onZoom(mPhotoView, mPages, isZoomed);
			}
		});
		toggleContentVisibility(true);
		return v;
	}
	
	private void toggleContentVisibility(boolean isLoading) {
		int content = isLoading ? View.GONE : View.VISIBLE;
		int loader = isLoading ? View.VISIBLE : View.INVISIBLE;
		mPhotoView.setVisibility(content);
		mPageNum.setVisibility(loader);
		if (isLoading) {
			runLoader();
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
//		outState.putIntArray(ARG_PAGE, mPages);
		super.onSaveInstanceState(outState);
	}
	
	private PageStat getStat() {
		if (mStats==null) {
			mStats = new PageStat(mCallback.getCatalog().getId(), mCallback.getViewSession(), mPages, land());
		}
		return mStats;
	}
	
	protected void addRequest(ImageRequest ir) {
		ir.setMemoryCache(false);
		ir.setFileName(new PageflipFileNameGenerator());
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
	
	protected boolean lowMem() {
		return mCallback.isLowMemory();
	}
	
	protected boolean land() {
		return mCallback.isLandscape();
	}
	
	protected int getSecondNum() {
		return mPages[1];
	}
	public abstract void loadView();

	public abstract void loadZoom();
	
	private void loadImage() {
		Bitmap b = mPhotoView.getBitmap();
		if ( (b == null || b.isRecycled() ) && mCallback.isPositionSet() ) {
			loadView();
		}
	}
	
	private void unLoadImage() {
		Bitmap b = mPhotoView.getBitmap();
		if (b != null && !b.isRecycled()) {
			if (mPhotoView.getScale() != mPhotoView.getMinimumScale()) {
				mPhotoView.setScale(mPhotoView.getMinimumScale());
			}
			b.recycle();
		}
	}
	
	@Override
	public void onResume() {
		updateBranding();
		getCallback().onReady(mPosition);
		loadImage();
		super.onResume();
	}
	
	public boolean isPageVisible() {
		return mPageVisible;
	}
	
	public void onVisible() {
		loadImage();
		if (!mPageVisible) {
			if (mPhotoView.getBitmap()!=null) {
				getStat().startView();
			}
			// TODO do performance stuff, low memory devices can start loading here instead of onResume
		}
		mPageVisible = true;
	}
	
	public void onInvisible() {
		getStat().collectView();
		mPageVisible = false;
	}
	
	@Override
	public void onPause() {
		mTextLoader.stop();
		unLoadImage();
		onInvisible();
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
				if (isPageVisible()) {
					getStat().startView();
				}
				mPhotoView.setImageBitmap(ir.getBitmap());
				toggleContentVisibility(false);
			} else {
				mTextLoader.error();
			}
			
//			} else if (ir.getPlaceholderError() != 0) {
//				mPhotoView.setImageResource(ir.getPlaceholderError());
//			}
			
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
