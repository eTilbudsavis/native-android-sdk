package com.tjek.sdk

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

object TjekPreferences {

    private val INSTALLATION_ID = stringPreferencesKey("installation_id")

    // these two info won't be stored in the sdk anymore.
    // It's possible to retrieve them one last time when migrating to the new sdk
    //todo
    private val LEGACY_LOCATION_JSON = stringPreferencesKey("location_json")
    private val LEGACY_LOCATION_ENABLED_FLAG = stringPreferencesKey("location_enabled")

    var initialized = AtomicBoolean(false)

    var installationId: String = ""
    private set

    private val Context.dataStore by preferencesDataStore(
        name = "tjek_sdk_preferences",
        produceMigrations = { context ->
            listOf(SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "com.shopgun.android.sdk_preferences",
                keysToMigrate = setOf("installation_id", "location_json", "location_enabled")
            ))
        }
    )

    fun initAtStartup(context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            installationId = context.dataStore.data.firstOrNull()?.get(INSTALLATION_ID)
                ?: createUUID().also { id -> context.dataStore.edit { it[INSTALLATION_ID] = id } }

            initialized.set(true)
        }
    }
}