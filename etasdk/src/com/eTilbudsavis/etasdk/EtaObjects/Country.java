/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
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
	 * Convert a {@link JSONArray} into a {@link List}&lt;T&gt;.
	 * @param countries A {@link JSONArray} in the format of a valid API v2 country response
	 * @return A {@link List}&lt;T&gt;
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
	 * A factory method for converting {@link JSONObject} into a POJO.
	 * @param country A {@link JSONObject} in the format of a valid API v2 country response
	 * @return A Country object
	 */
	public static Country fromJSON(JSONObject country) {
		return fromJSON(new Country(), country);
	}
	
	/**
	 * A factory method for converting {@link JSONObject} into POJO.
	 * <p>This method exposes a way, of updating/setting an objects properties</p>
	 * @param country An object to set/update
	 * @param jCountry A {@link JSONObject} in the format of a valid API v2 country response
	 * @return A {@link List} of POJO
	 */
	public static Country fromJSON(Country country, JSONObject jCountry) {
		if (country == null) country = new Country();
		if (jCountry == null) return country;
		
		country.setId(Json.valueOf(jCountry, ServerKey.ID));
		country.setUnsubscribePrintUrl(Json.valueOf(jCountry, ServerKey.UNSUBSCRIBE_PRINT_URL));
		
		return country;
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject o = super.toJSON();
		try {
			// API haven't implemented ERN for Country yet, so we'll remove it for now
			if (o.has(ServerKey.ERN)) {
				o.remove(ServerKey.ERN);
			}
			
			o.put(ServerKey.UNSUBSCRIBE_PRINT_URL, Json.nullCheck(getUnsubscribePrintUrl()));
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
