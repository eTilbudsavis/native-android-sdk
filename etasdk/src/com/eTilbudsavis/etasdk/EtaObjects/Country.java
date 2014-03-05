package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Country extends EtaErnObject<Country> implements Serializable {

	public static final String TAG = "Country";
	
	private static final long serialVersionUID = 1L;
	
	private String mUnsubscribeUrl;
	
	/**
	 * Default constructor
	 */
	public Country() { }

	/**
	 * Convert a {@link JSONArray} into a {@link List} of Country.
	 * @param list A {@link JSONArray} containing API v2 Country objects
	 * @return A {@link List} of Country
	 */
	public static List<Country> fromJSON(JSONArray countries) {
		List<Country> list = new ArrayList<Country>();
		try {
			for (int i = 0 ; i < countries.length() ; i++) {
					list.add(Country.fromJSON(countries.getJSONObject(i)));
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}
	
	/**
	 * A factory method for converting JSON into POJO.
	 * @param country A {@link JSONArray} containing API v2 country objects
	 * @return A Country object
	 */
	public static Country fromJSON(JSONObject country) {
		return fromJSON(new Country(), country);
	}
	
	private static Country fromJSON(Country c, JSONObject country) {
		if (c == null) c = new Country();
		if (country == null) return c;
		
		c.setId(Json.valueOf(country, ServerKey.ID));
		c.setUnsubscribePrintUrl(Json.valueOf(country, ServerKey.UNSUBSCRIBE_PRINT_URL));
		
		return c;
	}
	
	@Override
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	/**
	 * Static method for converting object into {@link JSONObject}, same as {@link EtaObject#toJSON() toJson()}
	 * @see EtaObject#toJSON()
	 * @param typeahead A object to convert
	 * @return A {@link JSONObject} representation of the Country
	 */
	public static JSONObject toJSON(Country country) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.ID, Json.nullCheck(country.getId()));
			o.put(ServerKey.UNSUBSCRIBE_PRINT_URL, Json.nullCheck(country.getUnsubscribePrintUrl()));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}

	@Override
	public String getErnPrefix() {
		return ERN_COUNTRY;
	}
	
	/**
	 * Get the country code of this country. The country codes are two-letter uppercase ISO country codes (such as "US")
	 * as defined by ISO 3166-1 (alfa-2), see <a href="http://da.wikipedia.org/wiki/ISO_3166-1">wikipedia</a> for more info.
	 * @return A String if id exists, or null
	 */
	@Override
	public String getId() {
		return super.getId();
	}

	/**
	 * Set the country code of this country. The country codes must two-letter uppercase ISO country codes (such as "US")
	 * as defined by ISO 3166-1 (alfa-2), see <a href="http://da.wikipedia.org/wiki/ISO_3166-1">wikipedia</a> for more info.
	 * @return A String
	 */
	@SuppressLint("DefaultLocale")
	@Override
	public Country setId(String id) {
		if (id != null && id.length() == 2) {
			super.setId(id.toUpperCase());
		} else {
			EtaLog.d(TAG, "The country code: " + id + " isn't allowed, see documentation for more details");
		}
		return this;
	}
	
	/**
	 * Set the URL to a website in which is it possible for the user to 'unsubscribe' them or their
	 * household from receiving the physical catalogs.
	 * @param url
	 */
	public void setUnsubscribePrintUrl(String url) {
		mUnsubscribeUrl = url;
	}
	
	/**
	 * This method returns an URL to a website in which is it possible for the user to 'unsubscribe' them or their
	 * household from receiving the physical catalogs.
	 * @return An url if one exists, else null
	 */
	public String getUnsubscribePrintUrl() {
		return mUnsubscribeUrl;
	}

	/**
     * This method is not yet supported, due to lacking implementation server-side
     * and throws an UnsupportedOperationException when called.
	 * @param id Ignored
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public Country setErn(String ern) {
		throw new UnsupportedOperationException("Country does not yet support setErn(String)");
	}

	/**
     * This method is not yet supported, due to lacking implementation server-side
     * and throws an UnsupportedOperationException when called.
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public String getErn() {
		throw new UnsupportedOperationException("Country does not yet support getErn()");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		if (mUnsubscribeUrl == null) {
			if (other.mUnsubscribeUrl != null)
				return false;
		} else if (!mUnsubscribeUrl.equals(other.mUnsubscribeUrl))
			return false;
		return true;
	}
	
}