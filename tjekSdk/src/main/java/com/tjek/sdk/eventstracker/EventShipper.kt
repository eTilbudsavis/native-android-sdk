package com.tjek.sdk.eventstracker
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.eventstracker.api.EventStatus
import com.tjek.sdk.eventstracker.api.ShipEventRequest
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.TimeUnit

internal class EventShipper(
    private val eventDao: EventDao
) {

    private val tag = "EventShipper"
    private val mutex = Mutex()

    suspend fun shipEvents() {
        mutex.lock()

        val toBeShipped = eventDao.getEvents()
        if (toBeShipped.isEmpty()) {
            TjekLogCat.v("$tag: no event to ship at the moment")
            mutex.unlock()
            return
        }

        TjekLogCat.v("$tag shipping ${toBeShipped.size}")
        when (val res = ShipEventRequest.shipEvents(toBeShipped)) {
            is ResponseType.Error -> {
                TjekLogCat.e("$tag: $res")
                mutex.unlock()
                return
            }
            is ResponseType.Success -> {
                // delete ack-ed events
                val ack = res.data.events.filter { it.status == EventStatus.ack }.map { it.id }
                if (ack.isNotEmpty()) {
                    eventDao.deleteEvents(ack)
                }
                // check timestamp for nack-ed events and delete them if they're too old
                val nack = res.data.events
                    .filter { it.status == EventStatus.nack }
                    .also { if (it.isNotEmpty()) TjekLogCat.w("$tag nack events $it") }
                    .map { it.id }
                val timeLimit =
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - TimeUnit.HOURS.toSeconds(36)
                val oldNack = toBeShipped.filter { it.timestamp < timeLimit && nack.contains(it.id) }.map { it.id }
                if (nack.isNotEmpty()) {
                    eventDao.deleteEvents(oldNack)
                }
                // other status
                val other =
                    res.data.events
                        .filter { it.status != EventStatus.ack && it.status != EventStatus.nack }
                        .also { if (it.isNotEmpty()) TjekLogCat.w("$tag $it") }
                        .map { it.id }
                if (other.isNotEmpty()) {
                    eventDao.deleteEvents(other)
                }

                // print some logs
                TjekLogCat.v("$tag: Ack=${ack.size}, Nack=${nack.size} (${oldNack.size} too old), other=${other.size}")
                mutex.unlock()
            }
        }
    }
}