package com.shopgun.android

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.tjek.sdk.TjekPreferences
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SharedPrefTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // delete files that can compromise the test
        val dataStoreFile = File(context.filesDir.parent?.plus("/files/datastore/") + "tjek_sdk_preferences.preferences_pb" )
        dataStoreFile.delete()
        val prefFile = File(context.filesDir.parent?.plus("/shared_prefs/") + "com.shopgun.android.sdk_preferences.xml")
        prefFile.delete()

        // create the old shared pref file and put some data
        context.getSharedPreferences("com.shopgun.android.sdk_preferences", Context.MODE_PRIVATE)
            .edit()
            .putString("installation_id", "123456789")
            .putBoolean("location_enabled", false)
            .putString("location_json", "{\"accuracy\":0,\"address\":\"Kastrup\",\"altitude\":0,\"bearing\":0,\"r_lat\":55.6204,\"r_lng\":12.5934,\"provider\":\"shopgun\",\"r_radius\":700000,\"speed\":0,\"time\":1660303746462,\"r_sensor\":false}")
            .commit()
    }

    @Test
    fun testSharedPrefMigration() {
        TjekPreferences.initialize(context)
        while (!TjekPreferences.initialized.get())
            Thread.sleep(100)
        Assert.assertEquals("123456789", TjekPreferences.installationId)
        // check that the old file doesn't exist
        val prefsPath = context.filesDir.parent?.plus("/shared_prefs/")
        val oldFile = File(prefsPath + "com.shopgun.android.sdk_preferences.xml")
        Assert.assertEquals(false, oldFile.exists())
        // check location data
        runBlocking {
            val legacyLocation = TjekPreferences.getLegacyLocation(context)
            Assert.assertEquals(true, legacyLocation != null)
            Assert.assertEquals("Kastrup", legacyLocation?.address)
            Assert.assertEquals(false, legacyLocation?.isLocationEnabled)
            Assert.assertEquals(55.6204, legacyLocation?.latitude)
            Assert.assertEquals(12.5934, legacyLocation?.longitude)
            Assert.assertEquals(700000, legacyLocation?.radius)
            Assert.assertEquals(false, legacyLocation?.sensor)
            Assert.assertEquals(0.0, legacyLocation?.accuracy)
            Assert.assertEquals(1660303746462, legacyLocation?.time)
        }
    }
}