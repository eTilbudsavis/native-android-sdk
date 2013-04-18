package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Country implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String mId;
	private String mAlpha2;
	private String mCode;
	private String mName;
	private String mUnsubscribePrintUrl;
	
	public Country(JSONObject country) {
		try {
			mId = country.getString("id");
			mAlpha2 = country.getString("alpha2");
			mCode = country.getString("code");
			mName = country.getString("name");
			mUnsubscribePrintUrl = country.getString("unsubscribePrintUrl");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	public Country setId(String id) {
		this.mId = id;
		return this;
	}

	public String getId() {
		return mId;
	}

	public Country setAlpha2(String alpha2) {
		this.mAlpha2 = alpha2;
		return this;
	}

	public String getAlpha2() {
		return mAlpha2;
	}

	public Country setCode(String code) {
		this.mCode = code;
		return this;
	}

	public String getCode() {
		return mCode;
	}

	public Country setName(String name) {
		this.mName = name;
		return this;
	}

	public String getName() {
		return mName;
	}

	public Country setUnsubscribePrintUrl(String mUnsubscribePrintUrl) {
		this.mUnsubscribePrintUrl = mUnsubscribePrintUrl;
		return this;
	}
	
	public String getUnsubscribePrintUrl() {
		return mUnsubscribePrintUrl;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Country))
			return false;

		Country c = (Country)o;
		return mId.equals(c.getId()) &&
				mAlpha2.equals(c.getAlpha2()) &&
				mCode.equals(c.getCode()) &&
				mName.equals(c.getName()) &&
				mUnsubscribePrintUrl.equals(c.getUnsubscribePrintUrl());
	}
	
	
}