package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import Utils.Endpoint;
import Utils.Params;
import Utils.Sort;

public class Store implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "Store";
	
	/** Sort a list by distance in ascending order. (smallest to largest) */
	public static final String SORT_DISTANCE = Sort.DISTANCE;

	/** Sort a list by distance in descending order. (largest to smallest)*/
	public static final String SORT_DISTANCE_DESC = Sort.DISTANCE_DESC;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED = Sort.CREATED;

	/** Sort a list by created in ascending order. (smallest to largest) */
	public static final String SORT_CREATED_DESC = Sort.CREATED_DESC;

	/** Parameter for getting a list of specific store id's */
	public static final String FILTER_STORE_IDS = Params.FILTER_STORE_IDS;

	/** Endpoint for store list resource */
	public static final String ENDPOINT_LIST = Endpoint.STORE_LIST;

	/** Endpoint for a single store resource */
	public static final String ENDPOINT_ID = Endpoint.STORE_ID;

	/** Endpoint for searching stores */
	public static final String ENDPOINT_SEARCH = Endpoint.STORE_SEARCH;

	/** Endpoint for fast searching stores */
	public static final String ENDPOINT_QUICK_SEARCH = Endpoint.STORE_QUICK_SEARCH;

	private String mId;
	private String mErn;
	private String mStreet;
	private String mCity;
	private String mZipcode;
	private Country mCountry;
	private double mLatitude;
	private double mLongitude;
	private String mDealerUrl;
	private String mDealerId;
	private Branding mBranding;
	private String mContact;
	
	private Dealer mDealer;

	public Store(JSONObject store) {
		update(store);
	}

	public void update(JSONObject store) {

		try {
			mId = store.getString("id");
			mErn = store.getString("ern");
			mStreet = store.getString("street");
			mCity = store.getString("city");
			mZipcode = store.getString("zip_code");
			mCountry = new Country(store.getJSONObject("country"));
			mLatitude = store.getDouble("latitude");
			mLongitude = store.getDouble("longitude");
			mDealerUrl = store.getString("dealer_url");
			mDealerId = store.getString("dealer_id");
			mBranding = new Branding(store.getJSONObject("branding"));
			mContact = store.getString("contact");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public Store setId(String id) {
		mId = id;
		return this;
	}

	public String getId() {
		return mId;
	}

	public String getErn() {
		return mErn;
	}

	public Store setErn(String ern) {
		mErn = ern;
		return this;
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Store))
			return false;

		Store s = (Store)o;
		return mId.equals(s.getId()) &&
				mErn.equals(s.getErn()) &&
				mStreet.equals(s.getStreet()) &&
				mCity.equals(s.getCity()) &&
				mZipcode.equals(s.getZipcode()) &&
				mCountry == null ? s.getCountry() == null : mCountry.equals(s.getCountry()) &&
				mLatitude == s.getLatitude() &&
				mLongitude == s.getLongitude() &&
				mDealerUrl.equals(s.getDealerUrl()) &&
				mDealerId.equals(s.getDealerId()) &&
				mBranding == null ? s.getBranding() == null : mBranding.equals(s.getBranding()) &&
				mContact.equals(s.getContact());
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[")
		.append("branding=").append(mBranding.toString(everything))
		.append(", id=").append(mId)
		.append(", street=").append(mStreet)
		.append(", city=").append(mCity);
		
		if (everything) {
			sb.append(", zipcode=").append(mZipcode)
			.append(", country=").append(mCountry.toString(everything))
			.append(", latitude=").append(mLatitude)
			.append(", longitude=").append(mLongitude)
			.append(", dealer=").append(mDealer == null ? mDealerId : mDealer.toString(everything))
			.append(", contact=").append(mContact);
		}
		return sb.append("]").toString();
	}
	
}
