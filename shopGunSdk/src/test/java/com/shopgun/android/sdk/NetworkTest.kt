package com.shopgun.android.sdk

import android.os.Bundle
import com.tjek.sdk.TjekSDK
import com.tjek.sdk.api.TjekAPI
import com.tjek.sdk.api.models.Publication
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

    @Test
    fun testPublicationParcel() {
        TjekSDK.configure(
            enableLogCatMessages = true,
            endpointEnvironment = EndpointEnvironment.STAGING
        )
        runBlocking {
            val list = TjekAPI.getPublications()
            Assert.assertEquals(true, list.isNotEmpty())
            val pub = TjekAPI.getPublication(list[0].id)
            Assert.assertEquals(true, pub.id.isNotBlank())

            val b = Bundle()
            b.putParcelable("data", pub)
            val data: Publication? = b.getParcelable("data")
            Assert.assertEquals(pub.id, data?.id ?: "")
            Assert.assertEquals(pub.runDateRange.start, data?.runDateRange?.start)
            Assert.assertEquals(pub.runDateRange.endInclusive, data?.runDateRange?.endInclusive)
            Assert.assertEquals(pub.hasIncitoPublication, data?.hasIncitoPublication)
            Assert.assertEquals(pub.hasPagedPublication, data?.hasPagedPublication)
        }
    }
}