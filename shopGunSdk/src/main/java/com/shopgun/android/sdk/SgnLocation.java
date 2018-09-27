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

package com.shopgun.android.sdk;

import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;

import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

public class SgnLocation extends Location {

    public static final String TAG = Constants.getTag(SgnLocation.class);
    public static final int RADIUS_MIN = 0;
    public static final int RADIUS_MAX = 700000;
    public static final int DEFAULT_RADIUS = 100000;

    private static final String ERROR_RADIUS = "Radius must be within range %s to %s, provided radius: %s";
    private static final String SHOPGUN_PROVIDER = "shopgun";
    private static final String GMAPS_PROVIDER = "fused";
    private static final String PASSIVE_PROVIDER = "passive";
    private int mRadius = DEFAULT_RADIUS;
    private boolean mSensor = false;
    private String mAddress = null;
    private double mBoundNorth = 0.0d;
    private double mBoundEast = 0.0d;
    private double mBoundSouth = 0.0d;
    private double mBoundWest = 0.0d;

    public SgnLocation() {
        super(SHOPGUN_PROVIDER);
    }

    public SgnLocation(SgnLocation l) {
        this();
        set(l);
    }

    public static SgnLocation fromJSON(JSONObject object) {
        SgnLocation l = new SgnLocation();
        if (object == null) {
            return l;
        }
        SgnJson o = new SgnJson(object);
        l.setAccuracy(o.getFloat(Parameters.ACCURACY));
        l.setAddress(o.getString(Parameters.ADDRESS));
        l.setAltitude(o.getDouble(Parameters.ALTITUDE));
        l.setBearing(o.getFloat(Parameters.BEARING));
        l.setLatitude(o.getDouble(Parameters.LATITUDE));
        l.setLongitude(o.getDouble(Parameters.LONGITUDE));
        l.setProvider(o.getString(Parameters.PROVIDER, SHOPGUN_PROVIDER));
        l.setRadius(o.getInt(Parameters.RADIUS, DEFAULT_RADIUS));
        l.setSpeed(o.getFloat(Parameters.SPEED));
        l.setTime(o.getLong(Parameters.TIME));
        l.setSensor(o.getBoolean(Parameters.SENSOR));
        double east = o.getDouble(Parameters.BOUND_EAST);
        double west = o.getDouble(Parameters.BOUND_WEST);
        double north = o.getDouble(Parameters.BOUND_NORTH);
        double south = o.getDouble(Parameters.BOUND_SOUTH);
        l.setBounds(north, east, south, west);
        return l;
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
     *
     * <ul>
     *      <li>Latitude</li>
     *      <li>Longitude</li>
     *      <li>Sensor</li>
     *      <li>Radius</li>
     * </ul>
     * @return The mapped JSONObject
     */
    public JSONObject toJSON() {
        SgnJson o = new SgnJson();
        o.put(Parameters.ACCURACY, getAccuracy());
        o.put(Parameters.ADDRESS, getAddress());
        o.put(Parameters.ALTITUDE, getAltitude());
        o.put(Parameters.BEARING, getBearing());
        o.put(Parameters.LATITUDE, getLatitude());
        o.put(Parameters.LONGITUDE, getLongitude());
        o.put(Parameters.PROVIDER, getProvider());
        o.put(Parameters.RADIUS, getRadius());
        o.put(Parameters.SPEED, getSpeed());
        o.put(Parameters.TIME, getTime());
        o.put(Parameters.SENSOR, isSensor());
        if (isBoundsSet()) {
            o.put(Parameters.BOUND_EAST, getBoundEast());
            o.put(Parameters.BOUND_NORTH, getBoundNorth());
            o.put(Parameters.BOUND_SOUTH, getBoundSouth());
            o.put(Parameters.BOUND_WEST, getBoundWest());
        }
        return o.toJSON();
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
     * Set radius for this location.
     * <p>Minimum radius is 0, and maximum radius is 700000</p>
     *
     * @param radius A radius distance in meters
     * @throws IllegalArgumentException if radius is out of bounds
     */
    public void setRadius(@IntRange(from = 0, to = 700000)int radius) {
        if (radius < RADIUS_MIN || radius > RADIUS_MAX) {
            throw new IllegalArgumentException(String.format(ERROR_RADIUS, RADIUS_MIN, RADIUS_MAX, radius));
        }
        mRadius = radius;
        setTimeNow();
    }

    @Override
    public void setLatitude(double latitude) {
        super.setLatitude(latitude);
        setTimeNow();
    }

    @Override
    public void setLongitude(double longitude) {
        super.setLongitude(longitude);
        setTimeNow();
    }

    /**
     * Update the time for this location to now.
     */
    private void setTimeNow() {
        super.setTime(System.currentTimeMillis());
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
        setTimeNow();
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
        setTimeNow();
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
        return (mBoundNorth != 0.0d &&
                mBoundSouth != 0.0d &&
                mBoundEast != 0.0d &&
                mBoundWest != 0.0d);
    }

    /**
     * Returns the approximate distance in meters between this location and the given location. Distance is defined using the WGS84 ellipsoid.
     *
     * @param store to measure distance to
     * @return the approximate distance in meters
     * @see SgnLocation#distanceTo(Location)
     */
    public int distanceTo(Store store) {
        Location tmp = new Location(SgnLocation.SHOPGUN_PROVIDER);
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
        setTimeNow();
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
        super.setLatitude(0.0d);
        super.setLongitude(0.0d);
        super.setProvider(SHOPGUN_PROVIDER);
        super.setSpeed(0.0f);
        setTimeNow();
        mAddress = null;
        mBoundEast = 0.0d;
        mBoundNorth = 0.0d;
        mBoundSouth = 0.0d;
        mBoundWest = 0.0d;
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

    public String toShortString() {
        return String.format(Locale.US,
                "%s, radius=%s, sensor=%s, lat=%.3f, lng=%.3f, time=%s",
                mAddress, mRadius, mSensor, getLatitude(), getLongitude(), new Date(getTime()).toGMTString());
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
     * <p><b>It's not an equals method</b></p>
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

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SgnLocation> CREATOR = new Parcelable.Creator<SgnLocation>() {
        @Override
        public SgnLocation createFromParcel(Parcel in) {
            Location location = Location.CREATOR.createFromParcel(in);
            return new SgnLocation(in, location);
        }

        @Override
        public SgnLocation[] newArray(int size) {
            return new SgnLocation[size];
        }
    };

    protected SgnLocation(Parcel in, Location location) {
        super(location);
        mRadius = in.readInt();
        mSensor = in.readByte() != 0x00;
        mAddress = in.readString();
        mBoundNorth = in.readDouble();
        mBoundEast = in.readDouble();
        mBoundSouth = in.readDouble();
        mBoundWest = in.readDouble();
    }

    @Override
    public int describeContents() {
        return super.describeContents();
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