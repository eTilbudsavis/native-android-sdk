package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Pricing implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Pricing";
	
	private double mPrice;
	private Double mPrePrice;
	private String mCurrency;
	
	public Pricing() {
		mPrice = 0.0;
		mPrePrice = 0.0;
		mCurrency = "Currency";
	}
	
	public Pricing(JSONObject pricing) {
		try {
			mPrice = pricing.getDouble("price");
			mPrePrice = pricing.getString("preprice").equals("null") == true ? null : pricing.getDouble("preprice");
			mCurrency = pricing.getString("currency");
		} catch (JSONException e) {
			e.printStackTrace();
		}
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

	public Pricing setPrePrice(double prePrice) {
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Pricing))
			return false;

		Pricing p = (Pricing)o;
		return mPrice == p.getPrice() &&
				mPrePrice == null ? p.getPrePrice() == null : mPrePrice == p.getPrePrice() &&
				mCurrency.equals(p.getCurrency());
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("Price=").append(mPrice)
		.append(", preprice=").append(mPrePrice == null ? "null" : mPrePrice)
		.append(", currency=").append(mCurrency)
		.append("]").toString();
		
	}
}
