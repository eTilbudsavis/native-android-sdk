package com.tjek.sdk

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

internal object TjekLogCat {

    private val logEnabled = AtomicBoolean(false)

    fun enableLogging() {
        logEnabled.set(true)
    }

    fun e(tag: String, message: String) {
        if (!logEnabled.get()) return
        Log.e(tag, message)
    }

    fun d(tag: String, message: String) {
        if (!logEnabled.get()) return
        Log.d(tag, message)
    }

    fun v(tag: String, message: String) {
        if (!logEnabled.get()) return
        Log.v(tag, message)
    }

    fun w(tag: String, message: String) {
        if (!logEnabled.get()) return
        Log.w(tag, message)
    }
}