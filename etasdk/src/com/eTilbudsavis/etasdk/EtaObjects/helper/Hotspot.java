package com.eTilbudsavis.etasdk.EtaObjects.helper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.PointF;

import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class Hotspot implements EtaObject<JSONObject> {
	
	public static final String TAG = Hotspot.class.getSimpleName();
	
	private PointF mNorthWest = new PointF(Float.MIN_VALUE, Float.MIN_VALUE);
	private PointF mSouthEast = new PointF(Float.MIN_VALUE, Float.MIN_VALUE);
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
				
				float x = Float.valueOf(point.getString(0));
				float y = Float.valueOf(point.getString(1));
				
				if (h.getNorthWest().x == Float.MIN_VALUE) {
					// Nothing set yet
					h.getNorthWest().x = x;
				} else if (h.getNorthWest().x > x) {
					// switch values
					h.getSouthEast().x = h.getNorthWest().x;
					h.getNorthWest().x = x;
				} else {
					// no other options left
					h.getSouthEast().x = x;
				}
				
				if (h.getNorthWest().y == Float.MIN_VALUE) {
					// Nothing set yet
					h.getNorthWest().y = y;
				} else if (h.getNorthWest().y > y) {
					// switch values
					h.getSouthEast().y = h.getNorthWest().y;
					h.getNorthWest().y = y;
				} else {
					// no other options left
					h.getSouthEast().y = y;
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
		return (mNorthWest.x < x && x < mSouthEast.x) && (mNorthWest.y < y && y < mSouthEast.y);
	}
	
	public JSONObject toJSON() {
		return null;
	}
	
	public PointF getNorthWest() {
		return mNorthWest;
	}
	
	public void setNorthWest(PointF northWest) {
		mNorthWest = northWest;
	}
	
	public PointF getSouthEast() {
		return mSouthEast;
	}
	
	public void setSouthEast(PointF southEast) {
		mSouthEast = southEast;
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
		return Hotspot.class.getSimpleName() + "[offer:" + offer + ", northWest(" + mNorthWest.toString() + "), southEast(" + mSouthEast.toString() + ")]";
	}
	
}