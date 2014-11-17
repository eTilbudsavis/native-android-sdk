package com.eTilbudsavis.etasdk.pageflip;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Page;
import com.eTilbudsavis.etasdk.ImageLoader.ImageDebugger;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;

/**
 * Methods in this class <b>CHANGES A LOT</b>, and will break your stuff.
 * Please don't use them for anything serious.
 * @author Danny Hvam - danny@etilbudsavis.dk 
 *
 */
public class PU {
	
	public static final long KILO_BYTE = 0x400;
	
	private PU() {
		
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
		int size = (w * h * 4) / (int)KILO_BYTE;
		if (infp==null) {
			EtaLog.d(tag, "Bitmap[w:" + w + ", h:" + h + ", " + size + "kb]");
		} else {
			EtaLog.d(tag, "Bitmap[info:" + infp + ", w:" + w + ", h:" + h + ", " + size + "kb]");
		}
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
	
	public static ImageDebugger getSimpleDebugger(final String tag) {
		return new ImageDebugger() {
			
			public void debug(ImageRequest ir) {
				EtaLog.d(tag, ir.getFileName() + ", " + ir.getLog().getTotalDuration());
			}
		};
	}
	
	public static void cacheAllImages(final String tag, final Context ctx, final Catalog c) {
		Runnable downloader = new Runnable() {
			
			public void run() {
				
				toast(ctx, "Downloading " + c.getBranding().getName());
				int count = 0;
				ImageRequest t = null;
				ImageRequest v = null;
				ImageRequest z = null;
				
				for (Page p : c.getPages()) {
					
					while ((t != null && v != null && z != null) &&
							(!t.isFinished() || !v.isFinished() || !z.isFinished())) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// nothing really
						}
					}
					
					count++;
					
					// Get thumb
					t = display(p.getThumb(), ctx);
					v = display(p.getView(), ctx);
					z = display(p.getZoom(), ctx);
					
					// Give feedback
					String s = String.format("%s / %s", count, c.getPageCount());
					EtaLog.d(tag, s);
					if (count%5==0) {
						toast(ctx, s);
					}
					
				}
				toast(ctx, "Finished downloading " + c.getBranding().getName());
			}
		};
		new Thread(downloader).start();
	}
	
	private static ImageRequest display(String url, Context ctx) {
		ImageRequest ir = new ImageRequest(url, new ImageView(ctx));
		ir.setFileName(new PageflipFileNameGenerator());
		ImageLoader.getInstance().displayImage(ir);
		return ir;
	}
	
	private static void toast(final Context ctx, final String s) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			
			public void run() {
				Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	
}
