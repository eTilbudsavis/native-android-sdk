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
import androidx.room.*

@Entity(tableName = "shippable_events")
data class ShippableEvent(
    @PrimaryKey val id: String,
    val version: Int,
    val timestamp: Long,

    // this is the event in the json format, ready to be sent to the server
    val jsonEvent: String
) {
    constructor(event: Event): this(
        id = event.id,
        version = event.version,
        timestamp = event.timestamp,
        jsonEvent = event.toJson()
    )
}

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ShippableEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<ShippableEvent>)

    @Query("DELETE FROM shippable_events WHERE `id` IN (:ids)")
    suspend fun deleteEvents(ids: List<String>)

    @Query("SELECT * FROM shippable_events")
    suspend fun getEvents(): List<ShippableEvent>

    @Query("SELECT COUNT(*) FROM shippable_events")
    suspend fun getCount(): Int
}

