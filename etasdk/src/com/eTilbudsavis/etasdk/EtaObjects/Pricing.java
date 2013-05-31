package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Pricing implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private double mPrice;
	private double mPrePrice;
	private String mCurrency;
	
	public Pricing() {
		mPrice = 0.0;
		mPrePrice = 0.0;
		mCurrency = "Currency";
	}
	
	public Pricing(JSONObject pricing) {
		try {
			mPrice = pricing.getDouble("price");
			mPrePrice = pricing.getDouble("preprice");
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

	public double getPrePrice() {
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
				mPrePrice == p.getPrePrice() &&
				mCurrency.equals(p.getCurrency());
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append(getClass().getSimpleName()).append("[")
		.append("Price=").append(mPrice)
		.append(", preprice=").append(mPrePrice)
		.append(", currency=").append(mCurrency)
		.append("]").toString();
		
	}
}
