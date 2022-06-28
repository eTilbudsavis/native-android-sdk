package com.tjek.sdk

import android.content.res.Configuration

enum class DeviceOrientation {
    Portrait, Landscape
}

fun Configuration.getDeviceOrientation(): DeviceOrientation =
    when(orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> DeviceOrientation.Landscape
        else -> DeviceOrientation.Portrait
    }