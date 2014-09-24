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

import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;

public class Country extends ErnObject<Country> implements EtaObject<JSONObject>, Serializable {
	
	public static final String TAG = Country.class.getSimpleName();

	private static final String ERN_CLASS = "country";
	
	private static final long serialVersionUID = 1L;
	
	private String mUnsubscribeUrl;
	
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
			EtaLog.e(TAG, "", e);
		}
		return list;
	}
	
	/**
	 * A factory method for converting {@link JSONObject} into a POJO.
	 * @param country A {@link JSONObject} in the format of a valid API v2 country response
	 * @return A Country object
	 */
	public static Country fromJSON(JSONObject jCountry) {
		Country country = new Country();
		if (jCountry == null) {
			return country;
		}
		
		country.setId(Json.valueOf(jCountry, JsonKey.ID));
		country.setUnsubscribePrintUrl(Json.valueOf(jCountry, JsonKey.UNSUBSCRIBE_PRINT_URL));
		
		return country;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.ID, Json.nullCheck(getId()));
			o.put(JsonKey.ERN, Json.nullCheck(getErn()));
			o.put(JsonKey.UNSUBSCRIBE_PRINT_URL, Json.nullCheck(getUnsubscribePrintUrl()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}

	@Override
	String getErnClass() {
		return ERN_CLASS;
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
			EtaLog.i(TAG, "The country code: " + id + " isn't allowed, see documentation for more details");
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
