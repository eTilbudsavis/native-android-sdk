package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;

public class Country extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;


	public static final String TAG = "Country";
	
	private int mId = 0;
	private int mAreaId = 0;
	private String mCountry;
	private String mLanguage;
	
	public Country() { }
	
	@SuppressWarnings("unchecked")
	public static Country fromJSON(JSONObject country) {
		return fromJSON(new Country(), country);
	}
	
	private static Country fromJSON(Country c, JSONObject country) {
		if (c == null) c = new Country();
		if (country == null) return c;
		
		try {
			c.setId(country.getInt(EtaObject.S_ID));
			c.setAreaId(country.getInt(EtaObject.S_AREA_ID));
			c.setCountry(getJsonString(country, S_COUNTRY));
			c.setLanguage(getJsonString(country, S_LANGUAGE));
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
			o.put(EtaObject.S_ID, c.getId());
			o.put(EtaObject.S_AREA_ID, c.getAreaId());
			o.put(EtaObject.S_COUNTRY, c.getCountry());
			o.put(EtaObject.S_LANGUAGE, c.getLanguage());
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
						stringCompare(mCountry, c.getCountry()) &&
						stringCompare(mLanguage, c.getLanguage());
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