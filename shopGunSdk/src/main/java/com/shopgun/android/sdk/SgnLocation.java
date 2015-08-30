/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/**
 * @fileoverview Location.
 * @author Danny Hvam <danny@eTilbudsavis.dk>
 */
package com.shopgun.android.sdk;

import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.utils.Api.Param;
import com.shopgun.android.sdk.utils.Json;

import org.json.JSONException;
import org.json.JSONObject;

public class SgnLocation extends Location {

    public static final String TAG = Constants.getTag(SgnLocation.class);
    public static final int RADIUS_MIN = 0;
    public static final int RADIUS_MAX = 700000;
    public static final int DEFAULT_RADIUS = 100000;
    public static final double DEFAULT_COORDINATE = 0.0d;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SgnLocation> CREATOR = new Parcelable.Creator<SgnLocation>() {
        @Override
        public SgnLocation createFromParcel(Parcel in) {
            return new SgnLocation(in);
        }

        @Override
        public SgnLocation[] newArray(int size) {
            return new SgnLocation[size];
        }
    };
    private static final String ERROR_RADIUS = "Radius must be within range %s to %s, provided radius: %s";
    private static final String ETA_PROVIDER = "shopgun";
    private static final String GMAPS_PROVIDER = "fused";
    private static final String PASSIVE_PROVIDER = "passive";
    private int mRadius = DEFAULT_RADIUS;
    private boolean mSensor = false;
    private String mAddress = null;
    private double mBoundNorth = DEFAULT_COORDINATE;
    private double mBoundEast = DEFAULT_COORDINATE;
    private double mBoundSouth = DEFAULT_COORDINATE;
    private double mBoundWest = DEFAULT_COORDINATE;

    public SgnLocation() {
        super(ETA_PROVIDER);
    }

    public SgnLocation(SgnLocation l) {
        this();
        set(l);
    }

    public static SgnLocation fromJSON(JSONObject o) {
        SgnLocation l = new SgnLocation();
        if (o == null) {
            return l;
        }
        l.setAccuracy(Json.valueOf(o, Param.ACCURACY, l.getAccuracy()));
        l.setAddress(Json.valueOf(o, Param.ADDRESS, l.getAddress()));
        l.setAltitude(Json.valueOf(o, Param.ALTITUDE, l.getAltitude()));
        l.setBearing(Json.valueOf(o, Param.BEARING, l.getBearing()));
        l.setLatitude(Json.valueOf(o, Param.LATITUDE, l.getLatitude()));
        l.setLongitude(Json.valueOf(o, Param.LONGITUDE, l.getLongitude()));
        l.setProvider(Json.valueOf(o, Param.PROVIDER, l.getProvider()));
        l.setRadius(Json.valueOf(o, Param.RADIUS, DEFAULT_RADIUS));
        l.setSpeed(Json.valueOf(o, Param.SPEED, l.getSpeed()));
        l.setTime(Json.valueOf(o, Param.TIME, l.getTime()));
        l.setSensor(Json.valueOf(o, Param.SENSOR, false));
        double east = Json.valueOf(o, Param.BOUND_EAST, DEFAULT_COORDINATE);
        double west = Json.valueOf(o, Param.BOUND_WEST, DEFAULT_COORDINATE);
        double north = Json.valueOf(o, Param.BOUND_NORTH, DEFAULT_COORDINATE);
        double south = Json.valueOf(o, Param.BOUND_SOUTH, DEFAULT_COORDINATE);
        l.setBounds(north, east, south, west);
        return l;
    }

    protected SgnLocation(Parcel in) {
        super(ETA_PROVIDER);
        set(Location.CREATOR.createFromParcel(in));
        mRadius = in.readInt();
        mSensor = in.readByte() != 0x00;
        mAddress = in.readString();
        mBoundNorth = in.readDouble();
        mBoundEast = in.readDouble();
        mBoundSouth = in.readDouble();
        mBoundWest = in.readDouble();
    }

    public static boolean isFromSensor(Location l) {
        String provider = l.getProvider();
        return (LocationManager.GPS_PROVIDER.equals(provider) ||
                LocationManager.NETWORK_PROVIDER.equals(provider) ||
                PASSIVE_PROVIDER.equals(provider) ||
                GMAPS_PROVIDER.equals(provider));
    }

    /**
     * Method for validating an location's latitude and longitude
     *
     * @param location A location to check
     * @return true, if location.lat != 0.0 and location.lng != 0.0
     */
    public static boolean isValidLocation(Location location) {
        return isValid(location.getLatitude()) && isValid(location.getLongitude());
    }

    /**
     * Method for validating an location's latitude and longitude
     *
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

    /**
     * Returns a JSONObject, with mapped values for, what is needed for an API request:
     * <li>Latitude
     * <li>Longitude
     * <li>Sensor
     * <li>Radius
     *
     * @return The mapped JSONObject
     */
    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        try {
            o.put(Param.ACCURACY, getAccuracy());
            o.put(Param.ADDRESS, getAddress());
            o.put(Param.ALTITUDE, getAltitude());
            o.put(Param.BEARING, getBearing());
            o.put(Param.LATITUDE, getLatitude());
            o.put(Param.LONGITUDE, getLongitude());
            o.put(Param.PROVIDER, getProvider());
            o.put(Param.RADIUS, getRadius());
            o.put(Param.SPEED, getSpeed());
            o.put(Param.TIME, getTime());
            o.put(Param.SENSOR, isSensor());
            if (isBoundsSet()) {
                o.put(Param.BOUND_EAST, getBoundEast());
                o.put(Param.BOUND_NORTH, getBoundNorth());
                o.put(Param.BOUND_SOUTH, getBoundSouth());
                o.put(Param.BOUND_WEST, getBoundWest());
            }
        } catch (JSONException e) {
            SgnLog.e(TAG, null, e);
        }
        return o;
    }

    /**
     * Sets the contents of the location to the values from the given location.
     * <p>The sensor value is explicitly set to true when setting location via this method (as it's likely from a sensor
     * e.g. gps or network</p>
     */
    @Override
    public void set(Location l) {
        super.set(l);
        mSensor = true;
    }

    public void set(SgnLocation l) {
        super.set(l);
        mAddress = l.getAddress();
        mBoundEast = l.getBoundEast();
        mBoundNorth = l.getBoundNorth();
        mBoundSouth = l.getBoundSouth();
        mBoundWest = l.getBoundWest();
        mRadius = l.getRadius();
        mSensor = l.isSensor();
    }

    /**
     * Get current radius
     *
     * @return radius in meters.
     */
    public int getRadius() {
        return mRadius;
    }

    /**
     * Set the current search radius.
     *
     * @param radius in meters <li> Min value = 0 <li> Max value = 700000
     * @throws IllegalArgumentException if radius is out of bounds
     */
    public void setRadius(int radius) {
        if (radius < RADIUS_MIN || radius > RADIUS_MAX) {
            throw new IllegalArgumentException(String.format(ERROR_RADIUS, RADIUS_MIN, RADIUS_MAX, radius));
        }
        mRadius = radius;
        super.setTime(System.currentTimeMillis());
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
     * Determines if the location is set by a sensor
     *
     * @return true if location have been set by a sensor
     */
    public boolean isSensor() {
        return mSensor;
    }

    /**
     * Set whether the location has been set by sensor.<br>
     * Sensor is automatically set to true if location is set via {@link #set(Location) setLocation(Location l)}
     * and the given location is from a sensor (network, or GPS).
     *
     * @param sensor true if it's a device sensor, else false
     */
    public void setSensor(boolean sensor) {
        mSensor = sensor;
        super.setTime(System.currentTimeMillis());
    }

    /**
     * Returns an address, of this location if the address have previously been set with {@link #setAddress(String)}.
     *
     * @return an address if one was given, else null
     */
    public String getAddress() {
        return mAddress;
    }

    /**
     * Set a new address on the location
     *
     * @param address An address
     */
    public void setAddress(String address) {
        mAddress = address;
        super.setTime(System.currentTimeMillis());
    }

    /**
     * Method for determining if the location have indeed been set.
     *
     * @return true, if latitude != 0.0 and longitude != 0.0
     */
    public boolean isSet() {
        return isValidLocation(SgnLocation.this);
    }

    public boolean isBoundsSet() {
        return (mBoundNorth != DEFAULT_COORDINATE &&
                mBoundSouth != DEFAULT_COORDINATE &&
                mBoundEast != DEFAULT_COORDINATE &&
                mBoundWest != DEFAULT_COORDINATE);
    }

    /**
     * Returns the approximate distance in meters between this location and the given location. Distance is defined using the WGS84 ellipsoid.
     *
     * @param store to mesure distance to
     * @return the approximate distance in meters
     * @see {@link #distanceTo(Location)}
     */
    public int distanceTo(Store store) {
        Location tmp = new Location(SgnLocation.ETA_PROVIDER);
        tmp.setLatitude(store.getLatitude());
        tmp.setLongitude(store.getLongitude());
        float dist = distanceTo(tmp);
        return (int) dist;
    }

    /**
     * Set the bounds for queries
     * All parameters should be GPS coordinates.
     *
     * @param boundNorth A northern boundary
     * @param boundEast  A eastern boundary
     * @param boundSouth A southern boundary
     * @param boundWest  A western boundary
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

    @Override
    public String toString() {
        return "Location[provider=" + getProvider() +
                ", time=" + getTime() +
                ", latitude=" + getLatitude() +
                ", longitude=" + getLongitude() +
                ", address=[" + getAddress() + "]" +
                ", radius=" + mRadius +
                ", sensor=" + mSensor +
                ", bounds=[west=" + getBoundWest() +
                ", north=" + getBoundNorth() +
                ", east=" + getBoundEast() +
                ", south=" + getBoundSouth() + "]" +
                "]";
    }

    /**
     * Method for validating an location's latitude and longitude
     *
     * @return true, if latitude != 0.0 and longitude != 0.0, else false
     */
    public boolean isValid() {
        return SgnLocation.isValidLocation(getLatitude(), getLongitude());
    }

    /**
     * Checks if two {@link SgnLocation} is at the same point in the eyes of the API.
     * So, latitude, longitude, radius and sensor will be checked. (and null)
     * <p><b>It's not an equals method<b></p>
     *
     * @param other A location to compare with
     * @return true if they are the same, otherwise false
     */
    public boolean isSame(SgnLocation other) {

        if (other == null)
            return false;

        if (this == other)
            return true;

        if (mRadius != other.mRadius)
            return false;

        if (mSensor != other.mSensor)
            return false;

        if (distanceTo(other) > 1)
            return false;

        return true;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mRadius);
        dest.writeByte((byte) (mSensor ? 0x01 : 0x00));
        dest.writeString(mAddress);
        dest.writeDouble(mBoundNorth);
        dest.writeDouble(mBoundEast);
        dest.writeDouble(mBoundSouth);
        dest.writeDouble(mBoundWest);
    }

}