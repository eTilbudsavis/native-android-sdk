package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Pricing extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Pricing";
	
	private double mPrice = 0.0;
	private Double mPrePrice;
	private String mCurrency;
	
	public Pricing() {
		
	}
	
	public static Pricing fromJSON(JSONObject pricing) {
		return fromJSON(new Pricing(), pricing);
	}
	
	public static Pricing fromJSON(Pricing p, JSONObject pricing) {
		if (p == null) p = new Pricing();
		if (pricing == null) return p;
		
		try {
			p.setPrice(Json.valueOf(pricing, ServerKey.PRICE, 0.0));
			p.setPrePrice(pricing.isNull(ServerKey.PREPRICE) ? null : pricing.getDouble(ServerKey.PREPRICE));
			p.setCurrency(Json.valueOf(pricing, ServerKey.CURRENCY));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return p;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.PRICE, getPrice());
			o.put(ServerKey.PREPRICE, Json.nullCheck(getPrePrice()));
			o.put(ServerKey.CURRENCY, Json.nullCheck(getCurrency()));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}
	
	public double getPrice() {
		return mPrice;
	}

	public Pricing setPrice(double price) {
		mPrice = price;
		return this;
	}

	public Double getPrePrice() {
		return mPrePrice;
	}

	public Pricing setPrePrice(Double prePrice) {
		mPrePrice = prePrice;
		return this;
	}

	public String getCurrency() {
		return mCurrency;
	}

	public Pricing setCurrency(String currency) {
		mCurrency = currency;
		return this;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mCurrency == null) ? 0 : mCurrency.hashCode());
		result = prime * result
				+ ((mPrePrice == null) ? 0 : mPrePrice.hashCode());
		long temp;
		temp = Double.doubleToLongBits(mPrice);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pricing other = (Pricing) obj;
		if (mCurrency == null) {
			if (other.mCurrency != null)
				return false;
		} else if (!mCurrency.equals(other.mCurrency))
			return false;
		if (mPrePrice == null) {
			if (other.mPrePrice != null)
				return false;
		} else if (!mPrePrice.equals(other.mPrePrice))
			return false;
		if (Double.doubleToLongBits(mPrice) != Double
				.doubleToLongBits(other.mPrice))
			return false;
		return true;
	}

	
}
