package com.eTilbudsavis.etasdk.EtaObjects.helper;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class Hotspot implements EtaObject<JSONObject>, Serializable {
	
	private static final long serialVersionUID = 7068341225117028048L;

	public static final String TAG = Hotspot.class.getSimpleName();
	
	/** The top most part of the hotspot, relative to the catalog.dimensions */
	public double top = Double.MIN_VALUE;

	/** The bottom most part of the hotspot, relative to the catalog.dimensions */
	public double bottom = Double.MIN_VALUE;

	/** The left most part of the hotspot, relative to the catalog.dimensions */
	public double left = Double.MIN_VALUE;

	/** The top most part of the hotspot, relative to the catalog.dimensions */
	public double right = Double.MIN_VALUE;
	
	private Offer mOffer;
	
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
				
				if (h.left == Double.MIN_VALUE) {
					// Nothing set yet
					h.left = x;
				} else if (h.left > x) {
					// switch values
					h.right = h.left;
					h.left = x;
				} else {
					// no other options left
					h.right = x;
				}
				
				if (h.top == Double.MIN_VALUE) {
					// Nothing set yet
					h.top = y;
				} else if (h.top > y) {
					// switch values
					h.bottom = h.top;
					h.top = y;
				} else {
					// no other options left
					h.bottom = y;
				}
				
			} catch (JSONException e) {
				EtaLog.e(TAG, e.getMessage(), e);
			}
		}
		return h;
	}
	
	public Hotspot() {
		
	}
	
	public boolean contains(double x, double y) {
		return (top < x && x < bottom ) && (left < y && y < right);
	}
	
	public JSONObject toJSON() {
		return null;
	}
	
	public Offer getOffer() {
		return mOffer;
	}
	
	public void setOffer(Offer offer) {
		mOffer = offer;
	}
	
	@Override
	public String toString() {
		String offer = (mOffer==null?"null":mOffer.getHeading());
		return "hotspot[offer:" + offer + ", t:" + top + ", b:" + bottom + ", l:" + left + ", r:" + right + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(bottom);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(left);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((mOffer == null) ? 0 : mOffer.hashCode());
		temp = Double.doubleToLongBits(right);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(top);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Hotspot other = (Hotspot) obj;
		if (Double.doubleToLongBits(bottom) != Double
				.doubleToLongBits(other.bottom))
			return false;
		if (Double.doubleToLongBits(left) != Double
				.doubleToLongBits(other.left))
			return false;
		if (mOffer == null) {
			if (other.mOffer != null)
				return false;
		} else if (!mOffer.equals(other.mOffer))
			return false;
		if (Double.doubleToLongBits(right) != Double
				.doubleToLongBits(other.right))
			return false;
		if (Double.doubleToLongBits(top) != Double.doubleToLongBits(other.top))
			return false;
		return true;
	}
	
}