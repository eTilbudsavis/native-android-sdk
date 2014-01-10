package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Dimension extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Dimension";
	
	private static final String S_WIDTH = "width";
	private static final String S_HEIGHT = "height";
	
	private double mWidth = 0.0;
	private double mHeight = 0.0;
	
	public Dimension() {
	}
	
	public static Dimension fromJSON(String dimension) {
		Dimension d = new Dimension();
		try {
			d = fromJSON(d, new JSONObject(dimension));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return d;
	}
	
	public static Dimension fromJSON(JSONObject dimension) {
		return fromJSON(new Dimension(), dimension);
	}
	
	public static Dimension fromJSON(Dimension d, JSONObject dimension) {
		if (d == null) d = new Dimension();
		if (dimension == null) return d;
		
		try {
			d.setWidth(dimension.getDouble(S_WIDTH));
			d.setHeight(dimension.getDouble(S_HEIGHT));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return d;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Dimension d) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_HEIGHT, d.getHeight());
			o.put(S_WIDTH, d.getWidth());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}
	
	public double getWidth() {
		return mWidth;
	}

	public Dimension setWidth(double width) {
		mWidth = width;
		return this;
	}

	public double getHeight() {
		return mHeight;
	}

	public Dimension setHeight(double height) {
		mHeight = height;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(mHeight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mWidth);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dimension other = (Dimension) obj;
		if (Double.doubleToLongBits(mHeight) != Double
				.doubleToLongBits(other.mHeight))
			return false;
		if (Double.doubleToLongBits(mWidth) != Double
				.doubleToLongBits(other.mWidth))
			return false;
		return true;
	}
	
	
}
