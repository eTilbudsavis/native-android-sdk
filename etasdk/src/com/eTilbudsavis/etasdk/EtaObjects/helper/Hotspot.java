package com.eTilbudsavis.etasdk.EtaObjects.helper;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import android.os.Parcelable;
import android.os.Parcel;

public class Hotspot implements EtaObject<JSONObject>, Serializable, Parcelable {
	
	private static final long serialVersionUID = 7068341225117028048L;

	public static final String TAG = Hotspot.class.getSimpleName();
	
	/** The default significant area */
	public static final double SIGNIFICANT_AREA = 0.01d;
	
	private int mPage = 0;
	
	private Offer mOffer;
	
	private boolean mIsSpanningTwoPages = false;
	
	/** The top most part of the hotspot, relative to the catalog.dimensions */
	public double mTop = Double.MIN_VALUE;

	/** The bottom most part of the hotspot, relative to the catalog.dimensions */
	public double mBottom = Double.MIN_VALUE;

	/** The left most part of the hotspot, relative to the catalog.dimensions */
	public double mLeft = Double.MIN_VALUE;

	/** The top most part of the hotspot, relative to the catalog.dimensions */
	public double mRight = Double.MIN_VALUE;

	/** The top most part of the hotspot. This is the absolute value */
	public double mAbsTop = Double.MIN_VALUE;

	/** The bottom most part of the hotspot. This is the absolute value */
	public double mAbsBottom = Double.MIN_VALUE;

	/** The left most part of the hotspot. This is the absolute value */
	public double mAbsLeft = Double.MIN_VALUE;

	/** The top most part of the hotspot. This is the absolute value */
	public double mAbsRight = Double.MIN_VALUE;
	
	private int mColor = Color.TRANSPARENT;

	public static Parcelable.Creator<Hotspot> CREATOR = new Parcelable.Creator<Hotspot>(){
		public Hotspot createFromParcel(Parcel source) {
			return new Hotspot(source);
		}
		public Hotspot[] newArray(int size) {
			return new Hotspot[size];
		}
	};
	
	public Hotspot() {
		
	}
	
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
				
				if (h.mAbsLeft == Double.MIN_VALUE) {
					// Nothing set yet
					h.mAbsLeft = x;
				} else if (h.mAbsLeft > x) {
					// switch values
					h.mAbsRight = h.mAbsLeft;
					h.mAbsLeft = x;
				} else {
					// no other options left
					h.mAbsRight = x;
				}
				
				if (h.mAbsTop == Double.MIN_VALUE) {
					// Nothing set yet
					h.mAbsTop = y;
				} else if (h.mAbsTop > y) {
					// switch values
					h.mAbsBottom = h.mAbsTop;
					h.mAbsTop = y;
				} else {
					// no other options left
					h.mAbsBottom = y;
				}
				
			} catch (JSONException e) {
				EtaLog.e(TAG, e.getMessage(), e);
			}
		}
		
		return h;
	}
	
	public void normalize(Dimension d) {
		mTop = mAbsTop/d.getHeight();
		mRight = mAbsRight/d.getWidth();
		mBottom = mAbsBottom/d.getHeight();
		mLeft = mAbsLeft/d.getWidth();
	}
	
	public boolean inBounds(double x, double y, double minArea, boolean landscape) {
		return inBounds(x, y) && isAreaSignificant(landscape);
	}
	
	public boolean inBounds(double x, double y) {
		return mTop < y && y < mBottom && mLeft < x && x < mRight;
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
		return Math.abs(mTop-mBottom) * Math.abs(mLeft-mRight);
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put("left", mLeft);
			o.put("top", mTop);
			o.put("right", mRight);
			o.put("bottom", mBottom);
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
		return String.format(text, offer, mTop, mRight, mBottom, mLeft, mAbsTop, mAbsRight, mAbsBottom, mAbsLeft);
	}

	private Hotspot(Parcel in) {
		this.mPage = in.readInt();
		this.mOffer = in.readParcelable(Offer.class.getClassLoader());
		this.mIsSpanningTwoPages = in.readByte() != 0;
		this.mTop = in.readDouble();
		this.mBottom = in.readDouble();
		this.mLeft = in.readDouble();
		this.mRight = in.readDouble();
		this.mAbsTop = in.readDouble();
		this.mAbsBottom = in.readDouble();
		this.mAbsLeft = in.readDouble();
		this.mAbsRight = in.readDouble();
		this.mColor = in.readInt();
	}

	public int describeContents() { 
		return 0; 
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.mPage);
		dest.writeParcelable(this.mOffer, flags);
		dest.writeByte(mIsSpanningTwoPages ? (byte) 1 : (byte) 0);
		dest.writeDouble(this.mTop);
		dest.writeDouble(this.mBottom);
		dest.writeDouble(this.mLeft);
		dest.writeDouble(this.mRight);
		dest.writeDouble(this.mAbsTop);
		dest.writeDouble(this.mAbsBottom);
		dest.writeDouble(this.mAbsLeft);
		dest.writeDouble(this.mAbsRight);
		dest.writeInt(this.mColor);
	}
	
}