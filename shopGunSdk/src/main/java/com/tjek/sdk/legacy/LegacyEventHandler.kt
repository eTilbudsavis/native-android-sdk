package com.tjek.sdk.legacy

import android.content.Context
import com.tjek.sdk.eventstracker.ShippableEvent
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import java.io.File

internal object LegacyEventHandler {

    private var realmConfiguration: RealmConfiguration? = null

    fun initialize(context: Context) {
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