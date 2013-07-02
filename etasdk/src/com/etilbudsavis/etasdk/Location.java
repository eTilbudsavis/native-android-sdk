/**
 * @fileoverview	Location.
 * @author			Danny Hvam <danny@etilbudsavid.dk>
 * @version			0.0.1
 */
package com.etilbudsavis.etasdk;

import java.io.Serializable;
import java.util.LinkedHashMap;

import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;

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

	public Integer getDistance() {
		return mDistance;		
	}
	
	private void updatePageflipLocation() {
		for (Pageflip p : mEat.pageflipList) {
			try {
				p.updateLocation();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * A bundle ready for use in the API-calls
	 * 
	 * <p>This is a method for ease of use in the
	 * API calls to the ETA server.</p>
	 * @return Bundle with API paramaters
	 */
	public Bundle getLocationAsApiParams() {
		Bundle apiParams = new Bundle();
		apiParams.putDouble("api_latitude", mLatitude);
		apiParams.putDouble("api_longitude", mLongitude);
		apiParams.putInt("api_locationDetermined", mLocationDetermined);
		apiParams.putInt("api_geocoded", mGeocoded);
		apiParams.putInt("api_accuracy", mAccuracy);
		apiParams.putInt("api_distance", mDistance);
		return apiParams;
	}
	
	/**
	 * Method returns a bundle containing the location variables.
	 * 
	 * <p>Instead please use {@link #getLocationAsApiParams()} for API parameters<br>
	 * and use the provided getters for getting variables from this object.</p>
	 * 
	 * @return Bundle with location variables
	 */
	@Deprecated
	public Bundle getLocation() {
		return getLocationAsApiParams();
	}
	
	public LinkedHashMap<String, Object> getPageflipLocation() {
		LinkedHashMap<String, Object> etaloc = new LinkedHashMap<String, Object>();
		etaloc.put("latitude", mLatitude);
		etaloc.put("longitude", mLongitude);
		etaloc.put("distance", mUseDistance ? mDistance : "0" );
		etaloc.put("locationDetermined", mLocationDetermined);
		etaloc.put("geocoded", mGeocoded);
		if (mGeocoded == 0) 
			etaloc.put("accuracy", mAccuracy);
		
		return etaloc;
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

	public double getLatitude() {
		return mLatitude;
	}

	public Location setLatitude(double latitude) {
		this.mLatitude = latitude;
		return this;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public Location setLongitude(double longitude) {
		this.mLongitude = longitude;
		return this;
	}


	public int getGeocoded() {
		return mGeocoded;
	}

	/**
	 * Geocoded os a value for whether the location is found by sensor.<br>
	 * Found by sensor, then geocoded must be 0, and then an accuracy must also be provided. <br>
	 * Found by geocoding an address, then geocoded must be 1, and accuracy can be null.
	 * @param geocoded Sensor or geocoded address
	 * @param accuracy in meters<li>must be set if geocoded is 0 else it can be null
	 * @return this location
	 */
	public Location setGeocoded(int geocoded, int accuracy) {
		mGeocoded = geocoded < 0 ? 0 : (geocoded > 1 ? 1 : geocoded ) ;
		mAccuracy = mGeocoded == 0 ? accuracy : 0;
		return this;
	}


	/**
	 * 
	 * @param accuracy must be set if geocoded == 0
	 * @param geocoded if set to 0, you need to set an accuracy
	 * @return
	 */
	public Location setAccuracy(int accuracy) {
		mAccuracy = accuracy > 0 ?  accuracy : 0;
		return this;
	}


	public int getAccuracy() {
		return mAccuracy;
	}

	public int getLocationDetermined() {
		return mLocationDetermined;
	}


	public Location setLocationDetermined(int locationDetermined) {
		this.mLocationDetermined = locationDetermined;
		return this;
	}


	public double getBoundsNorth() {
		return mBoundsNorth;
	}


	public Location setmBoundsNorth(double boundsNorth) {
		this.mBoundsNorth = boundsNorth;
		return this;
	}


	public double getBoundsEast() {
		return mBoundsEast;
	}


	public Location setBoundsEast(double boundsEast) {
		this.mBoundsEast = boundsEast;
		return this;
	}


	public double getBoundsSouth() {
		return mBoundsSouth;
	}


	public Location setBoundsSouth(double boundsSouth) {
		this.mBoundsSouth = boundsSouth;
		return this;
	}


	public double getBoundsWest() {
		return mBoundsWest;
	}


	public Location setBoundsWest(double boundsWest) {
		this.mBoundsWest = boundsWest;
		return this;
	}
	
	

}