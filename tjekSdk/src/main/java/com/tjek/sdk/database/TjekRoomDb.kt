package com.tjek.sdk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tjek.sdk.eventstracker.EventDao
import com.tjek.sdk.eventstracker.ShippableEvent


@Database(entities = [
    ShippableEvent::class
    ], version = 1)
abstract class TjekRoomDb : RoomDatabase() {
    abstract fun eventDao(): EventDao

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: TjekRoomDb? = null

        fun getInstance(context: Context): TjekRoomDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): TjekRoomDb {
            return Room.databaseBuilder(context, TjekRoomDb::class.java, "tjek_sdk_db").build()
        }
    }
}

