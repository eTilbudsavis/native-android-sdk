package com.eTilbudsavis.etasdk.pageflip;

import android.graphics.Bitmap;

import com.eTilbudsavis.etasdk.ImageLoader.ImageDebugger;
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

	public static String getName(ImageRequest ir) {
		String s[] = ir.getUrl().split("/");
		int l = s.length-1;
		return s[l-1] + "-" + s[l];
	}
	
	public static void printHeapInfo(String tag) {
		Runtime rt = Runtime.getRuntime();
		long free = rt.freeMemory()/KILO_BYTE;
		long available = rt.totalMemory()/KILO_BYTE;
		long max = rt.maxMemory()/KILO_BYTE;
		EtaLog.d(tag, "Heap[free: " + free + " (of " + available + "), max: " + max);
	}
	
	public static void printBitmapInfo(String tag, String properties, Bitmap b) {
		int w = b.getWidth();
		int h = b.getHeight();
		int size = (w * h * 4) / (int)KILO_BYTE;
		EtaLog.d(tag, "Bitmap[prop:" + properties + ", " + w + "x" + h + ", " + size + "kb]");
	}
	
	public static ImageDebugger getSimpleDebugger(final String tag) {
		return new ImageDebugger() {
			
			public void debug(ImageRequest ir) {
				EtaLog.d(tag, getName(ir) + ", " + ir.getLog().getTotalDuration());
			}
		};
	}
}
