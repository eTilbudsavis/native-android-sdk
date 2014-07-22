/**
 * @fileoverview	Location.
 * @author			Danny Hvam <danny@eTilbudsavis.dk>
 */
package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.location.LocationManager;

import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;
import com.eTilbudsavis.etasdk.Utils.Param;

public class EtaLocation extends Location {

	public static final String TAG = Eta.TAG_PREFIX + EtaLocation.class.getSimpleName();
	
	private static final String ETA_PROVIDER = "etasdk";
	private static final String GMAPS_PROVIDER = "fused";
	private static final String PASSIVE_PROVIDER = "passive";
	
	public static final String ACCURACY  = "accuracy";
	public static final String BEARING  = "accuracy";
	public static final String ALTITUDE  = "altitude";
	public static final String PROVIDER  = "accuracy";
	public static final String SPEED  = "speed";
	public static final String TIME  = "time";
	public static final String ADDRESS  = "address";
	
	public static final int RADIUS_MIN = 0;
	public static final int RADIUS_MAX = 700000;
	public static final int DEFAULT_RADIUS = 100000;
	public static final double DEFAULT_COORDINATE = 0.0d;
	
	private int mRadius = DEFAULT_RADIUS;
	private boolean mSensor = false;
	private String mAddress = null;
	private double mBoundNorth = DEFAULT_COORDINATE;
	private double mBoundEast = DEFAULT_COORDINATE;
	private double mBoundSouth = DEFAULT_COORDINATE;
	private double mBoundWest = DEFAULT_COORDINATE;
	private List<LocationListener> mSubscribers = new ArrayList<LocationListener>();
	
	public EtaLocation() {
		super(ETA_PROVIDER);
	}
	
	public EtaLocation(EtaLocation l) {
		super(l);
		mAddress = l.getAddress();
		mBoundEast = l.getBoundEast();
		mBoundNorth = l.getBoundNorth();
		mBoundSouth = l.getBoundSouth();
		mBoundWest = l.getBoundWest();
		mRadius = l.getRadius();
		mSensor = l.isSensor();
	}
	
	public EtaLocation(JSONObject o) {
		this();
		setAccuracy(Json.valueOf(o, ACCURACY, getAccuracy()));
		setAddress(Json.valueOf(o, ADDRESS, getAddress()));
		setAltitude(Json.valueOf(o, ALTITUDE, getAltitude()));
		setBearing(Json.valueOf(o, BEARING, getBearing()));
		setLatitude(Json.valueOf(o, Param.LATITUDE, getLatitude()));
		setLongitude(Json.valueOf(o, Param.LONGITUDE, getLongitude()));
		setProvider(Json.valueOf(o, PROVIDER, getProvider()));
		setRadius(Json.valueOf(o, Param.RADIUS, DEFAULT_RADIUS));
		setSpeed(Json.valueOf(o, SPEED, getSpeed()));
		setTime(Json.valueOf(o, TIME, getTime()));
		setSensor(Json.valueOf(o, Param.SENSOR, false));
		double east = Json.valueOf(o, Param.BOUND_EAST, DEFAULT_COORDINATE);
		double west = Json.valueOf(o, Param.BOUND_WEST, DEFAULT_COORDINATE);
		double north = Json.valueOf(o, Param.BOUND_NORTH, DEFAULT_COORDINATE);
		double south = Json.valueOf(o, Param.BOUND_SOUTH, DEFAULT_COORDINATE);
		setBounds(north, east, south, west);
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
			o.put(ACCURACY, getAccuracy());
			o.put(ADDRESS, getAddress());
			o.put(ALTITUDE, getAltitude());
			o.put(BEARING, getBearing());
			o.put(Param.LATITUDE, getLatitude());
			o.put(Param.LONGITUDE, getLongitude());
			o.put(PROVIDER, getProvider());
			o.put(Param.RADIUS, getRadius());
			o.put(SPEED, getSpeed());
			o.put(TIME, getTime());
			o.put(Param.SENSOR, isSensor());
			if (isBoundsSet()) {
				o.put(Param.BOUND_EAST, getBoundEast());
				o.put(Param.BOUND_NORTH, getBoundNorth());
				o.put(Param.BOUND_SOUTH, getBoundSouth());
				o.put(Param.BOUND_WEST, getBoundWest());
			}
		} catch (JSONException e) {
			EtaLog.e(TAG, null, e);
		}
		return o;
	}
	
	@Override
	public void set(Location l) {
		super.set(l);
		mSensor = isFromSensor(l);
	}
	
	public static boolean isFromSensor(Location l) {
		String provider = l.getProvider();
		return (LocationManager.GPS_PROVIDER.equals(provider) ||
				LocationManager.NETWORK_PROVIDER.equals(provider) ||
				PASSIVE_PROVIDER.equals(provider) ||
				GMAPS_PROVIDER.equals(provider) );
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
		set(address, latitude, longitude, mRadius);
	}
	
	/**
	 * Set location for an address that has been geocoded to a latitude, longitude format<br /><br />
	 * NOTE: This implicitly implies that, no {@link #setSensor(boolean) sensor} has been used.
	 * https://developers.google.com/maps/documentation/geocoding/ for more info
	 * 
	 * @param address that has been geocoded
	 * @param latitude of the address
	 * @param longitude of the address
	 * @param radius A radius to the point
	 * @return this object
	 */
	public void set(String address, double latitude, double longitude, int radius) {
		mAddress = address;
		super.setLatitude(latitude);
		super.setLongitude(longitude);
		mSensor = false;
		mRadius = radius;
		super.setTime(System.currentTimeMillis());
	}
	
	/**
	 * Set the current search radius.
	 * @param radius in meters <li> Min value = 0 <li> Max value = 700000
	 * @throws IllegalArgumentException if radius is out of bounds
	 * @return this Object, for easy chaining of set methods.
	 */
	public void setRadius(int radius) {
		if (radius < RADIUS_MIN || radius > RADIUS_MAX) {
			String errMsg = "Radius must be within range %s to %s, provided radius: %s";
			errMsg = String.format(errMsg, RADIUS_MIN, RADIUS_MAX, radius);
			throw new IllegalArgumentException(errMsg);
		}
		mRadius = radius;
		super.setTime(System.currentTimeMillis());
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
		super.setTime(System.currentTimeMillis());
	}
	
	@Override
	public void setLongitude(double longitude) {
		super.setLongitude(longitude);
		super.setTime(System.currentTimeMillis());
	}
	
	@Override
	public void setTime(long time) {
		super.setTime(time);
	}
	
	/**
	 * Set whether the location has been set by sensor.<br>
	 * Sensor is automatically set to true if location is set via {@link #set(Location) setLocation(Location l)}
	 * and the given location is from a sensor (network, or GPS).
	 * @param sensor
	 */
	public void setSensor(boolean sensor) {
		mSensor = sensor;
		super.setTime(System.currentTimeMillis());
	}
	
	/**
	 * Determines if the location is set by a sensor
	 * @return true if location have been set by a sensor
	 */
	public boolean isSensor() {
		return mSensor;
	}
	
	/**
	 * 
	 * @param address
	 */
	public void setAddress(String address) {
		mAddress = address;
		super.setTime(System.currentTimeMillis());
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
		return (mBoundNorth != DEFAULT_COORDINATE && 
				mBoundSouth != DEFAULT_COORDINATE && 
				mBoundEast != DEFAULT_COORDINATE && 
				mBoundWest != DEFAULT_COORDINATE);
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
		super.setTime(System.currentTimeMillis());
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
	 * Method clears all location variables, and saves the new state to preferences.
	 */
	public void clear() {
		super.setAccuracy(0.0f);
		super.setAltitude(0.0d);
		super.setBearing(0.0f);
		super.setExtras(null);
		super.setLatitude(DEFAULT_COORDINATE);
		super.setLongitude(DEFAULT_COORDINATE);
		super.setProvider(ETA_PROVIDER);
		super.setSpeed(0.0f);
		super.setTime(System.currentTimeMillis());
		mAddress = null;
		mBoundEast = DEFAULT_COORDINATE;
		mBoundNorth = DEFAULT_COORDINATE;
		mBoundSouth = DEFAULT_COORDINATE;
		mBoundWest = DEFAULT_COORDINATE;
		mRadius = DEFAULT_RADIUS;
		mSensor = false;
	}
	
	public void notifyDataSetChanged() {
		notifySubscribers();
	}
	
	/**
	 * Invoke notifications to subscribers of this location object.<br><br>
	 * This object automatically notifies all subscribers on changes.
	 */
	private void notifySubscribers() {
		Eta.getInstance().getHandler().post(new Runnable() {
			
			public void run() {

				for (LocationListener l : mSubscribers) {
					try {
						l.onLocationChange();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		});
	}
	
	/**
	 * Subscribe to events and changes in location.
	 * @param listener for callbacks
	 */
	public void subscribe(LocationListener listener) {
		if (!mSubscribers.contains(listener)) {
			mSubscribers.add(listener);
		}
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
                ",address=[" + getAddress() + "]" + 
                ",radius=" + mRadius +
                ",sensor=" + mSensor + 
                ",bounds=[west=" + getBoundWest() + 
                ",north=" + getBoundNorth() + 
                ",east=" + getBoundEast() + 
                ",south=" + getBoundSouth() + "]" +
                "]";
	}
	
	public interface LocationListener {
		public void onLocationChange();
	}
	
}