package com.tjek.sdk

import android.location.Location
import com.fonfon.geohash.GeoHash
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GeohashTest {

    private val PRECISION = 4

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

    @Test
    fun test_1() {
        val geoHash = GeoHash.fromLocation(location1, PRECISION)
        Assert.assertEquals("u3bu", geoHash.toString())
    }

    @Test
    fun test_2() {
        val geoHash = GeoHash.fromLocation(location2, PRECISION)
        Assert.assertEquals("9yc4", geoHash.toString())
    }

    @Test
    fun test_3() {
        val geoHash = GeoHash.fromLocation(location3, PRECISION)
        Assert.assertEquals("qfmm", geoHash.toString())
    }
}