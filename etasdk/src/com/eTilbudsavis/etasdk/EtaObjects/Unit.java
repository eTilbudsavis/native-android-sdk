package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.Utils;

public class Unit extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Unit";
	
	private String mSymbol;
	private Si mSi;
	
	public Unit() {;
	}
	
	public static Unit fromJSON(String unit) {
		Unit u = new Unit();
		try {
			u = fromJSON(u, new JSONObject(unit));
		} catch (JSONException e) {
			Utils.logd(TAG, e);
		}
		return u;
	}
	
	@SuppressWarnings("unchecked")
	public static Unit fromJSON(JSONObject unit) {
		return fromJSON(new Unit(), unit);
	}
	
	public static Unit fromJSON(Unit u, JSONObject unit) {
		if (u == null) u = new Unit();
		if (unit == null) return u;
		
		try {
			u.setSymbol(getJsonString(unit, S_SYMBOL));
			u.setSi(Si.fromJSON(unit.getJSONObject(S_SI)));
		} catch (JSONException e) {
			Utils.logd(TAG, e);
		}
		
		return u;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Unit u) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_SYMBOL, u.getSymbol());
			o.put(S_SI, u.getSi() == null ? null : u.getSi().toJSON());
		} catch (JSONException e) {
			Utils.logd(TAG, e);
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Unit))
			return false;

		Unit u = (Unit)o;
		return stringCompare(mSymbol, u.getSymbol());
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("Symbol=").append(mSymbol)
		.append("]").toString();
		
	}
}
