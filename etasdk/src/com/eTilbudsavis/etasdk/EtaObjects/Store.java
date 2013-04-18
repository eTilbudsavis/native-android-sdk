package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Store implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String mId;
	public String mStreet;
	public String mCity;
	public String mZipcode;
	public Country mCountry;
	public String mUrl;
	public double mLatitude;
	public double mLongitude;
	public Dealer mDealer;
	public int mDistance;
	public String mContact;


	public Store(JSONObject store) {
		update(store);
	}

	public void update(JSONObject store) {

		try {
			mId = store.getString("id");
			mStreet = store.getString("street");
			mCity = store.getString("city");
			mZipcode = store.getString("zipcode");
			mCountry = new Country(store.getJSONObject("country"));
			mUrl = store.getString("url");
			mLatitude = store.getDouble("latitude");
			mLongitude = store.getDouble("longitude");
			mDealer = new Dealer(store.getJSONObject("dealer"));
			mDistance = store.getInt("distance");
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

	public Store setUrl(String url) {
		mUrl = url;
		return this;
	}

	public String getUrl() {
		return mUrl;
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

	public Store setDealer(Dealer dealer) {
		mDealer = dealer;
		return this;
	}

	public Dealer getDealer() {
		return mDealer;
	}

	public Store setDistance(int distance) {
		mDistance = distance;
		return this;
	}

	public int getDistance() {
		return mDistance;
	}

	public Store setContact(String contact) {
		mContact = contact;
		return this;
	}
	
	public String getContact() {
		return mContact;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof Store))
			return false;

		Store s = (Store)o;
		return mId.equals(s.getId()) &&
				mStreet.equals(s.getStreet()) &&
				mCity.equals(s.getCity()) &&
				mZipcode.equals(s.getZipcode()) &&
				mCountry.equals(s.getCountry()) &&
				mUrl.equals(s.getUrl()) &&
				mLatitude == s.getLatitude() &&
				mLongitude == s.getLongitude() &&
				mDealer.equals(s.getDealer()) &&
				mDistance == s.getDistance() &&
				mContact.equals(s.getContact());
	}
}
