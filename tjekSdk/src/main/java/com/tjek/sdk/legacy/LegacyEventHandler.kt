package com.tjek.sdk.legacy
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
import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.eventstracker.ShippableEvent
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import java.io.File

internal object LegacyEventHandler {

    private var realmConfiguration: RealmConfiguration? = null

    fun initialize(context: Context) {
        try {
            Realm.init(context)
            // create a configuration based on what we used to have
            realmConfiguration = RealmConfiguration.Builder()
                .name("com.shopgun.android.sdk.anonymousEvent.realm")
                .modules(LegacyEventRealmModule())
                .schemaVersion(2)
                .build()
            // check if there are events in the old database: do we have an old db file?
            if (File(realmConfiguration!!.path).exists()) {
                var isEmpty = true
                try {
                    val realm = Realm.getInstance(realmConfiguration!!)
                    isEmpty = realm.isEmpty
                    if (isEmpty) {
                        // if it's empty, try to delete it
                        realm.close()
                        Realm.deleteRealm(realmConfiguration!!)
                    }
                } catch (ignore: Exception) {
                } finally {
                    if (isEmpty) {
                        realmConfiguration = null
                    }
                }
            } else {
                // the db doesn't exist
                realmConfiguration = null
            }
        } catch (e: Exception) {
            // Realm.init could throw exceptions
            realmConfiguration = null
            TjekLogCat.printStackTrace(e)
        }
    }

    fun getLegacyEvents(): Result<List<ShippableEvent>> = runCatching {
        realmConfiguration?.let {
            val realm = Realm.getInstance(it)
            val events: RealmResults<AnonymousEventWrapper> =
                realm.where(AnonymousEventWrapper::class.java).findAll()
            realm.close()
            events.map { wrapper ->
                ShippableEvent(
                    id = wrapper.id,
                    version = wrapper.version,
                    timestamp = wrapper.timestamp,
                    jsonEvent = wrapper.event
                )
            }
        } ?: emptyList()
    }

}