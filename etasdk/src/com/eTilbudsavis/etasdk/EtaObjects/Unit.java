package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;

public class Unit extends EtaObject implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Unit";
	
	private String mSymbol;
	
	public Unit() {;
	}
	
	public static Unit fromJSON(String unit) {
		Unit u = new Unit();
		try {
			u = fromJSON(u, new JSONObject(unit));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
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
			u.setSymbol(unit.getString(S_SYMBOL));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
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
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
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
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Unit))
			return false;

		Unit u = (Unit)o;
		return mSymbol.equals(u.getSymbol());
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("Symbol=").append(mSymbol)
		.append("]").toString();
		
	}
}
