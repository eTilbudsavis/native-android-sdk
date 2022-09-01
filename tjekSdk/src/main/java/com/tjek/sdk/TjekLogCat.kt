package com.tjek.sdk
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
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

internal object TjekLogCat {

    private const val tag = "tjek-sdk"
    private val logEnabled = AtomicBoolean(false)

    var exceptionLogger: (Exception) -> Unit = { }

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
        exceptionLogger(e)
        if (!logEnabled.get()) return
        e.printStackTrace()
    }

    fun forceE(message: String) {
        Log.e(tag, message)
    }

}