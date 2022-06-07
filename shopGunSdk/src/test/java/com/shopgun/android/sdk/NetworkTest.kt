package com.shopgun.android.sdk

import com.tjek.sdk.TjekSDK
import com.tjek.sdk.api.TjekAPI
import com.tjek.sdk.api.remote.EndpointEnvironment
import com.tjek.sdk.api.remote.NetworkLogLevel
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class NetworkTest {

    @Test
    fun testPublications() {
        TjekSDK.configure(
            enableLogCatMessages = true,
            networkLogLevel = NetworkLogLevel.Full,
            endpointEnvironment = EndpointEnvironment.STAGING
        )
        runBlocking {
            val list = TjekAPI.getPublications()
            Assert.assertEquals(true, list.isNotEmpty())
        }
    }

    @Test
    fun testPublication() {
        TjekSDK.configure(
            enableLogCatMessages = true,
            networkLogLevel = NetworkLogLevel.Full,
            endpointEnvironment = EndpointEnvironment.STAGING
        )
        runBlocking {
            val list = TjekAPI.getPublications()
            Assert.assertEquals(true, list.isNotEmpty())
            val pub = TjekAPI.getPublication(list[0].id)
            Assert.assertEquals(true, pub.id.isNotBlank())
        }
    }
}