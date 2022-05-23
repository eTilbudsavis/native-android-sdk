package com.tjek.sdk

import android.content.Context

class TjekSDK constructor(context: Context) {

    companion object {

        @Volatile
        private var INSTANCE: TjekSDK? = null

        @JvmStatic
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TjekSDK(context).also {
                    INSTANCE = it
                }
            }
    }

    private val appContext = context.applicationContext

    init {
        performMigration()
    }

    private fun performMigration() {
        // todo performMigration
    }

}