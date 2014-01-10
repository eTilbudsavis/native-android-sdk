package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Si  extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Unit";
	
	private String mSymbol;
	private double mFactor = 1;
	
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
	
	public static Si fromJSON(JSONObject si) {
		return fromJSON(new Si(), si);
	}
	
	public static Si fromJSON(Si s, JSONObject si) {
		if (s == null) s = new Si();
		if (si == null) return s;
		
		try {
			s.setSymbol(getJsonString(si, ServerKey.SYMBOL));
			s.setFactor(si.getDouble(ServerKey.FACTOR));
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
			o.put(ServerKey.SYMBOL, s.getSymbol());
			o.put(ServerKey.FACTOR, s.getFactor());
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

	public Double getFactor() {
		return mFactor;
	}

	public Si setFactor(Double factor) {
		mFactor = factor == null ? 1 : factor;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(mFactor);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((mSymbol == null) ? 0 : mSymbol.hashCode());
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
		Si other = (Si) obj;
		if (Double.doubleToLongBits(mFactor) != Double
				.doubleToLongBits(other.mFactor))
			return false;
		if (mSymbol == null) {
			if (other.mSymbol != null)
				return false;
		} else if (!mSymbol.equals(other.mSymbol))
			return false;
		return true;
	}
	
	
}