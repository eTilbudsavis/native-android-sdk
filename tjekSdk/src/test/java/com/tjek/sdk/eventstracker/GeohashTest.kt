package com.tjek.sdk.eventstracker

import android.location.Location
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GeohashTest {

    private var location1 = Location("geohash").apply {
        latitude = 55.679633
        longitude = 12.577850
    }
    private var location2 = Location("geohash").apply {
        latitude = 38.354052
        longitude = -99.571113
    }
    private var location3 = Location("geohash").apply {
        latitude = -31.386644
        longitude = 131.341086
    }
    private var location4 = Location("geohash").apply {
        latitude = 48.668983
        longitude = -4.329021
    }

    private var location5 = Location("geohash").apply {
        latitude = 55.64852
        longitude =  12.65419
    }

    @Test
    fun test_1() {
        val geoHash = GeoHash.fromLocation(location1, 4)
        Assert.assertEquals("u3bu", geoHash.toString())
    }

    @Test
    fun test_2() {
        val geoHash = GeoHash.fromLocation(location2, 4)
        Assert.assertEquals("9yc4", geoHash.toString())
    }

    @Test
    fun test_3() {
        val geoHash = GeoHash.fromLocation(location3, 4)
        Assert.assertEquals("qfmm", geoHash.toString())
    }

    @Test
    fun test_4() {
        val geoHash = GeoHash.fromLocation(location4, 9)
        Assert.assertEquals("gbsuv7ztq", geoHash.toString())
    }

    @Test
    fun test_5() {
        val geoHash = GeoHash.fromLocation(location5, 9)
        Assert.assertEquals("u3buxfw2p", geoHash.toString())
    }
}