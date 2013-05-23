/**
 * @fileoverview	Location.
 * @author			Danny Hvam <danny@eTilbudsavis.dk>
 */
package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import Utils.Utilities;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

public class EtaLocation extends Location {

	private static final String ETA_PROVIDER	= "etasdk";
	
	/** API v2 parameter name for latitude. */
	public static final String SENSOR = "r_sensor";
	
	/** API v2 parameter name for latitude. */
	public static final String LATITUDE = "r_lat";
	
	/** API v2 parameter name for longitude. */
	public static final String LONGITUDE = "r_lng";
	
	/** API v2 parameter name for radius. */
	public static final String RADIUS = "r_radius";
	
	/** API v2 parameter name for bounds east. */
	public static final String BOUND_EAST = "b_east";
	
	/** API v2 parameter name for bounds north. */
	public static final String BOUND_NORTH = "b_north";
	
	/** API v2 parameter name for bounds south. */
	public static final String BOUND_SOUTH = "b_south";
	
	/** API v2 parameter name for bounds west. */
	public static final String BOUND_WEST = "b_west";
	
	private static final String TIME = "etasdk_loc_time";
	
	// Location.
	private int mRadius = Integer.MAX_VALUE;
	private boolean mSensor = false;
	private double mBoundNorth = 0f;
	private double mBoundEast = 0f;
	private double mBoundSouth = 0f;
	private double mBoundWest = 0f;
	
	private ArrayList<LocationListener> mSubscribers = new ArrayList<EtaLocation.LocationListener>();

	public EtaLocation() {
		super(ETA_PROVIDER);
	}

	public Boolean isLocationSet() {
		return (mRadius != Integer.MAX_VALUE && getLatitude() != 0.0 && getLongitude() != 0.0);
	}

	public Boolean isBoundsSet() {
		return (mBoundNorth != Integer.MAX_VALUE && mBoundSouth != Integer.MAX_VALUE && 
				mBoundEast != Integer.MAX_VALUE && mBoundWest != Integer.MAX_VALUE);
	}

	@Override
	public void set(Location l) {
		super.set(l);
		mSensor = (getProvider().equals(LocationManager.GPS_PROVIDER) || getProvider().equals(LocationManager.NETWORK_PROVIDER) );
	}
	
	public void set(Location l, int radius, boolean sensor) {
		super.set(l);
		mRadius = radius;
		mSensor = sensor;
	}
	
	public void set(double latitude, double longitude, int radius, boolean sensor) {
		mRadius = radius;
		mSensor = sensor;
		setLatitude(latitude);
		setLongitude(longitude);
		setTime(System.currentTimeMillis());
		setProvider(ETA_PROVIDER);
	}

	/**
	 * Set the current search radius.
	 * @param radius in meters <li> Min value = 0 <li> Max value = 700000
	 * @return this Object, for easy chaining of set methods.
	 */
	public EtaLocation setRadius(int radius) {
		if (radius < 0)
			mRadius = 0;
		else if (radius > 700000)
			mRadius = 700000;
		else
			mRadius = radius;
		return this;
	}

	/**
	 * Get current radius
	 * @return radius in meters.
	 */
	public int getRadius() {
		return mRadius;
	}

	public EtaLocation setSensor(boolean sensor) {
		mSensor = sensor;
		return this;
	}
	
	public boolean getSensor() {
		return mSensor;
	}

	public LinkedHashMap<String, Object> getPageflipLocation() {
		LinkedHashMap<String, Object> etaloc = new LinkedHashMap<String, Object>();
		etaloc.put(LATITUDE, getLatitude());
		etaloc.put(LONGITUDE, getLongitude());
		etaloc.put(RADIUS, mRadius);
		return etaloc;
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
		setBoundEast(boundEast);
		setBoundNorth(boundNorth);
		setBoundSouth(boundSouth);
		setBoundWest(boundWest);
	}
	
	/**
	 * GPS coordinate for the northern bound of a search.
	 * @param boundsNorth
	 */
	public EtaLocation setBoundNorth(double boundNorth) {
		mBoundNorth = boundNorth;
		return this;
	}

	/**
	 * GPS coordinate for the eastern bound of a search.
	 * @param boundEast
	 */
	public EtaLocation setBoundEast(double boundEast) {
		mBoundEast = boundEast;
		return this;
	}

	/**
	 * GPS coordinate for the southern bound of a search.
	 * @param boundSouth
	 */
	public EtaLocation setBoundSouth(double boundSouth) {
		mBoundSouth = boundSouth;
		return this;
	}

	/**
	 * GPS coordinate for the western bound of a search.
	 * @param boundWest
	 */
	public EtaLocation setBoundWest(double boundWest) {
		mBoundWest = boundWest;
		return this;
	}
	
	public double getBoundEast() {
		return mBoundEast;
	}
	
	public double getBoundNorth() {
		return mBoundNorth;
	}
	
	public double getBoundSouth() {
		return mBoundSouth;
	}
	
	public double getBoundWest() {
		return mBoundWest;
	}

	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean(SENSOR, mSensor);
		savedInstanceState.putInt(RADIUS, mRadius);
		savedInstanceState.putDouble(LATITUDE, getLatitude());
		savedInstanceState.putDouble(LONGITUDE, getLongitude());
		savedInstanceState.putDouble(BOUND_EAST, mBoundEast);
		savedInstanceState.putDouble(BOUND_WEST, mBoundWest);
		savedInstanceState.putDouble(BOUND_NORTH, mBoundNorth);
		savedInstanceState.putDouble(BOUND_SOUTH, mBoundSouth);
		savedInstanceState.putLong(TIME, getTime());

	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		setSensor(savedInstanceState.getBoolean(SENSOR));
		setRadius(savedInstanceState.getInt(RADIUS));
		setLatitude(savedInstanceState.getDouble(LATITUDE));
		setLongitude(savedInstanceState.getDouble(LONGITUDE));
		setBoundEast(savedInstanceState.getDouble(BOUND_EAST));
		setBoundWest(savedInstanceState.getDouble(BOUND_WEST));
		setBoundNorth(savedInstanceState.getDouble(BOUND_NORTH));
		setBoundSouth(savedInstanceState.getDouble(BOUND_SOUTH));
		setTime(savedInstanceState.getLong(TIME));
	}

	public boolean saveToSharedPrefs(Context c) {
		SharedPreferences sp = c.getSharedPreferences(Eta.PREFS_NAME, Context.MODE_PRIVATE);
		return sp.edit()
		.putBoolean(SENSOR, mSensor)
		.putInt(RADIUS, mRadius)
		.putFloat(LATITUDE, (float)getLatitude())
		.putFloat(LONGITUDE, (float)getLongitude())
		.putFloat(BOUND_EAST, (float)mBoundEast)
		.putFloat(BOUND_WEST, (float)mBoundWest)
		.putFloat(BOUND_NORTH, (float)mBoundNorth)
		.putFloat(BOUND_SOUTH, (float)mBoundSouth)
		.putLong(TIME, getTime())
		.commit();
	}
	
	public boolean restoreFromSharedPrefs(Context c) {
		SharedPreferences sp = c.getSharedPreferences(Eta.PREFS_NAME, Context.MODE_PRIVATE);
		if (sp.contains(SENSOR) && sp.contains(RADIUS) && sp.contains(LATITUDE) && 
				sp.contains(LONGITUDE) && sp.contains(BOUND_EAST) && sp.contains(BOUND_WEST) && 
				sp.contains(BOUND_NORTH) && sp.contains(BOUND_SOUTH) && sp.contains(TIME) ) {
			
			setSensor(sp.getBoolean(SENSOR, false));
			setRadius(sp.getInt(RADIUS, Integer.MAX_VALUE));
			setLatitude(sp.getFloat(LATITUDE, 0f));
			setLongitude(sp.getFloat(LONGITUDE, 0f));
			setBoundEast(sp.getFloat(BOUND_EAST, 0f));
			setBoundWest(sp.getFloat(BOUND_WEST, 0f));
			setBoundNorth(sp.getFloat(BOUND_NORTH, 0f));
			setBoundSouth(sp.getFloat(BOUND_SOUTH, 0f));
			setTime(sp.getLong(TIME, System.currentTimeMillis()));
			return true;
		} else {
			return false;
		}
		
	}

	/**
	 * Invoke notifications to subscribers of this location object.<br><br>
	 * This object automatically notifies all subscribers on changes.
	 */
	public void notifySubscribers() {
		for (LocationListener l : mSubscribers) {
			l.onLocationChange();
		}
	}
	
	/**
	 * Subscribe to events and changes in location.
	 * @param listener for callbacks
	 */
	public void subscribe(LocationListener listener) {
		mSubscribers.add(listener);
	}

	/**
	 * Unsubscribe from events and changes in location.
	 * @param listener to remove
	 * @return true if this Collection is modified, false otherwise.
	 */
	public boolean unSubscribe(LocationListener listener) {
		return mSubscribers.remove(listener);
	}
	
	@Override 
	public String toString() {
        return "Location[mProvider=" + getProvider() +
                ",mTime=" + getTime() +
                ",mLatitude=" + getLatitude() +
                ",mLongitude=" + getLongitude() +
                ",mRadius=" + mRadius +
                ",mSensor=" + mSensor + "]";
	}
	
	public interface LocationListener {
		public void onLocationChange();
	}
	
}