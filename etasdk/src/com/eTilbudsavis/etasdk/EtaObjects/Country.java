package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;

public class Country implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final String S_ID = "id";
	private static final String S_AREA_ID = "area_id";
	private static final String S_COUNTRY = "country";
	private static final String S_LANGUAGE = "language";

	public static final String TAG = "Country";
	
	private int mId = 0;
	private int mAreaId = 0;
	private String mCountry;
	private String mLanguage;
	
	public Country() {
	}
	
	public static Country fromJSON(String country) {
		Country c = new Country();
		try {
			c = fromJSON(c, new JSONObject(country));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return c;
	}
	
	public static Country fromJSON(JSONObject country) {
		return fromJSON(new Country(), country);
	}
	
	private static Country fromJSON(Country c, JSONObject country) {
		if (c == null) c = new Country();
		if (country == null) return c;
		
		try {
			c.setId(country.getInt(S_ID));
			c.setAreaId(country.getInt(S_AREA_ID));
			c.setCountry(country.getString(S_COUNTRY));
			c.setLanguage(country.getString(S_LANGUAGE));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return c;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Country c) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_ID, c.getId());
			o.put(S_AREA_ID, c.getAreaId());
			o.put(S_COUNTRY, c.getCountry());
			o.put(S_LANGUAGE, c.getLanguage());
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return o;
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