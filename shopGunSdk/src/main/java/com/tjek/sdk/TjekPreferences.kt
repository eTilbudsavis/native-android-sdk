package com.tjek.sdk

import android.content.Context
import android.content.SharedPreferences
import com.tjek.sdk.TjekPreferences
import com.shopgun.android.sdk.utils.SgnUtils
import com.shopgun.android.sdk.ShopGun
import com.shopgun.android.sdk.utils.Constants

internal object TjekPreferences {

    // todo: name change? how? is worth it?
    private const val PREFS_NAME = "com.shopgun.android.sdk_preferences"
    private const val INSTALLATION_ID = "installation_id"

    private lateinit var sharedPref: SharedPreferences

    val installationId: String
        get() {
            if (!sharedPref.contains(INSTALLATION_ID)) {
                sharedPref.edit().putString(INSTALLATION_ID, createUUID()).apply()
            }
            return sharedPref.getString(INSTALLATION_ID, null) ?: ""
        }

    fun setSharedPreferences(context: Context) {
        sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

}