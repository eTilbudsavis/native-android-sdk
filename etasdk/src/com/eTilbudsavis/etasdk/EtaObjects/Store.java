package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.Utils.EtaLog;


public class Store extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Store";
	
	/** Sort a list by distance in ascending order. (smallest to largest) */
	public static final String SORT_DISTANCE = Request.Sort.DISTANCE;

	/** Sort a list by distance in descending order. (largest to smallest)*/
	public static final String SORT_DISTANCE_DESC = Request.Sort.DISTANCE_DESC;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED = Request.Sort.CREATED;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED_DESC = Request.Sort.CREATED_DESC;

	/** Parameter for getting a list of specific store id's */
	public static final String FILTER_STORE_IDS = Request.Param.FILTER_STORE_IDS;

	/** Endpoint for store list resource */
	public static final String ENDPOINT_LIST = Request.Endpoint.STORE_LIST;

	/** Endpoint for a single store resource */
	public static final String ENDPOINT_ID = Request.Endpoint.STORE_ID;

	/** Endpoint for searching stores */
	public static final String ENDPOINT_SEARCH = Request.Endpoint.STORE_SEARCH;

	/** Endpoint for fast searching stores */
	public static final String ENDPOINT_QUICK_SEARCH = Request.Endpoint.STORE_QUICK_SEARCH;

	private String mId;
	private String mErn;
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

	public Store() { }
	
	public static ArrayList<Store> fromJSON(JSONArray stores) {
		ArrayList<Store> list = new ArrayList<Store>();
		try {
			for (int i = 0 ; i < stores.length() ; i++ )
				list.add(Store.fromJSON((JSONObject)stores.get(i)));
			
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return list;
	}
	
	public static Store fromJSON(JSONObject store) {
		return fromJSON(new Store(), store);
	}
	
	private static Store fromJSON(Store s, JSONObject store) {
		if (s == null) s = new Store();
		if (store == null) return s;
		
		try {
			s.setId(getJsonString(store, ServerKey.ID));
			s.setErn(getJsonString(store, ServerKey.ERN));
			s.setStreet(getJsonString(store, ServerKey.STREET));
			s.setCity(getJsonString(store, ServerKey.CITY));
			s.setZipcode(getJsonString(store, ServerKey.ZIP_CODE));
			s.setCountry(Country.fromJSON(store.getJSONObject(ServerKey.COUNTRY)));
			s.setLatitude(store.getDouble(ServerKey.LATITUDE));
			s.setLongitude(store.getDouble(ServerKey.LONGITUDE));
			s.setDealerUrl(getJsonString(store, ServerKey.DEALER_URL));
			s.setDealerId(getJsonString(store, ServerKey.DEALER_ID));
			s.setBranding(Branding.fromJSON(store.getJSONObject(ServerKey.BRANDING)));
			s.setContact(getJsonString(store, ServerKey.CONTACT));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return s;
	}

	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Store s) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.ID, s.getId());
			o.put(ServerKey.ERN, s.getErn());
			o.put(ServerKey.STREET, s.getStreet());
			o.put(ServerKey.CITY, s.getCity());
			o.put(ServerKey.ZIP_CODE, s.getZipcode());
			o.put(ServerKey.COUNTRY, s.getCountry().toJSON());
			o.put(ServerKey.LATITUDE, s.getLatitude());
			o.put(ServerKey.LONGITUDE, s.getLongitude());
			o.put(ServerKey.DEALER_URL, s.getDealerUrl());
			o.put(ServerKey.DEALER_ID, s.getDealerId());
			o.put(ServerKey.BRANDING, s.getBranding().toJSON());
			o.put(ServerKey.CONTACT, s.getContact());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}

	public Store setId(String id) {
		this.mId = id;
		return this;
	}

	public String getId() {
		return mId;
	}
	
	public Store setErn(String ern) {
		mErn = ern;
		return this;
	}
	
	public String getErn() {
		return mErn;
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
		result = prime * result + ((mErn == null) ? 0 : mErn.hashCode());
		result = prime * result + ((mId == null) ? 0 : mId.hashCode());
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
		if (mErn == null) {
			if (other.mErn != null)
				return false;
		} else if (!mErn.equals(other.mErn))
			return false;
		if (mId == null) {
			if (other.mId != null)
				return false;
		} else if (!mId.equals(other.mId))
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
