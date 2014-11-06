package com.eTilbudsavis.etasdk.pageflip;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class TextAnimLoader implements Runnable {
	
	public static final String TAG = TextAnimLoader.class.getSimpleName();
	
	public static final int DELAY = 350;
	public static final int NUM_DOTS = 5;
	private Handler mHandler;
	private String mText;
	private TextView mTextView;
	private int mDots = 1;
	private boolean mCountUp = true;
	
	public TextAnimLoader(TextView tv) {
		mTextView = tv;
		mHandler = new Handler();
		setText(null);
	}
	
	public void setText(String text) {
		mText = (text==null ? " " : text);
	}
	
	public void stop() {
		mHandler.removeCallbacks(this);
		mDots = 1;
		mCountUp = true;
	}
	
	public void error() {
		stop();
		mTextView.setText("Couldn't load\npage " + mText);
	}
	
	public void run() {
		
		if (mTextView.getVisibility() != View.VISIBLE) {
			return;
		}
		
		mHandler.postDelayed(this, DELAY);
		
		StringBuilder sb = new StringBuilder();
		sb.append(mText).append("\n");
		for (int i = 0; i < mDots; i++) {
			sb.append(".");
		}
		mTextView.setText(sb.toString());
		if (mCountUp) {
			mDots++;
			mCountUp = mDots!=NUM_DOTS;
		} else {
			mDots--;
			mCountUp = (mDots==1);
		}
	}
}