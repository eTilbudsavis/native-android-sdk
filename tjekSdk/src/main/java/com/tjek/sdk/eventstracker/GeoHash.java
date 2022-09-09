package com.tjek.sdk.eventstracker;

/*
 * Credits https://github.com/drfonfon/android-geohash
 *
 * Only relevant parts have been kept in this version.
 *
 */

import android.location.Location;

import androidx.annotation.NonNull;

import com.tjek.sdk.TjekLogCat;

final class GeoHash {

    public static final int MAX_CHARACTER_PRECISION = 12;

    public static final double LATITUDE_MAX_ABS = 90.0;
    public static final double LONGITUDE_MAX_ABS = 180.0;
    public static final int MAX_BIT_PRECISION = Long.bitCount(Long.MAX_VALUE) + 1;// max - 64;
    public static final int BASE32_BITS = 5;
    public static final String base32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    public static final int MAX_GEO_HASH_BITS_COUNT = BASE32_BITS * MAX_CHARACTER_PRECISION;

    private long bits = 0;
    private byte significantBits = 0;


    /**
     * Generate {@link GeoHash} from
     *
     * @param location           {@link Location} object
     * @param numberOfCharacters max characters count - 12
     * @return new {@link GeoHash}
     */
    public static GeoHash fromLocation(Location location, int numberOfCharacters) {
        if (numberOfCharacters > MAX_CHARACTER_PRECISION) {
            TjekLogCat.INSTANCE.v("A geohash can only be " + MAX_CHARACTER_PRECISION + " character long.");
            numberOfCharacters = MAX_CHARACTER_PRECISION;
        }
        int desiredPrecision = Math.min(numberOfCharacters * BASE32_BITS, MAX_GEO_HASH_BITS_COUNT);
        return new GeoHash(location.getLatitude(), location.getLongitude(), desiredPrecision);
    }

    @NonNull
    @Override
    public String toString() {
        if (significantBits % BASE32_BITS != 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder();

        long firstFiveBitsMask = 0xf800000000000000L;
        long bitsCopy = bits;
        int partialChunks = (int) Math.ceil(((double) significantBits / BASE32_BITS));

        for (int i = 0; i < partialChunks; i++) {
            int pointer = (int) ((bitsCopy & firstFiveBitsMask) >>> 59);
            buf.append(base32.charAt(pointer));
            bitsCopy <<= BASE32_BITS;
        }
        return buf.toString();
    }

    private GeoHash(double latitude, double longitude, int desiredPrecision) {
        desiredPrecision = Math.min(desiredPrecision, MAX_BIT_PRECISION);
        boolean isEvenBit = true;
        double[] latitudeRange = {-LATITUDE_MAX_ABS, LATITUDE_MAX_ABS};
        double[] longitudeRange = {-LONGITUDE_MAX_ABS, LONGITUDE_MAX_ABS};

        while (significantBits < desiredPrecision) {
            if (isEvenBit) {
                divideRangeEncode(longitude, longitudeRange);
            } else {
                divideRangeEncode(latitude, latitudeRange);
            }
            isEvenBit = !isEvenBit;
        }

        bits <<= (MAX_BIT_PRECISION - desiredPrecision);
    }

    private void divideRangeEncode(double value, double[] range) {
        double mid = (range[0] + range[1]) / 2;
        if (value >= mid) {
            addOnBitToEnd();
            range[0] = mid;
        } else {
            addOffBitToEnd();
            range[1] = mid;
        }
    }

    private void addOnBitToEnd() {
        significantBits++;
        bits <<= 1;
        bits = bits | 0x1;
    }

    private void addOffBitToEnd() {
        significantBits++;
        bits <<= 1;
    }
}
