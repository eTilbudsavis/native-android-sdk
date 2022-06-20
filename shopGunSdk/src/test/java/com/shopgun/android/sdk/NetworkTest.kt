package com.shopgun.android.sdk

import android.os.Bundle
import com.tjek.sdk.TjekSDK
import com.tjek.sdk.api.TjekAPI
import com.tjek.sdk.api.models.PublicationV2
import com.tjek.sdk.api.models.StoreV2
import com.tjek.sdk.api.remote.*
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
            when (val response = TjekAPI.getPublications()) {
                is ResponseType.Error -> Assert.fail(response.toString())
                is ResponseType.Success -> {
                    Assert.assertEquals(true, response.data?.isNotEmpty())
                }
            }
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
            Assert.assertEquals(true, (list as ResponseType.Success).data?.isNotEmpty())
            when (val pub = TjekAPI.getPublication(list.data!![0].id)) {
                is ResponseType.Error -> Assert.fail(pub.toString())
                is ResponseType.Success -> {
                    Assert.assertEquals(true, pub.data!!.id.isNotBlank())
                }
            }
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
            Assert.assertEquals(true, (list as ResponseType.Success).data?.isNotEmpty())
            val pub = TjekAPI.getPublication(list.data!![0].id)
            Assert.assertEquals(true, pub.data!!.id.isNotBlank())

            val b = Bundle()
            b.putParcelable("data", pub.data)
            val data: PublicationV2? = b.getParcelable("data")
            Assert.assertEquals(pub.data!!.id, data?.id ?: "")
            Assert.assertEquals(pub.data!!.runDateRange.start, data?.runDateRange?.start)
            Assert.assertEquals(pub.data!!.runDateRange.endInclusive, data?.runDateRange?.endInclusive)
            Assert.assertEquals(pub.data!!.hasIncitoPublication, data?.hasIncitoPublication)
            Assert.assertEquals(pub.data!!.hasPagedPublication, data?.hasPagedPublication)
        }
    }

    @Test
    fun testStoreParcel() {
        TjekSDK.configure(
            enableLogCatMessages = true,
            endpointEnvironment = EndpointEnvironment.STAGING
        )
        runBlocking {
            val store = TjekAPI.getStore("2y-IoB4cMW4hkgI5GCBRN")
            Assert.assertEquals(true, store.data!!.id.isNotBlank())

            val b = Bundle()
            b.putParcelable("data", store.data)
            val data: StoreV2? = b.getParcelable("data")
            Assert.assertEquals(store.data!!.id, data?.id ?: "")
            Assert.assertEquals(store.data!!.openingHours?.size, data?.openingHours?.size)
        }
    }

    @Test
    fun testRequestError() {
        TjekSDK.configure(
            enableLogCatMessages = true,
            endpointEnvironment = EndpointEnvironment.STAGING
        )
        runBlocking {
            when (val p = TjekAPI.getPublication("invalidIdForTest")) {
                is ResponseType.Error -> {
                    when (val e = p.errorType) {
                        is ErrorType.Api -> Assert.assertEquals(APIError.ErrorName.CatalogNotFound, e.error.knownErrorName)
                        else -> Assert.fail()
                    }
                }
                is ResponseType.Success -> Assert.fail("this shouldn't be possible")
            }
        }
    }
}