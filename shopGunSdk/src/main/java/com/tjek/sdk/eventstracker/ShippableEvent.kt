package com.tjek.sdk.eventstracker

import androidx.room.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import com.tjek.sdk.eventstracker.Event

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

class EventAdapter {

    @ToJson fun toJson(e: ShippableEvent): String {
        return e.jsonEvent
    }

    @FromJson fun fromJson(json: String): ShippableEvent? {
        // implementation not needed
        return null
    }
}


@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(event: ShippableEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(events: List<ShippableEvent>)

    @Query("DELETE FROM shippable_events WHERE `id` = (:ids)")
    fun deleteEvents(ids: List<String>)

    @Query("SELECT * FROM shippable_events LIMIT :limit")
    fun getEvents(limit: Int = 100): List<ShippableEvent>
}

