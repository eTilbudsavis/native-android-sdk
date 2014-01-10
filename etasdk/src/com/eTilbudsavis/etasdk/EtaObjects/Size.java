package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Size extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Size";
	
	private double mFrom = 1;
	private double mTo = 1;
	
	public Size() {
	}
	
	public static Size fromJSON(String size) {
		Size s = new Size();
		try {
			s = fromJSON(s, new JSONObject(size));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return s;
	}
	
	public static Size fromJSON(JSONObject size) {
		return fromJSON(new Size(), size);
	}
	
	public static Size fromJSON(Size s, JSONObject size) {
		if (s == null) s = new Size();
		if (size == null) return s;
		
		try {
			s.setFrom(size.getDouble(ServerKey.FROM));
			s.setTo(size.getDouble(ServerKey.TO));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return s;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Size s) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.FROM, s.getFrom());
			o.put(ServerKey.TO, s.getTo());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}
	
	public double getFrom() {
		return mFrom;
	}
	
	public Size setFrom(double from) {
		mFrom = from;
		return this;
	}
	
	public double getTo() {
		return mTo;
	}
	
	public Size setTo(double to) {
		mTo = to;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(mFrom);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mTo);
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
		Size other = (Size) obj;
		if (Double.doubleToLongBits(mFrom) != Double
				.doubleToLongBits(other.mFrom))
			return false;
		if (Double.doubleToLongBits(mTo) != Double.doubleToLongBits(other.mTo))
			return false;
		return true;
	}
	
	
}
