package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Country implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Country";
	
	private int mId;
	private int mAreaId;
	private String mCountry;
	private String mLanguage;
	
	public Country(JSONObject country) {
		try {
			mId = country.getInt("id");
			mAreaId = country.getInt("area_id");
			mCountry = country.getString("country");
			mLanguage = country.getString("language");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public int getId() {
		return mId;
	}
	
	public void setId(int id) {
		this.mId = id;
	}
	
	/**
	 * Ern is not implemented for Country yet.<br>
	 * until it is implemented it will return {@link #getId() getId()}
	 * @return id of this Country
	 */
	public String getErn() {
		return String.valueOf(mId);
	}
	
	/**
	 * Due to lacking implementation server-side, this does nothing.
	 * @param ern
	 */
	public void setErn(String ern) {
	}
	
	public int getAreaId() {
		return mAreaId;
	}
	
	public void setAreaId(int areaId) {
		this.mAreaId = areaId;
	}
	
	public String getCountry() {
		return mCountry;
	}
	
	public void setCountry(String country) {
		this.mCountry = country;
	}
	
	public String getLanguage() {
		return mLanguage;
	}
	
	public void setLanguage(String language) {
		this.mLanguage = language;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Country))
			return false;

		Country c = (Country)o;
		return mId == c.getId() &&
				mAreaId == c.getAreaId() &&
				mCountry.equals(c.getCountry()) &&
				mLanguage.equals(c.getLanguage());
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[")
		.append(", country=").append(mCountry)
		.append(", language=").append(mLanguage);
		if (everything) {
			sb.append("id=").append(mId)
			.append(", areaId=").append(mAreaId);
		}
		return sb.append("]").toString();
	}
}