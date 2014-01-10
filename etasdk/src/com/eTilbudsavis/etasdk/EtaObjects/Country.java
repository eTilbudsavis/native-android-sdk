package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class Country extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;


	public static final String TAG = "Country";

	private String mId;
	private String mUnsubscribeUrl;
	
	public Country() { }
	
	public static Country fromJSON(JSONObject country) {
		return fromJSON(new Country(), country);
	}
	
	private static Country fromJSON(Country c, JSONObject country) {
		if (c == null) c = new Country();
		if (country == null) return c;
		
		c.setId(getJsonString(country, ServerKey.ID));
		c.setUnsubscribePrintUrl(getJsonString(country, ServerKey.UNSUBSCRIBE_PRINT_URL));
		
		return c;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Country c) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.ID, c.getId());
			o.put(ServerKey.UNSUBSCRIBE_PRINT_URL, c.getUnsubscribePrintUrl());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}

	public String getId() {
		return mId;
	}
	
	public void setId(String id) {
		this.mId = id;
	}

	public void setUnsubscribePrintUrl(String url) {
		this.mUnsubscribeUrl = url;
	}

	public String getUnsubscribePrintUrl() {
		return mUnsubscribeUrl;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
		result = prime * result
				+ ((mUnsubscribeUrl == null) ? 0 : mUnsubscribeUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Country other = (Country) obj;
		if (mId == null) {
			if (other.mId != null)
				return false;
		} else if (!mId.equals(other.mId))
			return false;
		if (mUnsubscribeUrl == null) {
			if (other.mUnsubscribeUrl != null)
				return false;
		} else if (!mUnsubscribeUrl.equals(other.mUnsubscribeUrl))
			return false;
		return true;
	}
	
	
	
}