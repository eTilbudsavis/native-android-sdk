/**
 * @fileoverview	Location.
 * @author			Danny Hvam <danny@eTilbudsavis.dk>
 */
package com.eTilbudsavis.etasdk;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Param;

public class EtaLocation extends Location {

	public static final String TAG = "EtaLocation";
	
	private static final String ETA_PROVIDER = "etasdk";
	
	private static final int RADIUS_MIN = 0;
	private static final int RADIUS_MAX = 700000;
	private static final int DEFAULT_RADIUS = 100000;
	private static final double DEFAULT_BOUND = 0.0;
	
	private int mRadius = DEFAULT_RADIUS;
	private boolean mSensor = false;
	private String mAddress = null;
	private double mBoundNorth = DEFAULT_BOUND;
	private double mBoundEast = DEFAULT_BOUND;
	private double mBoundSouth = DEFAULT_BOUND;
	private double mBoundWest = DEFAULT_BOUND;
	private Eta mEta;
	private ArrayList<LocationListener> mSubscribers = new ArrayList<LocationListener>();
	
	public EtaLocation(Eta eta) {
		super(ETA_PROVIDER);
		mEta = eta;
		restoreState();
	}
	
	@Override
	public void set(Location l) {
		set(l);
		mSensor = (l.getProvider().equals(LocationManager.GPS_PROVIDER) || l.getProvider().equals(LocationManager.NETWORK_PROVIDER) );
		notifySubscribers();
	}
	
	/**
	 * Set location for an address that has been geocoded to a latitude, longitude format<br /><br />
	 * NOTE: This implicitly implies that, no {@link #setSensor(boolean) sensor} has been used.
	 * https://developers.google.com/maps/documentation/geocoding/ for more info
	 * 
	 * @param address that has been geocoded
	 * @param latitude of the address
	 * @param longitude of the address
	 * @return this object
	 */
	public void set(String address, double latitude, double longitude) {
		mAddress = address;
		super.setLatitude(latitude);
		super.setLongitude(longitude);
		mSensor = false;
		setTimeNow();
		notifySubscribers();
	}
	
	/**
	 * Set the current search radius.
	 * @param radius in meters <li> Min value = 0 <li> Max value = 700000
	 * @return this Object, for easy chaining of set methods.
	 */
	public void setRadius(int radius) {
		if (radius < RADIUS_MIN || radius > RADIUS_MAX) {
			EtaLog.d(TAG, "Radius must be within range " + RADIUS_MIN + " to " + RADIUS_MAX + ", provided radius: " + radius);
			return;
		}
		mRadius = radius;
		setTimeNow();
		notifySubscribers();
	}

	/**
	 * Get current radius
	 * @return radius in meters.
	 */
	public int getRadius() {
		return mRadius;
	}

	@Override
	public void setLatitude(double latitude) {
		super.setLatitude(latitude);
		setTimeNow();
		notifySubscribers();
	}
	
	@Override
	public double getLatitude() {
		return super.getLatitude();
	}
	
	@Override
	public void setLongitude(double longitude) {
		super.setLongitude(longitude);
		setTimeNow();
		notifySubscribers();
	}

	@Override
	public double getLongitude() {
		return super.getLongitude();
	}
	
	@Override
	public long getTime() {
		return super.getTime();
	}
	
	@Override
	public void setTime(long time) {
		super.setTime(time);
		notifySubscribers();
	}
	
	/**
	 * Set whether the location has been set by sensor.<br>
	 * Sensor is automatically set to true if location is set via {@link #set(Location) setLocation(Location l)}
	 * and the given location is from a sensor (network, or GPS).
	 * @param sensor
	 */
	public void setSensor(boolean sensor) {
		mSensor = sensor;
		setTimeNow();
		notifySubscribers();
	}
	
	/**
	 * Determines if the location is set by a sensor
	 * @return true if location have been set by a sensor
	 */
	public boolean isSensor() {
		return mSensor;
	}
	
	/**
	 * Returns an address, of this location if the address have previously been set 
	 * with {@link #set(String, double, double) set(String address, double latitude, double longitude)}.
	 * @return an address if one was given, else null
	 */
	public String getAddress() {
		return mAddress;
	}
	
	/**
	 * Method for determining if the location have indeed been set.
	 * @return true, if latitude != 0.0 and longitude != 0.0
	 */
	public boolean isSet() {
		return isValidLocation(getLatitude(), getLongitude());
	}
	
	/**
	 * Method for validating an location's latitude and longitude
	 * @param lat to check
	 * @param lng to check
	 * @return true, if lat != 0.0 and lng != 0.0
	 */
	public static boolean isValidLocation(double lat, double lng) {
		return isValid(lat) && isValid(lng);
	}
	
	
	
	/*
	 * A coordinate is valid is it's not in the range of -0.1 to 0.1
	 */
	private static boolean isValid(double coordinate) {
		return !(-0.1 < coordinate && coordinate < 0.1);
	}
	
	public boolean isBoundsSet() {
		return (mBoundNorth != DEFAULT_BOUND && 
				mBoundSouth != DEFAULT_BOUND && 
				mBoundEast != DEFAULT_BOUND && 
				mBoundWest != DEFAULT_BOUND);
	}

	/**
	 * Returns a JSONObject, with mapped values for, what is needed for an API request:
	 * <li>Latitude
	 * <li>Longitude
	 * <li>Sensor
	 * <li>Radius
	 * @return The mapped JSONObject
	 */
	public JSONObject toJSON() {
		JSONObject o = new JSONObject();
		try {
			o.put(Param.LATITUDE, getLatitude());
			o.put(Param.LONGITUDE, getLongitude());
			o.put(Param.SENSOR, isSensor());
			o.put(Param.RADIUS, getRadius());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	/**
	 * Returns the approximate distance in meters between this location and the given location. Distance is defined using the WGS84 ellipsoid.
	 * @see {@link #distanceTo(Location)}
	 * @param store to mesure distance to
	 * @return the approximate distance in meters
	 */
	public int distanceTo(Store store) {
		Location tmp = new Location(EtaLocation.ETA_PROVIDER);
		tmp.setLatitude(store.getLatitude());
		tmp.setLongitude(store.getLongitude());
		float dist = distanceTo(tmp);
		return (int)dist;
	}

	/**
	 * Set the bounds for your search.
	 * All parameters should be GPS coordinates.
	 * @param boundEast
	 * @param boundWest
	 * @param boundEast
	 * @param boundEast
	 */
	public void setBounds(double boundNorth, double boundEast, double boundSouth, double boundWest) {
		mBoundEast = boundEast;
		mBoundNorth = boundNorth;
		mBoundSouth = boundSouth;
		mBoundWest = boundWest;
		setTimeNow();
		notifySubscribers();
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
	
	private void setTimeNow() {
		super.setTime(System.currentTimeMillis());
	}
	
	/**
	 * Generates a set of parameters that can be used as query parameters, when making requests to API v2.
	 * @return a bundle containing query parameters
	 */
	public Bundle getQuery() {
		
		Bundle b = new Bundle();

		b.putDouble(Param.LATITUDE, getLatitude());
		b.putDouble(Param.LONGITUDE, getLongitude());
		b.putBoolean(Param.SENSOR, isSensor());
		b.putInt(Param.RADIUS, getRadius());

		// Determine whether to include bounds.
		if (isBoundsSet()) {
			b.putDouble(Param.BOUND_EAST, getBoundEast());
			b.putDouble(Param.BOUND_NORTH, getBoundNorth());
			b.putDouble(Param.BOUND_SOUTH, getBoundSouth());
			b.putDouble(Param.BOUND_WEST, getBoundWest());
		}
		return b;
		
	}
	
	/**
	 * Saves this locations state to SharedPreferences. This method is called on all onPause events.
	 */
	public void saveState() {
		SharedPreferences.Editor e = mEta.getSettings().getPrefs().edit();
    	e.putBoolean(Settings.LOC_SENSOR, mSensor);
		e.putInt(Settings.LOC_RADIUS, mRadius);
		e.putFloat(Settings.LOC_LATITUDE, (float)getLatitude());
		e.putFloat(Settings.LOC_LONGITUDE, (float)getLongitude());
		
		e.putFloat(Settings.LOC_BOUND_EAST, (float)mBoundEast);
		e.putFloat(Settings.LOC_BOUND_WEST, (float)mBoundWest);
		e.putFloat(Settings.LOC_BOUND_NORTH, (float)mBoundNorth);
		e.putFloat(Settings.LOC_BOUND_SOUTH, (float)mBoundSouth);
		e.putString(Settings.LOC_ADDRESS, mAddress);
		e.putLong(Settings.LOC_TIME, getTime());
		e.commit();
	}
	
	public boolean restoreState() {
		
		SharedPreferences prefs = mEta.getSettings().getPrefs();
		if (prefs.contains(Settings.LOC_SENSOR) && prefs.contains(Settings.LOC_RADIUS) && prefs.contains(Settings.LOC_LATITUDE) && 
				prefs.contains(Settings.LOC_LONGITUDE) && prefs.contains(Settings.LOC_ADDRESS)  && prefs.contains(Settings.LOC_TIME) ) {
			
			mSensor = prefs.getBoolean(Settings.LOC_SENSOR, false);
			mRadius = prefs.getInt(Settings.LOC_RADIUS, Integer.MAX_VALUE);
			super.setLatitude(prefs.getFloat(Settings.LOC_LATITUDE, 0.0f));
			super.setLongitude(prefs.getFloat(Settings.LOC_LONGITUDE, 0.0f));
			mBoundEast = prefs.getFloat(Settings.LOC_BOUND_EAST, 0.0f);
			mBoundWest = prefs.getFloat(Settings.LOC_BOUND_WEST, 0.0f);
			mBoundNorth = prefs.getFloat(Settings.LOC_BOUND_NORTH, 0.0f);
			mBoundSouth = prefs.getFloat(Settings.LOC_BOUND_SOUTH, 0.0f);
			mAddress = prefs.getString(Settings.LOC_ADDRESS, null);
			super.setTime(prefs.getLong(Settings.LOC_TIME, System.currentTimeMillis()));
			return true;
		} 
		return false;
		
	}
	
	/**
	 * Invoke notifications to subscribers of this location object.<br><br>
	 * This object automatically notifies all subscribers on changes.
	 */
	public void notifySubscribers() {
		
		for (LocationListener l : mSubscribers) {
			try {
				l.onLocationChange();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Subscribe to events and changes in location.
	 * @param listener for callbacks
	 */
	public void subscribe(LocationListener listener) {
		if (!mSubscribers.contains(listener))
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
        return "Location[provider=" + getProvider() +
                ",time=" + getTime() +
                ",latitude=" + getLatitude() +
                ",longitude=" + getLongitude() +
                ",address=" + getAddress() + 
                ",radius=" + mRadius +
                ",sensor=" + mSensor + 
                ",bounds=[west=" + getBoundWest() + 
                ",north" + getBoundNorth() + 
                ",east=" + getBoundEast() + 
                ",south" + getBoundSouth() + "]" +
                "]";
	}
	
	public interface LocationListener {
		public void onLocationChange();
	}
	
}