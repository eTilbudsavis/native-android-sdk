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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.EtaObjects.helper.Branding;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;


/**
 * <p>This class is a representation of a store as the API v2 exposes it</p>
 * 
 * <p>More documentation available on via our
 * <a href="http://engineering.etilbudsavis.dk/eta-api/pages/references/stores.html">Store Reference</a>
 * documentation, on the engineering blog.
 * </p>
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class Store extends ErnObject<Store> implements EtaObject<JSONObject>, Serializable {
	
	private static final long serialVersionUID = 4105775934027363052L;

	public static final String TAG = Store.class.getSimpleName();

	private static final String ERN_CLASS = "store";
	
	private String mStreet;
	private String mCity;
	private String mZipcode;
	private Country mCountry;
	private double mLatitude = 0.0;
	private double mLongitude = 0.0;
	private String mDealerUrl;
	private String mDealerId;
	private Branding mBranding;
	private String mContact;
	
	private Dealer mDealer;
	
	public static ArrayList<Store> fromJSON(JSONArray stores) {
		ArrayList<Store> list = new ArrayList<Store>();
		try {
			for (int i = 0 ; i < stores.length() ; i++ )
				list.add(Store.fromJSON((JSONObject)stores.get(i)));
			
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return list;
	}
	
	public static Store fromJSON(JSONObject store) {
		Store s = new Store();
		if (store == null) {
			return s;
		}
		
		try {
			s.setId(Json.valueOf(store, JsonKey.ID));
			s.setErn(Json.valueOf(store, JsonKey.ERN));
			s.setStreet(Json.valueOf(store, JsonKey.STREET));
			s.setCity(Json.valueOf(store, JsonKey.CITY));
			s.setZipcode(Json.valueOf(store, JsonKey.ZIP_CODE));
			s.setCountry(Country.fromJSON(store.getJSONObject(JsonKey.COUNTRY)));
			s.setLatitude(Json.valueOf(store, JsonKey.LATITUDE, 0.0d));
			s.setLongitude(Json.valueOf(store, JsonKey.LONGITUDE, 0.0d));
			s.setDealerUrl(Json.valueOf(store, JsonKey.DEALER_URL));
			s.setDealerId(Json.valueOf(store, JsonKey.DEALER_ID));
			s.setBranding(Branding.fromJSON(store.getJSONObject(JsonKey.BRANDING)));
			s.setContact(Json.valueOf(store, JsonKey.CONTACT));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return s;
	}
	
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(JsonKey.STREET, Json.nullCheck(getStreet()));
			o.put(JsonKey.CITY, Json.nullCheck(getCity()));
			o.put(JsonKey.ZIP_CODE, Json.nullCheck(getZipcode()));
			o.put(JsonKey.COUNTRY, Json.nullCheck(getCountry().toJSON()));
			o.put(JsonKey.LATITUDE, getLatitude());
			o.put(JsonKey.LONGITUDE, getLongitude());
			o.put(JsonKey.DEALER_URL, Json.nullCheck(getDealerUrl()));
			o.put(JsonKey.DEALER_ID, Json.nullCheck(getDealerId()));
			o.put(JsonKey.BRANDING, Json.nullCheck(getBranding().toJSON()));
			o.put(JsonKey.CONTACT, Json.nullCheck(getContact()));
		} catch (JSONException e) {
			EtaLog.e(TAG, "", e);
		}
		return o;
	}
	@Override
	String getErnClass() {
		return ERN_CLASS;
	}
	
	public Store setStreet(String street) {
		mStreet = street;
		return this;
	}

	public String getStreet() {
		return mStreet;
	}

	public Store setCity(String city) {
		mCity = city;
		return this;
	}

	public String getCity() {
		return mCity;
	}

	public Store setZipcode(String zipcode) {
		mZipcode = zipcode;
		return this;
	}

	public String getZipcode() {
		return mZipcode;
	}

	public Store setCountry(Country country) {
		mCountry = country;
		return this;
	}

	public Country getCountry() {
		return mCountry;
	}

	public Store setLatitude(Double latitude) {
		mLatitude = latitude;
		return this;
	}

	public Double getLatitude() {
		return mLatitude;
	}

	public Store setLongitude(Double longitude) {
		mLongitude = longitude;
		return this;
	}

	public Double getLongitude() {
		return mLongitude;
	}

	public String getDealerUrl() {
		return mDealerUrl;
	}

	public Store setDealerUrl(String url) {
		mDealerUrl = url;
		return this;
	}

	public Store setDealerId(String dealer) {
		mDealerId = dealer;
		return this;
	}

	public String getDealerId() {
		return mDealerId;
	}

	public Branding getBranding() {
		return mBranding;
	}

	public Store setBranding(Branding branding) {
		mBranding = branding;
		return this;
	}

	public Store setContact(String contact) {
		mContact = contact;
		return this;
	}
	
	public String getContact() {
		return mContact;
	}

	public Store setDealer(Dealer d) {
		mDealer = d;
		return this;
	}
	
	public Dealer getDealer() {
		return mDealer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mBranding == null) ? 0 : mBranding.hashCode());
		result = prime * result + ((mCity == null) ? 0 : mCity.hashCode());
		result = prime * result
				+ ((mContact == null) ? 0 : mContact.hashCode());
		result = prime * result
				+ ((mCountry == null) ? 0 : mCountry.hashCode());
		result = prime * result
				+ ((mDealerId == null) ? 0 : mDealerId.hashCode());
		result = prime * result
				+ ((mDealerUrl == null) ? 0 : mDealerUrl.hashCode());
		long temp;
		temp = Double.doubleToLongBits(mLatitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mLongitude);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((mStreet == null) ? 0 : mStreet.hashCode());
		result = prime * result
				+ ((mZipcode == null) ? 0 : mZipcode.hashCode());
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
		Store other = (Store) obj;
		if (mBranding == null) {
			if (other.mBranding != null)
				return false;
		} else if (!mBranding.equals(other.mBranding))
			return false;
		if (mCity == null) {
			if (other.mCity != null)
				return false;
		} else if (!mCity.equals(other.mCity))
			return false;
		if (mContact == null) {
			if (other.mContact != null)
				return false;
		} else if (!mContact.equals(other.mContact))
			return false;
		if (mCountry == null) {
			if (other.mCountry != null)
				return false;
		} else if (!mCountry.equals(other.mCountry))
			return false;
		if (mDealerId == null) {
			if (other.mDealerId != null)
				return false;
		} else if (!mDealerId.equals(other.mDealerId))
			return false;
		if (mDealerUrl == null) {
			if (other.mDealerUrl != null)
				return false;
		} else if (!mDealerUrl.equals(other.mDealerUrl))
			return false;
		if (Double.doubleToLongBits(mLatitude) != Double
				.doubleToLongBits(other.mLatitude))
			return false;
		if (Double.doubleToLongBits(mLongitude) != Double
				.doubleToLongBits(other.mLongitude))
			return false;
		if (mStreet == null) {
			if (other.mStreet != null)
				return false;
		} else if (!mStreet.equals(other.mStreet))
			return false;
		if (mZipcode == null) {
			if (other.mZipcode != null)
				return false;
		} else if (!mZipcode.equals(other.mZipcode))
			return false;
		return true;
	}
	
}
