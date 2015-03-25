package com.eTilbudsavis.etasdk.pageflip.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.R;

public class LoadingTextView extends TextView {

	public static final String TAG = Constants.getTag(LoadingTextView.class);
	
	public static final int DELAY = 350;
	public static final int NUM_DOTS = 5;
	private Handler mHandler;
	private int mDots = 1;
	private boolean mCountUp = true;
	private String mLoadingText;
	
	public LoadingTextView(Context context) {
		super(context);
		init();
	}

	public LoadingTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LoadingTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void init() {
		mHandler = new Handler();
//		obs();
	}
	
	private void obs() {
		final ViewTreeObserver vto = getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
		  public void onGlobalLayout() {
		    if (getVisibility() == View.GONE) {
		    	stop();
		    } else {
		    	start();
		    }
		  }
		});
	}
	
	public void setLoadingText(String header) {
		mLoadingText = header;
	}
	
	public void error() {
		stop();
		String s = getResources().getString(R.string.etasdk_pageflip_load, mLoadingText);
		super.setText(s);
	}
	
	public void stop() {
		mHandler.removeCallbacks(mTextRunner);
		mDots = 1;
		mCountUp = true;
	}
	
	public void start() {
		stop();
		mTextRunner.run();
	}
	
	Runnable mTextRunner = new Runnable() {
		
		public void run() {
			
			mHandler.postDelayed(mTextRunner, DELAY);
			
			StringBuilder sb = new StringBuilder();
			sb.append(mLoadingText == null ? "" : mLoadingText).append("\n");
			for (int i = 0; i < mDots; i++) {
				sb.append(".");
			}
			setText(sb.toString());
			if (mCountUp) {
				mDots++;
				mCountUp = mDots!=NUM_DOTS;
			} else {
				mDots--;
				mCountUp = (mDots==1);
			}
		}
	};
	
}