package com.etilbudsavis.etasdk;

import java.io.Serializable;

import android.os.Bundle;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;

	// ETA object
	ETA mEat;
	
	// Settings
	private Boolean mUseLocation = true;
	private Boolean mUseDistance = true;

	
	// Location
	private int mDistance = 700000;
	private double mLatitude = 0;
	private double mLongitude = 0;
	private int mGeocoded = 0;
	private int mAccuracy = 0;
	private int mLocationDetermined = 0;

	// Bounds
	private double mBoundsNorth = 0;
	private double mBoundsEast = 0;
	private double mBoundsSouth = 0;
	private double mBoundsWest = 0;

	public Location(ETA eta) {
		mEat = eta;
	}

	//Exclude location in request?
	public void excludeLocaton() {
		mUseLocation = false;
	}
	
	public boolean useLocation() {
		return mUseLocation;
	}
	
	//Exclude distance in request?
	public void excludeDistance() {
		mUseDistance = false;
	}
	
	public boolean useDistance() {
		return mUseDistance;
	}
	
	public Boolean isLocationSet() {
		return mLatitude != 0 && mLongitude != 0;
	}

	public Boolean isBoundsSet() {
		return mBoundsNorth != 0 && mBoundsSouth != 0;
	}
	
	/**
	 * Sets (or updates) the ETA.location objects' location 
	 * this does NOT update the location in the pageflip.
	 * To do this, you need to call updateLocation() in pageflip.
	 * @param latitude
	 * @param longitude
	 * @param geocoded
	 * @param accuracy
	 * @param locationDetermined
	 * @param distance
	 */
	public void setLocation(double latitude, double longitude, int geocoded,
			int accuracy, int locationDetermined, int distance) {
		mLatitude = latitude;
		mLongitude = longitude;
		mLocationDetermined = locationDetermined;
		mGeocoded = geocoded;
		if (geocoded == 0)
			mAccuracy = accuracy;
		
		if (distance >= 0 && distance <= 700000)
			mDistance = distance;
		
		for (Pageflip p : mEat.pageflipList) {
			p.updateLocation();
		}
		
		
	}

	public Bundle getLocation() {
		Bundle object = new Bundle();
		object.putDouble("api_latitude", mLatitude);
		object.putDouble("api_longitude", mLongitude);
		object.putInt("api_locationDetermined", mLocationDetermined);
		object.putInt("api_geocoded", mGeocoded);
		object.putInt("api_accuracy", mAccuracy);
		object.putInt("api_distance", mDistance);
		return object;
	}

	public void setBounds(double boundsNorth, double boundsEast,
			double boundsSouth, double boundsWest) {
		mBoundsEast = boundsEast;
		mBoundsNorth = boundsNorth;
		mBoundsSouth = boundsSouth;
		mBoundsWest = boundsWest;
	}

	public Bundle getBounds() {
		Bundle object = new Bundle();
		object.putDouble("api_boundsNorth", mBoundsNorth);
		object.putDouble("api_boundsEast", mBoundsEast);
		object.putDouble("api_boundsSouth", mBoundsSouth);
		object.putDouble("api_boundsWest", mBoundsWest);
		return object;
	}
}
