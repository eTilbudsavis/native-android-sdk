package com.eTilbudsavis.etasdk.EtaObjects.helper;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class Hotspot implements EtaObject<JSONObject>, Serializable {
	
	private static final long serialVersionUID = 7068341225117028048L;

	public static final String TAG = Hotspot.class.getSimpleName();
	
	/** The default significant area */
	public static final double SIGNIFICANT_AREA = 0.01d;
	
	private int mPage = 0;
	
	private Offer mOffer;
	
	private boolean mIsSpanningTwoPages = false;
	
	/** The top most part of the hotspot, relative to the catalog.dimensions */
	public double top = Double.MIN_VALUE;

	/** The bottom most part of the hotspot, relative to the catalog.dimensions */
	public double bottom = Double.MIN_VALUE;

	/** The left most part of the hotspot, relative to the catalog.dimensions */
	public double left = Double.MIN_VALUE;

	/** The top most part of the hotspot, relative to the catalog.dimensions */
	public double right = Double.MIN_VALUE;

	/** The top most part of the hotspot. This is the absolute value */
	public double absTop = Double.MIN_VALUE;

	/** The bottom most part of the hotspot. This is the absolute value */
	public double absBottom = Double.MIN_VALUE;

	/** The left most part of the hotspot. This is the absolute value */
	public double absLeft = Double.MIN_VALUE;

	/** The top most part of the hotspot. This is the absolute value */
	public double absRight = Double.MIN_VALUE;
	
	private int mColor = Color.TRANSPARENT;
	
	public static Hotspot fromJSON(JSONArray jHotspot) {
		Hotspot h = new Hotspot();
		if (jHotspot == null) {
			return h;
		}
		
		// We expect the first JSONArray to have an additional 4 JSONArray's
		if (jHotspot.length() != 4) {
			EtaLog.w(TAG, "Expected jHotspot.length == 4, actual length: " + jHotspot.length());
			return h;
		}
		
		for (int i = 0 ; i < jHotspot.length() ; i++) {
			
			try {
				
				JSONArray point = jHotspot.getJSONArray(i);
				if (point.length()!=2) {
					EtaLog.w(TAG, "Expected hotspot.point.length == 2, actual length: " + point.length());
					continue;
				}
				
				double x = Double.valueOf(point.getString(0));
				double y = Double.valueOf(point.getString(1));
				
				if (h.absLeft == Double.MIN_VALUE) {
					// Nothing set yet
					h.absLeft = x;
				} else if (h.absLeft > x) {
					// switch values
					h.absRight = h.absLeft;
					h.absLeft = x;
				} else {
					// no other options left
					h.absRight = x;
				}
				
				if (h.absTop == Double.MIN_VALUE) {
					// Nothing set yet
					h.absTop = y;
				} else if (h.absTop > y) {
					// switch values
					h.absBottom = h.absTop;
					h.absTop = y;
				} else {
					// no other options left
					h.absBottom = y;
				}
				
			} catch (JSONException e) {
				EtaLog.e(TAG, e.getMessage(), e);
			}
		}
		
		return h;
	}
	
	public void normalize(Dimension d) {
		top = absTop/d.getHeight();
		right = absRight/d.getWidth();
		bottom = absBottom/d.getHeight();
		left = absLeft/d.getWidth();
	}
	
	public boolean inBounds(double x, double y, double minArea, boolean landscape) {
		return inBounds(x, y) && isAreaSignificant(landscape);
	}
	
	public boolean inBounds(double x, double y) {
		return top < y && y < bottom && left < x && x < right;
	}
	
	public boolean isAreaSignificant(boolean landscape) {
		return isAreaSignificant(SIGNIFICANT_AREA, landscape);
	}
	
	public boolean isAreaSignificant(double minArea, boolean landscape) {
		if (!landscape && mIsSpanningTwoPages) {
			return getArea() > minArea;
		}
		return true;
	}
	
	public double getArea() {
		return Math.abs(top-bottom) * Math.abs(left-right);
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put("left", left);
			o.put("top", top);
			o.put("right", right);
			o.put("bottom", bottom);
			String offer = (mOffer==null?"null":mOffer.getHeading());
			o.put("offer", offer);
		} catch (JSONException e) {
			EtaLog.e(TAG, e.getMessage(), e);
		}
		return o;
	}

	public boolean isDualPage() {
		return mIsSpanningTwoPages;
	}

	public void setDualPage(boolean isDualPage) {
		mIsSpanningTwoPages = isDualPage;
	}
	
	public int getPage() {
		return mPage;
	}

	public void setPage(int page) {
		this.mPage = page;
	}

	public Offer getOffer() {
		return mOffer;
	}
	
	public void setOffer(Offer offer) {
		mOffer = offer;
	}
	
	public int getColor() {
		return mColor;
	}
	
	public void setColor(int color) {
		mColor = color;
	}
	
	@Override
	public String toString() {
		String offer = (mOffer==null?"null":mOffer.getHeading());
		String text = "hotspot[offer:%s, t:%.2f, r:%.2f, b:%.2f, l:%.2f, absT:%.2f, absR:%.2f, absB:%.2f, absL:%.2f]";
		return String.format(text, offer, top, right, bottom, left, absTop, absRight, absBottom, absLeft);
	}
	
}