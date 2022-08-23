package com.shopgun.android.sdk

import com.tjek.sdk.TjekSDK
import com.tjek.sdk.api.remote.NetworkLogLevel
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.eventstracker.*
import com.tjek.sdk.eventstracker.api.EventEnvironment
import com.tjek.sdk.eventstracker.api.ShipEventRequest
import com.tjek.sdk.eventstracker.ShippableEvent
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class EventTest {

    @Test
    fun testShipDummyEvent() {
        TjekSDK.configure(
            enableLogCatMessages = true,
            networkLogLevel = NetworkLogLevel.Full,
            eventEnvironment = EventEnvironment.STAGING
        )
        val event1 = dummy()
        event1.mergePayload(mapOf(
            Pair("_field1", "blabla"), Pair("_field2", "abcdef")
        ))
        val event2 = dummy()
        event2.mergePayload(mapOf(
            Pair("_f1", "yyyy"), Pair("_f2", "ooooo")
        ))
        runBlocking {
            when (val res = ShipEventRequest.shipEvents(listOf(ShippableEvent(event1), ShippableEvent(event2)))) {
                is ResponseType.Error -> {
                    println(res.toString())
                    Assert.fail()
                }
                is ResponseType.Success -> {
                    println(res.data.toString())
                }
            }
        }
    }

    @Test
    fun testShipEvents() {
        TjekSDK.configure(
            enableLogCatMessages = true,
            networkLogLevel = NetworkLogLevel.Full,
            eventEnvironment = EventEnvironment.STAGING
        )
        val ppOpen = Event(
            type = EventType.PagedPublicationOpened.code,
            payloadType = mapOf(Pair("pp.id", "test_ppid"))
        )
        val ppPageOpen = Event(
            type = EventType.PagedPublicationPageOpened.code,
            payloadType = mapOf(
                Pair("pp.id", "test_ppid"),
                Pair("ppp.n", 4)
            )
        )
        val incitoOpen = Event(
            type = EventType.IncitoPublicationOpened_v2.code,
            payloadType = mapOf(Pair("ip.id", "test_incitoId"))
        )
        runBlocking {
            when (val res = ShipEventRequest.shipEvents(listOf(ShippableEvent(ppOpen), ShippableEvent(ppPageOpen), ShippableEvent(incitoOpen)))) {
                is ResponseType.Error -> {
                    println(res.toString())
                    Assert.fail()
                }
                is ResponseType.Success -> {
                    println(res.data.toString())
                }
            }
        }
    }

}