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
		// TODO Auto-generated constructor stub
	}
	
	public static String getName(String url) {
		String s[] = url.split("/");
		int l = s.length-1;
		return s[l-1] + "-" + s[l];
	}
	
	public static String getName(ImageRequest ir) {
		return getName(ir.getUrl());
	}
	
	public static void printName(String tag, ImageRequest ir) {
		EtaLog.d(tag, getName(ir));
	}
	
	public static void printHeap(String tag) {
		Runtime rt = Runtime.getRuntime();
		long free = rt.freeMemory()/KILO_BYTE;
		long available = rt.totalMemory()/KILO_BYTE;
		long max = rt.maxMemory()/KILO_BYTE;
		EtaLog.d(tag, "Heap[free: " + free + " (of " + available + "), max: " + max);
	}
	
	public static void printMemory(String tag, Context c) {
		ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
		try {
			EtaLog.d(tag, "LargeHeap[Mem: " + am.getMemoryClass() + ", LargeMem: " + am.getLargeMemoryClass() + "]");
		} catch (Throwable t) {
			EtaLog.d(tag, "LargeHeap[Mem: " + am.getMemoryClass() + "]");
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
		int size = (w * h * 4) / (int)KILO_BYTE;
		if (infp==null) {
			EtaLog.d(tag, "Bitmap[w:" + w + ", h:" + h + ", " + size + "kb]");
		} else {
			EtaLog.d(tag, "Bitmap[info:" + infp + ", w:" + w + ", h:" + h + ", " + size + "kb]");
		}
	}
	
	public static void printScreen(String tag, Context c) {
		Point p = getDisplayDimen(c);
		String out = String.format("ScreenSize[w:%s, h:%s]", p.x, p.y);
		EtaLog.d(tag, out);
	}
	
	@SuppressWarnings("deprecation")
	public static Point getDisplayDimen(Context c) {
		Point p = new Point();
		WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR2) {
			display.getSize(p);
		} else {
			p.y = display.getHeight();
			p.x = display.getWidth();
		}
		return p;
	}
	
	public static void printOptions(String tag, BitmapFactory.Options o) {
		String format = "Image[MimeType:%s, w:%s, h:%s]";
		String out = String.format(format, o.outMimeType, o.outWidth, o.outHeight);
	    EtaLog.d(tag, out);
	}
	
	public static ImageDebugger getSimpleDebugger(final String tag) {
		return new ImageDebugger() {
			
			public void debug(ImageRequest ir) {
				EtaLog.d(tag, getName(ir) + ", " + ir.getLog().getTotalDuration());
			}
		};
	}
	

	public static void cacheAllImages(final String tag, final Context ctx, final Catalog c) {
		Runnable downloader = new Runnable() {
			
			public void run() {

				toast(ctx, "Downloading " + c.getBranding().getName());
				int count = 0;
				ImageLoader i = ImageLoader.getInstance();
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
					t = new ImageRequest(p.getThumb(), new ImageView(ctx));
					v = new ImageRequest(p.getView(), new ImageView(ctx));
					z = new ImageRequest(p.getZoom(), new ImageView(ctx));
					i.displayImage(t);
					i.displayImage(v);
					i.displayImage(z);
					
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
	
	private static void toast(final Context ctx, final String s) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			
			public void run() {
				Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	
}
