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
	
	@SuppressWarnings("unchecked")
	public static Size fromJSON(JSONObject size) {
		return fromJSON(new Size(), size);
	}
	
	public static Size fromJSON(Size s, JSONObject size) {
		if (s == null) s = new Size();
		if (size == null) return s;
		
		try {
			s.setFrom(size.getDouble(Key.FROM));
			s.setTo(size.getDouble(Key.TO));
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
			o.put(Key.FROM, s.getFrom());
			o.put(Key.TO, s.getTo());
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Size))
			return false;

		Size s = (Size)o;
		return mFrom == s.getFrom() &&
				mTo == s.getTo();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("from=").append(mFrom)
		.append(", to=").append(mTo)
		.append("]").toString();
		
	}
	
}
