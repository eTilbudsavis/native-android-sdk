package com.tjek.sdk.database
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

