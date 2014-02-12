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
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class EtaLocation extends Location {

	public static final String TAG = "EtaLocation";
	
	private static final String ETA_PROVIDER	= "etasdk";
	
	private static final int RADIUS_MIN = 0;
	private static final int RADIUS_MAX = 700000;
	private static final double BOUND_DEFAULT = 0.0;
	
	// Location.
	private double mLatitude = 0.0;
	private double mLongitude = 0.0;
	private long mTime = 0;
	private int mRadius = RADIUS_MAX;
	private boolean mSensor = false;
	private String mAddress = null;
	private double mBoundNorth = BOUND_DEFAULT;
	private double mBoundEast = BOUND_DEFAULT;
	private double mBoundSouth = BOUND_DEFAULT;
	private double mBoundWest = BOUND_DEFAULT;
	private Eta mEta;
	private ArrayList<LocationListener> mSubscribers = new ArrayList<LocationListener>();
	
	public EtaLocation(Eta eta) {
		super(ETA_PROVIDER);
		mEta = eta;
		restoreState();
	}

	@Override
	public void set(Location l) {
		mLatitude = l.getLatitude();
		mLongitude = l.getLongitude();
		mTime = l.getTime();
		mSensor = (getProvider().equals(LocationManager.GPS_PROVIDER) || getProvider().equals(LocationManager.NETWORK_PROVIDER) );
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
		mLatitude = latitude;
		mLongitude = longitude;
		mSensor = false;
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
		mLatitude = latitude;
		notifySubscribers();
	}
	
	@Override
	public double getLatitude() {
		return mLatitude;
	}
	
	@Override
	public void setLongitude(double longitude) {
		mLongitude = longitude;
		notifySubscribers();
	}

	@Override
	public double getLongitude() {
		return mLongitude;
	}
	
	@Override
	public long getTime() {
		return mTime;
	}
	
	@Override
	public void setTime(long time) {
		EtaLog.d(TAG, "setTime: " + time);
		mTime = time;
		notifySubscribers();
	}
	
	/**
	 * Set whether the location has been set by sensor.<br>
	 * Senser is automatically set to true if location is set via {@link #set(Location) setLocation(Location l)}
	 * and the given location is from a sensor (network, or gps).
	 * @param sensor
	 */
	public void setSensor(boolean sensor) {
		mSensor = sensor;
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
	 * Set an postal address of a location.<br /><br />
	 * <b>NOTICE</b> The address is purely a convenience for the developers.<br />
	 * The SDK does NOT USE the data for anything, only latitude, longitude, radius and sensor are used, and hence they must be set.
	 * @param address
	 * @return This EtaLocation object
	 */
	public EtaLocation setAddress(String address) {
		mAddress = address;
		notifySubscribers();
		return this;
	}
	
	/**
	 * Returns an address, of this location if the address have previously been set.
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
		return (mBoundNorth != BOUND_DEFAULT && 
				mBoundSouth != BOUND_DEFAULT && 
				mBoundEast != BOUND_DEFAULT && 
				mBoundWest != BOUND_DEFAULT);
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
			o.put(Request.Param.LATITUDE, getLatitude());
			o.put(Request.Param.LONGITUDE, getLongitude());
			o.put(Request.Param.SENSOR, isSensor());
			o.put(Request.Param.RADIUS, getRadius());
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
	
	/**
	 * Generates a set of parameters that can be used as query parameters, when making requests to API v2.
	 * @return a bundle containing query parameters
	 */
	public Bundle getQuery() {
		
		Bundle b = new Bundle();

		b.putDouble(Request.Param.LATITUDE, getLatitude());
		b.putDouble(Request.Param.LONGITUDE, getLongitude());
		b.putBoolean(Request.Param.SENSOR, isSensor());
		b.putInt(Request.Param.RADIUS, getRadius());

		// Determine whether to include bounds.
		if (isBoundsSet()) {
			b.putDouble(Request.Param.BOUND_EAST, getBoundEast());
			b.putDouble(Request.Param.BOUND_NORTH, getBoundNorth());
			b.putDouble(Request.Param.BOUND_SOUTH, getBoundSouth());
			b.putDouble(Request.Param.BOUND_WEST, getBoundWest());
		}
		return b;
		
	}
	
	/**
	 * Saves this locations state to SharedPreferences. This method is called on all onPause events.
	 */
	public void saveState() {
		SharedPreferences.Editor editor = mEta.getSettings().getPrefs().edit();
    	editor
		.putBoolean(Settings.LOC_SENSOR, mSensor)
		.putInt(Settings.LOC_RADIUS, mRadius)
		.putFloat(Settings.LOC_LATITUDE, (float)mLatitude)
		.putFloat(Settings.LOC_LONGITUDE, (float)mLongitude)
		.putFloat(Settings.LOC_BOUND_EAST, (float)mBoundEast)
		.putFloat(Settings.LOC_BOUND_WEST, (float)mBoundWest)
		.putFloat(Settings.LOC_BOUND_NORTH, (float)mBoundNorth)
		.putFloat(Settings.LOC_BOUND_SOUTH, (float)mBoundSouth)
		.putString(Settings.LOC_ADDRESS, mAddress)
		.putLong(Settings.LOC_TIME, mTime)
		.commit();
	}
	
	public boolean restoreState() {
		
		SharedPreferences prefs = mEta.getSettings().getPrefs();
		if (prefs.contains(Settings.LOC_SENSOR) && prefs.contains(Settings.LOC_RADIUS) && prefs.contains(Settings.LOC_LATITUDE) && 
				prefs.contains(Settings.LOC_LONGITUDE) && prefs.contains(Settings.LOC_ADDRESS)  && prefs.contains(Settings.LOC_TIME) ) {
			
			mSensor = prefs.getBoolean(Settings.LOC_SENSOR, false);
			mRadius = prefs.getInt(Settings.LOC_RADIUS, Integer.MAX_VALUE);
			mLatitude = prefs.getFloat(Settings.LOC_LATITUDE, 0.0f);
			mLongitude = prefs.getFloat(Settings.LOC_LONGITUDE, 0.0f);
			mBoundEast = prefs.getFloat(Settings.LOC_BOUND_EAST, 0.0f);
			mBoundWest = prefs.getFloat(Settings.LOC_BOUND_WEST, 0.0f);
			mBoundNorth = prefs.getFloat(Settings.LOC_BOUND_NORTH, 0.0f);
			mBoundSouth = prefs.getFloat(Settings.LOC_BOUND_SOUTH, 0.0f);
			mAddress = prefs.getString(Settings.LOC_ADDRESS, null);
			mTime = prefs.getLong(Settings.LOC_TIME, System.currentTimeMillis());
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