/**
 * @fileoverview	Location.
 * @author			Danny Hvam <danny@eTilbudsavis.dk>
 */
package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationManager;

import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.Utils.Params;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class EtaLocation extends Location {

	public static final String TAG = "EtaLocation";
	
	private static final String ETA_PROVIDER	= "etasdk";
	
	/** API v2 parameter name for sensor. */
	public static final String SENSOR = Params.SENSOR;
	
	/** API v2 parameter name for latitude. */
	public static final String LATITUDE = Params.LATITUDE;
	
	/** API v2 parameter name for longitude. */
	public static final String LONGITUDE = Params.LONGITUDE;
	
	/** API v2 parameter name for radius. */
	public static final String RADIUS = Params.RADIUS;
	
	/** API v2 parameter name for bounds east. */
	public static final String BOUND_EAST = Params.BOUND_EAST;
	
	/** API v2 parameter name for bounds north. */
	public static final String BOUND_NORTH = Params.BOUND_NORTH;
	
	/** API v2 parameter name for bounds south. */
	public static final String BOUND_SOUTH = Params.BOUND_SOUTH;
	
	/** API v2 parameter name for bounds west. */
	public static final String BOUND_WEST = Params.BOUND_WEST;

	private static final String ADDRESS = "etasdk_loc_address";
	private static final String TIME = "etasdk_loc_time";
	private static final int RADIUS_MIN = 0;
	private static final int RADIUS_MAX = 700000;
	private static final double BOUND_DEFAULT = 0.0;
	
	// Location.
	private int mRadius = RADIUS_MAX;
	private boolean mSensor = false;
	private String mAddress = "";
	private double mBoundNorth = BOUND_DEFAULT;
	private double mBoundEast = BOUND_DEFAULT;
	private double mBoundSouth = BOUND_DEFAULT;
	private double mBoundWest = BOUND_DEFAULT;
	private SharedPreferences mSharedPrefs;
	private ArrayList<LocationListener> mSubscribers;

	public EtaLocation(SharedPreferences prefs) {
		super(ETA_PROVIDER);
		mSharedPrefs = prefs;
		mSubscribers = new ArrayList<LocationListener>();
		fromSharedPrefs();
	}

	@Override
	public void set(Location l) {
		super.set(l);
		mSensor = (getProvider().equals(LocationManager.GPS_PROVIDER) || getProvider().equals(LocationManager.NETWORK_PROVIDER) );
		toSharedPrefs();
	}
	
	/**
	 * Set location for an address that has been geocoded to a latitude, longitude format<br /><br />
	 * NOTE: This implicitly implies that, no {@link #setSensor(boolean) sensor} has been used.
	 * @see https://developers.google.com/maps/documentation/geocoding/ for more info
	 * 
	 * @param address that has been geocoded
	 * @param latitude of the address
	 * @param longitude of the address
	 * @return
	 */
	public EtaLocation set(String address, double latitude, double longitude) {
		mAddress = address;
		setLatitude(latitude);
		setLongitude(longitude);
		toSharedPrefs();
		return this;
	}
	
	/**
	 * Set the current search radius.
	 * @param radius in meters <li> Min value = 0 <li> Max value = 700000
	 * @return this Object, for easy chaining of set methods.
	 */
	public EtaLocation setRadius(int radius) {
		mRadius =  radius < RADIUS_MIN ? RADIUS_MIN : ( radius > RADIUS_MAX ? RADIUS_MAX : radius );
		toSharedPrefs();
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
		toSharedPrefs();
		return this;
	}
	
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
		toSharedPrefs();
		return this;
	}
	
	
	public String getAddress() {
		return mAddress;
	}

	public Boolean isSet() {
		return (getLatitude() != 0.0 && getLongitude() != 0.0);
	}

	public Boolean isBoundsSet() {
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
			o.put(LATITUDE, getLatitude());
			o.put(LONGITUDE, getLongitude());
			o.put(SENSOR, isSensor());
			o.put(RADIUS, getRadius());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o;
	}

	public int distanceToStore(Store store) {
		Location tmp = new Location(EtaLocation.ETA_PROVIDER);
		tmp.setLatitude(store.getLatitude());
		tmp.setLongitude(store.getLongitude());
		double dist = distanceTo(tmp);
		return (int)dist;
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
		mBoundEast = boundEast;
		mBoundNorth = boundNorth;
		mBoundSouth = boundSouth;
		mBoundWest = boundWest;
		toSharedPrefs();
	}

	/**
	 * GPS coordinate for the northern bound of a search.
	 * @param boundsNorth
	 */
	public EtaLocation setBoundNorth(double boundNorth) {
		mBoundNorth = boundNorth;
		toSharedPrefs();
		return this;
	}

	/**
	 * GPS coordinate for the eastern bound of a search.
	 * @param boundEast
	 */
	public EtaLocation setBoundEast(double boundEast) {
		mBoundEast = boundEast;
		toSharedPrefs();
		return this;
	}

	/**
	 * GPS coordinate for the southern bound of a search.
	 * @param boundSouth
	 */
	public EtaLocation setBoundSouth(double boundSouth) {
		mBoundSouth = boundSouth;
		toSharedPrefs();
		return this;
	}

	/**
	 * GPS coordinate for the western bound of a search.
	 * @param boundWest
	 */
	public EtaLocation setBoundWest(double boundWest) {
		mBoundWest = boundWest;
		toSharedPrefs();
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
	
	public List<NameValuePair> getQuery() {
		
		List<NameValuePair> query = new ArrayList<NameValuePair>();
		
		query.add(Utils.getNameValuePair(LATITUDE, getLatitude()));
		query.add(Utils.getNameValuePair(LONGITUDE, getLongitude()));
		query.add(Utils.getNameValuePair(SENSOR, isSensor()));
		query.add(Utils.getNameValuePair(RADIUS, getRadius()));

		// Determine whether to include bounds.
		if (isBoundsSet()) {
			query.add(Utils.getNameValuePair(BOUND_EAST, getBoundEast()));
			query.add(Utils.getNameValuePair(BOUND_NORTH, getBoundNorth()));
			query.add(Utils.getNameValuePair(BOUND_SOUTH, getBoundSouth()));
			query.add(Utils.getNameValuePair(BOUND_WEST, getBoundWest()));
		}
		return query;
	}

//	public void onSaveInstanceState(Bundle savedInstanceState) {
//		savedInstanceState.putBoolean(SENSOR, mSensor);
//		savedInstanceState.putInt(RADIUS, mRadius);
//		savedInstanceState.putDouble(LATITUDE, getLatitude());
//		savedInstanceState.putDouble(LONGITUDE, getLongitude());
//		savedInstanceState.putDouble(BOUND_EAST, mBoundEast);
//		savedInstanceState.putDouble(BOUND_WEST, mBoundWest);
//		savedInstanceState.putDouble(BOUND_NORTH, mBoundNorth);
//		savedInstanceState.putDouble(BOUND_SOUTH, mBoundSouth);
//		savedInstanceState.putString(ADDRESS, mAddress);
//		savedInstanceState.putLong(TIME, getTime());
//
//	}
//	
//	public void onRestoreInstanceState(Bundle savedInstanceState) {
//		mSensor = savedInstanceState.getBoolean(SENSOR);
//		mRadius = savedInstanceState.getInt(RADIUS);
//		setLatitude(savedInstanceState.getDouble(LATITUDE));
//		setLongitude(savedInstanceState.getDouble(LONGITUDE));
//		mBoundEast = savedInstanceState.getDouble(BOUND_EAST);
//		mBoundWest = savedInstanceState.getDouble(BOUND_WEST);
//		mBoundNorth = savedInstanceState.getDouble(BOUND_NORTH);
//		mBoundSouth = savedInstanceState.getDouble(BOUND_SOUTH);
//		mAddress = savedInstanceState.getString(ADDRESS);
//		setTime(savedInstanceState.getLong(TIME));
//	}
	
	private void toSharedPrefs() {
		setTime(System.currentTimeMillis());
		new Thread() {
	        public void run() {
	        	mSharedPrefs.edit()
	    		.putBoolean(SENSOR, mSensor)
	    		.putInt(RADIUS, mRadius)
	    		.putFloat(LATITUDE, (float)getLatitude())
	    		.putFloat(LONGITUDE, (float)getLongitude())
	    		.putFloat(BOUND_EAST, (float)mBoundEast)
	    		.putFloat(BOUND_WEST, (float)mBoundWest)
	    		.putFloat(BOUND_NORTH, (float)mBoundNorth)
	    		.putFloat(BOUND_SOUTH, (float)mBoundSouth)
	    		.putString(ADDRESS, mAddress)
	    		.putLong(TIME, getTime())
	    		.commit();
	        }
		}.start();
		
	}
	
	private boolean fromSharedPrefs() {
		if (mSharedPrefs.contains(SENSOR) && mSharedPrefs.contains(RADIUS) && mSharedPrefs.contains(LATITUDE) && 
				mSharedPrefs.contains(LONGITUDE) && mSharedPrefs.contains(BOUND_EAST) && mSharedPrefs.contains(BOUND_WEST) && 
				mSharedPrefs.contains(BOUND_NORTH) && mSharedPrefs.contains(BOUND_SOUTH) && mSharedPrefs.contains(TIME) ) {
			
			mSensor = mSharedPrefs.getBoolean(SENSOR, false);
			mRadius = mSharedPrefs.getInt(RADIUS, Integer.MAX_VALUE);
			setLatitude(mSharedPrefs.getFloat(LATITUDE, 0f));
			setLongitude(mSharedPrefs.getFloat(LONGITUDE, 0f));
			mBoundEast = mSharedPrefs.getFloat(BOUND_EAST, 0f);
			mBoundWest = mSharedPrefs.getFloat(BOUND_WEST, 0f);
			mBoundNorth = mSharedPrefs.getFloat(BOUND_NORTH, 0f);
			mBoundSouth = mSharedPrefs.getFloat(BOUND_SOUTH, 0f);
			mAddress = mSharedPrefs.getString(ADDRESS, null);
			setTime(mSharedPrefs.getLong(TIME, System.currentTimeMillis()));
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