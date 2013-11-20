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
	
	@SuppressWarnings("unchecked")
	public static Country fromJSON(JSONObject country) {
		return fromJSON(new Country(), country);
	}
	
	private static Country fromJSON(Country c, JSONObject country) {
		if (c == null) c = new Country();
		if (country == null) return c;
		
		c.setId(getJsonString(country, Key.ID));
		c.setUnsubscribePrintUrl(getJsonString(country, Key.SERVER_UNSUBSCRIBE_PRINT_URL));
		
		return c;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Country c) {
		JSONObject o = new JSONObject();
		try {
			o.put(Key.ID, c.getId());
			o.put(Key.SERVER_UNSUBSCRIBE_PRINT_URL, c.getUnsubscribePrintUrl());
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Country))
			return false;

		Country c = (Country)o;
		return stringCompare(mId, c.getId()) &&
				stringCompare(mUnsubscribeUrl, c.getUnsubscribePrintUrl());
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[")
		.append(", id=").append(mId)
		.append(", unsubscribe_url=").append(mUnsubscribeUrl);
		return sb.append("]").toString();
	}
}