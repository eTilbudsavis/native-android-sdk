package com.eTilbudsavis.etasdk.log;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.View;

import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.pageflip.PageflipUtils;

public class RandomLog {
	
	private RandomLog() {
	}
	
	public static void logInvalidSignature(String tag, Request<?> request, EtaError e) {

    	if (e.getCode() == 1104) {
    		
    		StringBuilder sb = new StringBuilder();
    		sb.append(request.getNetworkLog().toString());
    		sb.append("/n");
    		sb.append(request.getLog().getString("Invalid Signature"));
    		sb.append("/n");
    		sb.append(e.toJSON().toString());
    		
    		EtaLog.d(tag, sb.toString());
    	}
    	
	}
	
	public static void printViewDimen(String tag, String viewName, View v) {
		EtaLog.d(tag, viewName + ", getWidth: " + v.getWidth() + ", getHeight: " + v.getHeight());
		EtaLog.d(tag, viewName + ", getMeasuredWidth: " + v.getMeasuredWidth() + ", getMeasuredHeight: " + v.getMeasuredHeight());
	}
	
	public static void printBitmapInfo(String tag, Bitmap b) {
		printBitmapInfo(tag, null, b);
	}
	
	public static void printBitmapInfo(String tag, String infp, Bitmap b) {
		int w = b.getWidth();
		int h = b.getHeight();
		float size = ((float)(w * h * 4) / (float)(1024*1024));
		String text = null;
		if (infp==null) {
			String format = "Bitmap[w:%s, h:%s, %.2fmb]";
			text = String.format(format, w, h, size);
		} else {
			String format = "Bitmap[info:%s, w:%s, h:%s, %.2fmb]";
			text = String.format(format, infp, w, h, size);
		}
		EtaLog.d(tag, text);
	}
	
	public static void printScreen(String tag, Context c) {
		Point p = PageflipUtils.getDisplayDimen(c);
		String out = String.format("ScreenSize[w:%s, h:%s]", p.x, p.y);
		EtaLog.d(tag, out);
	}
	
	public static void printOptions(String tag, BitmapFactory.Options o) {
		String format = "Image[MimeType:%s, w:%s, h:%s]";
		String out = String.format(format, o.outMimeType, o.outWidth, o.outHeight);
	    EtaLog.d(tag, out);
	}
	
}
