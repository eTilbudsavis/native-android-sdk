package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Unit implements Serializable {

	private static final long serialVersionUID = 1L;

	private String mSymbol;
	
	public Unit() {
		mSymbol = "Symbol";
	}
	
	public Unit(JSONObject unit) {
		try {
			mSymbol = unit.getString("symbol");
		} catch (JSONException e) {
			e.printStackTrace();
		}
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
