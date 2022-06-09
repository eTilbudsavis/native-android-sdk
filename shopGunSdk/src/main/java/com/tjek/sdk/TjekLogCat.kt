package com.tjek.sdk

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

internal object TjekLogCat {

    private const val tag = "tjek-sdk"
    private val logEnabled = AtomicBoolean(false)

    fun enableLogging() {
        logEnabled.set(true)
    }

    fun e(message: String) {
        if (!logEnabled.get()) return
        Log.e(tag, message)
    }

    fun d(message: String) {
        if (!logEnabled.get()) return
        Log.d(tag, message)
    }

    fun v(message: String) {
        if (!logEnabled.get()) return
        Log.v(tag, message)
    }

    fun w(message: String) {
        if (!logEnabled.get()) return
        Log.w(tag, message)
    }

    fun printStackTrace(e: Exception) {
        if (!logEnabled.get()) return
        e.printStackTrace()
    }

    fun forceE(message: String) {
        Log.e(tag, message)
    }
}