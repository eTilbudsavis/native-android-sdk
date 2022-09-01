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
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

enum class DeviceOrientation {
    Portrait, Landscape
}

fun Configuration.getDeviceOrientation(): DeviceOrientation =
    when(orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> DeviceOrientation.Landscape
        else -> DeviceOrientation.Portrait
    }

/**
 * Representation of languages including region.
 * The basic format is ll_CC, where ll is a two-letter language code, and CC is a two-letter country code.
 * Examples:
 * en_US, da_DK, en_DK
 * @param ctx context
 * @return a string in the form "ll_CC", like "en_GB"
 */
fun getFormattedLocale(ctx: Context): String {
    val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // this is updated more frequently than Locale.getDefault()
        ctx.resources.configuration.locales[0]
    } else {
        @Suppress("DEPRECATION")
        ctx.resources.configuration.locale
    }
    val language = locale.language.ifEmpty { Locale.ENGLISH.language }
    val country = locale.country.ifEmpty { Locale.US.country }
    return String.format("%s_%s", language, country)
}

fun createUUID() = UUID.randomUUID().toString()