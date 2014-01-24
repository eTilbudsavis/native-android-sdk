package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Unit extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Unit";
	
	private String mSymbol;
	private Si mSi;
	
	public Unit() { }
	
	public static Unit fromJSON(String unit) {
		Unit u = new Unit();
		try {
			u = fromJSON(u, new JSONObject(unit));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return u;
	}
	
	public static Unit fromJSON(JSONObject unit) {
		return fromJSON(new Unit(), unit);
	}
	
	public static Unit fromJSON(Unit u, JSONObject unit) {
		if (u == null) u = new Unit();
		if (unit == null) return u;
		
		try {
			u.setSymbol(jsonToString(unit, ServerKey.SYMBOL));
			u.setSi(Si.fromJSON(unit.getJSONObject(ServerKey.SI)));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
		return u;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Unit u) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.SYMBOL, u.getSymbol());
			o.put(ServerKey.SI, u.getSi() == null ? null : u.getSi().toJSON());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}

	public String getSymbol() {
		return mSymbol;
	}

	public Unit setSymbol(String symbol) {
		mSymbol = symbol;
		return this;
	}

	public Si getSi() {
		return mSi;
	}

	public Unit setSi(Si si) {
		mSi = si;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mSi == null) ? 0 : mSi.hashCode());
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
		Unit other = (Unit) obj;
		if (mSi == null) {
			if (other.mSi != null)
				return false;
		} else if (!mSi.equals(other.mSi))
			return false;
		if (mSymbol == null) {
			if (other.mSymbol != null)
				return false;
		} else if (!mSymbol.equals(other.mSymbol))
			return false;
		return true;
	}
	
	
}
