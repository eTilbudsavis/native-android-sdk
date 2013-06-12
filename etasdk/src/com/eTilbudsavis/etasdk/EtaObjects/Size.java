package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Size implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String S_FROM = "from";
	private static final String S_TO = "to";
	
	public static final String TAG = "Size";
	
	private double mFrom = 0.0;
	private double mTo = 0.0;
	
	public Size() {
	}
	
	public static Size fromJSON(String size) {
		try {
			return fromJSON(new Size(), new JSONObject(size));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Size fromJSON(JSONObject size) {
		return fromJSON(new Size(), size);
	}
	
	public static Size fromJSON(Size s, JSONObject size) {
		if (s == null) s = new Size();
		if (size == null) return s;
		
		try {
			s.setFrom(size.getDouble(S_FROM));
			s.setTo(size.getDouble(S_TO));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Size s) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_FROM, s.getFrom());
			o.put(S_TO, s.getTo());
		} catch (JSONException e) {
			o = null;
			e.printStackTrace();
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
