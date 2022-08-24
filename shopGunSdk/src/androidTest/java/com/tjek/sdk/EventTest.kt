package com.tjek.sdk

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.tjek.sdk.api.remote.NetworkLogLevel
import com.tjek.sdk.database.TjekRoomDb
import com.tjek.sdk.eventstracker.EventShipper
import com.tjek.sdk.eventstracker.ShippableEvent
import com.tjek.sdk.eventstracker.api.EventEnvironment
import com.tjek.sdk.eventstracker.dummy
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        TjekRoomDb.getInstance(context).clearAllTables()
    }

    @Test
    fun testShipping() {
        TjekSDK.configure(
            enableLogCatMessages = true,
            networkLogLevel = NetworkLogLevel.Full,
            eventEnvironment = EventEnvironment.STAGING
        )
        val payloadType = mapOf(Pair("_a", "test"))
        val events = listOf(
            ShippableEvent(dummy().apply { mergePayload(payloadType) }),
            ShippableEvent(dummy().apply { mergePayload(payloadType) }),
            ShippableEvent(dummy().apply { mergePayload(payloadType) }),
        )
        val dao = TjekRoomDb.getInstance(context).eventDao()
        runBlocking {
            dao.insert(events)
            Assert.assertEquals(3, dao.getEvents().size)
            EventShipper(dao).shipEvents()
            Assert.assertEquals(0, dao.getEvents().size)
        }
    }
}