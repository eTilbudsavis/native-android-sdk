/**
 * @fileoverview	Location.
 * @author			Danny Hvam <danny@etilbudsavid.dk>
 * @version			0.0.1
 */
package com.etilbudsavis.etasdk;

import java.io.Serializable;

import android.os.Bundle;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ETA mEat;
	
	// Settings.
	private Boolean mUseLocation = true;
	private Boolean mUseDistance = true;
	
	// Location.
	private int mDistance = 700000;
	private double mLatitude = 0;
	private double mLongitude = 0;
	private int mGeocoded = 0;
	private int mAccuracy = 0;
	private int mLocationDetermined = 0;

	// Bounds.
	private double mBoundsNorth = 0;
	private double mBoundsEast = 0;
	private double mBoundsSouth = 0;
	private double mBoundsWest = 0;

	// Constructor.
	public Location(ETA eta) {
		mEat = eta;
	}

	
	/**
	 * Set whether or not to use location in ETA API calls
	 * @param value a boolean value
	 */
	public void useLocation(boolean value) {
		mUseLocation = value;
	}
	
	/**
	 * Returns the current setting for usage of location
	 * @return a boolean value
	 */
	public boolean useLocation() {
		return mUseLocation;
	}
	
	/**
	 * Set whether or not to use distance in ETA API calls
	 * @param value a boolean value
	 */
	public void useDistance(boolean value) {
		mUseDistance = value;
	}
	
	/**
	 * Returns the current setting for usage of distance
	 * @return a boolean value
	 */
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
	 *
	 * @param latitude
	 * @param longitude
	 * @param geocoded
	 * @param accuracy
	 * @param locationDetermined
	 * @param distance
	 */
	public void setLocation(double latitude, double longitude, int geocoded,
			int accuracy, int locationDetermined) {
		mLatitude = latitude;
		mLongitude = longitude;
		mLocationDetermined = locationDetermined;
		mGeocoded = geocoded;

		if (geocoded == 0)
			mAccuracy = accuracy;
		
		updatePageflipLocation();
		
	}
	
	public void setDistance(int distance) {
		
		if (distance >= 0 && distance <= 700000)
			mDistance = distance;
		
	}
	
	private void updatePageflipLocation() {
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