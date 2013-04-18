/**
 * @fileoverview	Location.
 * @author			Danny Hvam <danny@eTilbudsavis.dk>
 */
package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.util.LinkedHashMap;

import android.os.Bundle;

public class Location implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean mUseDistance = true;
	// Should there still be options to use distance and location?
	
	// Location.
	private int mRadius = -1;
	private double mLatitude = 0;
	private double mLongitude = 0;

	// Bounds.
	private double mBoundsNorth = 0;
	private double mBoundsEast = 0;
	private double mBoundsSouth = 0;
	private double mBoundsWest = 0;

	// Constructor.
	public Location() {
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
		return mRadius != -1;
	}

	public Boolean isBoundsSet() {
		return mBoundsNorth != 0 && mBoundsSouth != 0 && 
				mBoundsEast != 0 && mBoundsWest != 0;
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
	public void setLocation(double latitude, double longitude) {
		mLatitude = latitude;
		mLongitude = longitude;
	}

	/**
	 * Set the current search radius.
	 * @param radius in meters <li> Min value = 0 <li> Max value = 700000
	 * @return this Object, for easy chaining of set methods.
	 */
	public Location setRadius(int radius) {
		if (radius >= 0 && radius <= 700000)
			mRadius = radius;
		return this;
	}

	/**
	 * Get current radius
	 * @return radius in meters.
	 */
	public Integer getRadius() {
		return mRadius;		
	}

	/**
	 * Set latitude to use in search.
	 * @param latitude
	 * @return this Object, for easy chaining of set methods.
	 */
	public Location setLatitude(double latitude) {
		this.mLatitude = latitude;
		return this;
	}
	
	/**
	 * Get the current latitude.
	 * @return latitude
	 */
	public double getLatitude() {
		return mLatitude;
	}

	/**
	 * Set longitude to use in search.
	 * @param longitude
	 * @return this Object, for easy chaining of set methods.
	 */
	public Location setLongitude(double longitude) {
		this.mLongitude = longitude;
		return this;
	}

	/**
	 * Get the current longitude.
	 * @return longitude
	 */
	public double getLongitude() {
		return mLongitude;
	}

	/**
	 * A bundle ready for use in the API-calls
	 * 
	 * <p>This is a method for ease of use in the
	 * API calls to the ETA server.</p>
	 * @return Bundle with API parameters
	 */
	public Bundle getApiParams() {
		Bundle apiParams = new Bundle();
		apiParams.putDouble(Api.LATITUDE, mLatitude);
		apiParams.putDouble(Api.LONGITUDE, mLongitude);
		apiParams.putInt(Api.RADIUS, mRadius);
		
		if (isBoundsSet()) {
			apiParams.putAll(getApiBounds());
		}
		
		return apiParams;
	}
	
	public LinkedHashMap<String, Object> getPageflipLocation() {
		LinkedHashMap<String, Object> etaloc = new LinkedHashMap<String, Object>();
		etaloc.put(Api.LATITUDE, mLatitude);
		etaloc.put(Api.LONGITUDE, mLongitude);
		etaloc.put(Api.RADIUS, mUseDistance ? mRadius : "0" );
		return etaloc;
	}

	public Bundle getApiBounds() {
		Bundle object = new Bundle();
		object.putDouble(Api.BOUND_NORTH, mBoundsNorth);
		object.putDouble(Api.BOUND_EAST, mBoundsEast);
		object.putDouble(Api.BOUND_SOUTH, mBoundsSouth);
		object.putDouble(Api.BOUND_WEST, mBoundsWest);
		return object;
	}

	/**
	 * Set the bounds for your search.
	 * All parameters should be GPS coordinates.
	 * @param boundsNorth 
	 * @param boundsEast
	 * @param boundsSouth
	 * @param boundsWest
	 */
	public void setBounds(double boundNorth, double boundEast,
			double boundSouth, double boundWest) {
		mBoundsEast = boundEast;
		mBoundsNorth = boundNorth;
		mBoundsSouth = boundSouth;
		mBoundsWest = boundWest;
	}
	
	/**
	 * GPS coordinate for the northern bound of a search.
	 * @param boundsNorth
	 * @return this Object, for easy chaining of set methods.
	 */
	public Location setBoundNorth(double boundNorth) {
		this.mBoundsNorth = boundNorth;
		return this;
	}

	/**
	 * GPS coordinate for the eastern bound of a search.
	 * @param boundEast
	 * @return this Object, for easy chaining of set methods.
	 */
	public Location setBoundEast(double boundEast) {
		this.mBoundsEast = boundEast;
		return this;
	}

	/**
	 * GPS coordinate for the southern bound of a search.
	 * @param boundSouth
	 * @return this Object, for easy chaining of set methods.
	 */
	public Location setBoundsSouth(double boundSouth) {
		this.mBoundsSouth = boundSouth;
		return this;
	}

	/**
	 * GPS coordinate for the western bound of a search.
	 * @param boundWest
	 * @return this Object, for easy chaining of set methods.
	 */
	public Location setBoundsWest(double boundWest) {
		this.mBoundsWest = boundWest;
		return this;
	}
	
	public double getBoundEast() {
		return mBoundsEast;
	}
	
	public double getBoundNorth() {
		return mBoundsNorth;
	}
	
	public double getBoundSouth() {
		return mBoundsSouth;
	}
	
	public double getBoundWest() {
		return mBoundsWest;
	}
	
}