package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Si  extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Unit";
	
	private String mSymbol;
	private double mFactor;
	
	public Si() {
		
	}
	
	public static Si fromJSON(String si) {
		Si s = new Si();
		try {
			s = fromJSON(s, new JSONObject(si));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return s;
	}
	
	@SuppressWarnings("unchecked")
	public static Si fromJSON(JSONObject si) {
		return fromJSON(new Si(), si);
	}
	
	public static Si fromJSON(Si s, JSONObject si) {
		if (s == null) s = new Si();
		if (si == null) return s;
		
		try {
			s.setSymbol(getJsonString(si, S_SYMBOL));
			s.setFactor(si.getDouble(S_FACTOR));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
		return s;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Si s) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_SYMBOL, s.getSymbol());
			o.put(S_FACTOR, s.getFactor());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}

	public String getSymbol() {
		return mSymbol;
	}

	public Si setSymbol(String symbol) {
		mSymbol = symbol;
		return this;
	}

	public double getFactor() {
		return mFactor;
	}

	public Si setFactor(double factor) {
		mFactor = factor;
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Si))
			return false;

		Si s = (Si)o;
		return stringCompare(mSymbol, s.getSymbol()) &&
				mFactor == s.getFactor();
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("symbol=").append(mSymbol)
		.append("factor=").append(mFactor)
		.append("]").toString();
		
	}
}