package com.tjek.sdk.eventstracker

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

